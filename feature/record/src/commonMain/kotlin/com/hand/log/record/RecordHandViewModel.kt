package com.hand.log.record

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hand.log.domain.model.Action
import com.hand.log.domain.model.ActionType
import com.hand.log.domain.model.Blinds
import com.hand.log.domain.model.Card
import com.hand.log.domain.model.HandRecord
import com.hand.log.domain.model.HeroHand
import com.hand.log.domain.model.Street
import com.hand.log.domain.repository.HandRecordRepository
import com.hand.log.domain.repository.PokerTableRepository
import com.hand.log.record.contract.CardSelectorTarget
import com.hand.log.record.contract.RecordHandEffect
import com.hand.log.record.contract.RecordHandModalEffect
import com.hand.log.record.contract.RecordHandState
import com.hand.log.record.contract.RecordStep
import com.hand.log.record.model.PlayerStatus
import com.hand.log.record.model.RecordPlayers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

internal class RecordHandViewModel(
	private val tableId: String,
	private val pokerTableRepository: PokerTableRepository,
	private val handRecordRepository: HandRecordRepository,
) : ViewModel() {

	private val _state = MutableStateFlow<RecordHandState>(RecordHandState.Loading)
	val state: StateFlow<RecordHandState> get() = _state

	private val _modalEffect = MutableStateFlow<RecordHandModalEffect>(RecordHandModalEffect.Idle)
	val modalEffect: StateFlow<RecordHandModalEffect> get() = _modalEffect

	private val _effect = MutableSharedFlow<RecordHandEffect>()
	val effect: SharedFlow<RecordHandEffect> get() = _effect.asSharedFlow()

	init {
		loadTable()
	}

	private fun loadTable() {
		viewModelScope.launch {
			val table = pokerTableRepository.getTableById(tableId)
			val defaultStack = table?.startingStack ?: 0.0
			val stacks = table?.players?.associate { it.seat to it.stack } ?: emptyMap()

			_state.value = RecordHandState.Recording(
				tableId = tableId,
				table = table,
				players = RecordPlayers.create(
					playerCount = table?.playerCount ?: 0,
					defaultStack = defaultStack,
					stacks = stacks,
				),
				buttonSeat = 1,
				blinds = table?.blinds,
			)
		}
	}

	// --- Card Selection ---

	fun selectHeroCard() {
		val current = _state.value as? RecordHandState.Recording ?: return
		val availableCards = current.selectedCards - current.heroCards.toSet()
		_modalEffect.value = RecordHandModalEffect.ShowCardSelector(
			target = CardSelectorTarget.HeroCard(maxCards = 2),
			selectedCards = availableCards,
		)
	}

	fun selectBoardCard(street: Street) {
		val current = _state.value as? RecordHandState.Recording ?: return
		val maxCards = when (street) {
			Street.FLOP -> 3
			else -> 1
		}
		val existingCards = current.streets.getCards(street).toSet()
		val availableCards = current.selectedCards - existingCards
		_modalEffect.value = RecordHandModalEffect.ShowCardSelector(
			target = CardSelectorTarget.BoardCard(street, maxCards = maxCards),
			selectedCards = availableCards,
		)
	}

	fun onCardsSelected(cards: List<Card>) {
		val current = _state.value as? RecordHandState.Recording ?: return
		val modal = _modalEffect.value as? RecordHandModalEffect.ShowCardSelector ?: return

		when (modal.target) {
			is CardSelectorTarget.HeroCard -> {
				val oldCards = current.heroCards.toSet()
				val newHeroHand = if (cards.size >= 2) {
					HeroHand(card1 = cards[0], card2 = cards[1])
				} else {
					null
				}
				updateRecording {
					copy(
						heroHand = newHeroHand,
						selectedCards = selectedCards - oldCards + cards,
					)
				}
			}

			is CardSelectorTarget.BoardCard -> {
				val street = modal.target.street
				val oldCards = current.streets.getCards(street).toSet()
				updateRecording {
					copy(
						streets = streets.setCards(street, cards),
						selectedCards = selectedCards - oldCards + cards,
					)
				}
			}
		}
		dismissModal()
	}

	fun dismissModal() {
		_modalEffect.value = RecordHandModalEffect.Idle
	}

	// --- Setup ---

	fun updateHeroStack(amount: String) {
		val current = _state.value as? RecordHandState.Recording ?: return
		val heroSeat = current.table?.heroSeat ?: return
		updatePlayerStack(heroSeat, amount)
	}

	fun updateButtonSeat(seat: Int) {
		updateRecording { copy(buttonSeat = seat) }
	}

	fun updateBlinds(sb: String, bb: String) {
		val sbValue = sb.toDoubleOrNull() ?: 0.0
		val bbValue = bb.toDoubleOrNull() ?: 0.0
		updateRecording { copy(blinds = Blinds(sb = sbValue, bb = bbValue)) }
	}

	fun updatePlayerStack(seat: Int, amount: String) {
		val stack = amount.toDoubleOrNull() ?: return
		updateRecording {
			copy(players = players.update(seat) { copy(stack = stack) })
		}
	}

	// --- Action ---

	fun selectActionSeat(seat: Int) {
		updateRecording { copy(currentActionSeat = seat) }
	}

	fun selectActionType(type: ActionType) {
		updateRecording { copy(currentActionType = type) }
		if (type != ActionType.BET && type != ActionType.RAISE) {
			confirmAction()
		}
	}

	fun updateActionAmount(amount: String) {
		updateRecording { copy(currentActionAmount = amount) }
	}

	fun confirmAction() {
		val current = _state.value as? RecordHandState.Recording ?: return
		val seat = current.currentActionSeat ?: return
		val type = current.currentActionType ?: return
		val player = current.players[seat] ?: return

		// 액션 타입 & 금액 결정
		var resolvedType = type
		val amount: Double? = when (type) {
			ActionType.FOLD, ActionType.CHECK -> null
			ActionType.CALL -> {
				val streetActions = current.streets.getActions(current.currentStreet)
				val maxBet = streetActions.maxOfOrNull { it.amount ?: 0.0 } ?: current.blinds?.bb ?: 0.0
				if (maxBet >= player.stack) {
					resolvedType = ActionType.ALL_IN
					player.stack
				} else {
					maxBet
				}
			}
			ActionType.ALL_IN -> player.stack
			ActionType.BET, ActionType.RAISE -> {
				val betAmount = current.currentActionAmount.toDoubleOrNull() ?: 0.0
				if (betAmount >= player.stack) {
					resolvedType = ActionType.ALL_IN
					player.stack
				} else if (betAmount < current.minRaiseAmount) {
					// 최소 레이즈 미만이면 무시
					return
				} else {
					betAmount
				}
			}
		}

		// Streets & Players 업데이트
		val action = Action(playerSeat = seat, type = resolvedType, amount = amount)
		val updatedStreets = current.streets.addAction(current.currentStreet, action)

		val newStack = if (amount != null && amount > 0.0) {
			(player.stack - amount).coerceAtLeast(0.0)
		} else {
			player.stack
		}
		val newStatus = when (resolvedType) {
			ActionType.FOLD -> PlayerStatus.FOLDED
			ActionType.ALL_IN -> PlayerStatus.ALL_IN
			else -> player.status
		}
		val updatedPlayers = current.players.update(seat) {
			copy(
				stack = newStack,
				status = newStatus,
				currentBet = currentBet + (amount ?: 0.0),
			)
		}

		// Aggressor 갱신 (올인도 기존 최대 베팅보다 크면 레이즈 역할)
		val isRaiseAction = resolvedType == ActionType.BET || resolvedType == ActionType.RAISE
		val isAllInRaise = resolvedType == ActionType.ALL_IN && (amount ?: 0.0) > (
			current.streets.getActions(current.currentStreet)
				.filter { it.playerSeat != seat }
				.maxOfOrNull { it.amount ?: 0.0 } ?: 0.0
			)
		val newAggressor = if (isRaiseAction || isAllInRaise) {
			seat
		} else {
			current.lastAggressorSeat
		}

		// 다음 좌석 결정
		val nextState = current.copy(streets = updatedStreets, players = updatedPlayers)
		val nextSeat = getNextActionSeat(nextState, seat, newAggressor)

		updateRecording {
			copy(
				streets = updatedStreets,
				players = updatedPlayers,
				currentActionSeat = nextSeat,
				currentActionType = null,
				currentActionAmount = "",
				lastAggressorSeat = newAggressor,
			)
		}

		if (nextSeat == null) {
			nextStep()
		}
	}

	fun removeLastAction() {
		val current = _state.value as? RecordHandState.Recording ?: return
		val streetActions = current.streets.getActions(current.currentStreet)
		if (streetActions.isEmpty()) return

		val removedAction = streetActions.last()
		val seat = removedAction.playerSeat

		val updatedPlayers = current.players.update(seat) {
			copy(
				stack = stack + (removedAction.amount ?: 0.0),
				status = if (removedAction.type == ActionType.FOLD || removedAction.type == ActionType.ALL_IN) {
					PlayerStatus.ACTIVE
				} else {
					status
				},
				currentBet = (currentBet - (removedAction.amount ?: 0.0)).coerceAtLeast(0.0),
			)
		}

		updateRecording {
			copy(
				streets = streets.removeLastAction(currentStreet),
				players = updatedPlayers,
				currentActionSeat = seat,
			)
		}
	}

	// --- Navigation ---

	fun nextStep() {
		updateRecording {
			val steps = RecordStep.entries
			val currentIndex = steps.indexOf(currentStep)
			if (currentIndex < steps.lastIndex) {
				val nextStep = steps[currentIndex + 1]
				val nextStreet = when (nextStep) {
					RecordStep.SETUP -> currentStreet
					RecordStep.PREFLOP -> Street.PREFLOP
					RecordStep.FLOP -> Street.FLOP
					RecordStep.TURN -> Street.TURN
					RecordStep.RIVER -> Street.RIVER
				}

				// 프리플랍: aggressor 없이 시작 → BB까지 한 바퀴 돌고 BB 옵션 보장
				// 포스트플랍: aggressor 없이 시작 → 모든 active 좌석이 액션하면 종료
				val initialAggressor: Int? = null

				val updated = copy(
					currentStep = nextStep,
					currentStreet = nextStreet,
					streets = streets.ensureStreet(nextStreet),
					players = players.resetCurrentBets(),
					currentActionType = null,
					currentActionAmount = "",
					lastAggressorSeat = initialAggressor,
				)
				val firstSeat = updated.actionOrder.firstOrNull()
				updated.copy(currentActionSeat = firstSeat)
			} else {
				this
			}
		}
	}

	fun previousStep() {
		updateRecording {
			val steps = RecordStep.entries
			val currentIndex = steps.indexOf(currentStep)
			if (currentIndex > 0) {
				val prevStep = steps[currentIndex - 1]
				val prevStreet = when (prevStep) {
					RecordStep.SETUP -> currentStreet
					RecordStep.PREFLOP -> Street.PREFLOP
					RecordStep.FLOP -> Street.FLOP
					RecordStep.TURN -> Street.TURN
					RecordStep.RIVER -> Street.RIVER
				}
				val prevUpdated = copy(
					currentStep = prevStep,
					currentStreet = prevStreet,
					currentActionType = null,
					currentActionAmount = "",
					lastAggressorSeat = null,
				)
				val lastAction = prevUpdated.streets.getActions(prevStreet).lastOrNull()
				prevUpdated.copy(currentActionSeat = lastAction?.playerSeat)
			} else {
				this
			}
		}
	}

	// --- Result ---

	fun updateResult(amount: String) {
		updateRecording { copy(result = amount) }
	}

	fun updateMemo(text: String) {
		updateRecording { copy(memo = text) }
	}

	@OptIn(ExperimentalTime::class)
	fun saveHand() {
		val current = _state.value as? RecordHandState.Recording ?: return

		viewModelScope.launch {
			try {
				val now = Clock.System.now().toEpochMilliseconds()
				val handRecord = HandRecord(
					id = generateId(),
					tableId = current.tableId,
					createdAt = now,
					blinds = current.blinds,
					heroHand = current.heroHand,
					heroStack = current.heroStack,
					buttonSeat = current.buttonSeat,
					streets = current.streets.toHandStreets(),
					result = current.result.toDoubleOrNull(),
					memo = current.memo.ifBlank { null },
				)

				handRecordRepository.saveHand(handRecord) {
					viewModelScope.launch {
						_effect.emit(RecordHandEffect.SaveSuccess)
					}
				}
			} catch (e: Exception) {
				_effect.emit(RecordHandEffect.SaveError(e.message ?: "저장 중 오류가 발생했습니다"))
			}
		}
	}

	// --- Helpers ---

	private fun getNextActionSeat(
		state: RecordHandState.Recording,
		currentSeat: Int,
		lastAggressor: Int?,
	): Int? {
		val fullOrder = if (state.currentStreet == Street.PREFLOP) {
			state.preflopActionOrder
		} else {
			state.postflopActionOrder
		}
		val activeSeats = state.actionOrder.toSet()
		if (activeSeats.isEmpty()) return null

		val currentIdx = fullOrder.indexOf(currentSeat)
		if (currentIdx < 0) return null

		if (lastAggressor == null) {
			val streetActions = state.streets.getActions(state.currentStreet)
			val actedSeats = streetActions.map { it.playerSeat }.toSet()
			if (activeSeats.all { it in actedSeats }) {
				return null
			}
		}

		for (i in 1..fullOrder.size) {
			val candidateIdx = (currentIdx + i) % fullOrder.size
			val candidate = fullOrder[candidateIdx]
			if (candidate !in activeSeats) continue
			if (lastAggressor != null && candidate == lastAggressor) return null
			return candidate
		}

		return null
	}

	private fun updateRecording(block: RecordHandState.Recording.() -> RecordHandState.Recording) {
		_state.update { state ->
			if (state is RecordHandState.Recording) state.block() else state
		}
	}

	private fun generateId(): String {
		val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
		return (1..20).map { chars.random() }.joinToString("")
	}
}

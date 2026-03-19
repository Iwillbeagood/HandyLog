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
		// 히어로 카드 재선택 시 기존 히어로 카드는 사용 가능하도록 제외
		val usedCards = current.selectedCards - current.heroCards.toSet()
		_modalEffect.value = RecordHandModalEffect.ShowCardSelector(
			target = CardSelectorTarget.HeroCard(maxCards = 2),
			selectedCards = usedCards,
		)
	}

	fun selectBoardCard(street: Street) {
		val current = _state.value as? RecordHandState.Recording ?: return
		val maxCards = when (street) {
			Street.FLOP -> 3
			else -> 1
		}
		val existingCards = current.streets.getCards(street).toSet()
		val usedCards = current.selectedCards - existingCards
		_modalEffect.value = RecordHandModalEffect.ShowCardSelector(
			target = CardSelectorTarget.BoardCard(street, maxCards = maxCards),
			selectedCards = usedCards,
		)
	}

	fun selectSingleBoardCard(street: Street, cardIndex: Int) {
		val current = _state.value as? RecordHandState.Recording ?: return
		val existingCard = current.streets.getCards(street).getOrNull(cardIndex)
		val usedCards = if (existingCard != null) {
			current.selectedCards - setOf(existingCard)
		} else {
			current.selectedCards
		}
		_modalEffect.value = RecordHandModalEffect.ShowCardSelector(
			target = CardSelectorTarget.SingleBoardCard(street, cardIndex),
			selectedCards = usedCards,
		)
	}

	fun onCardsSelected(cards: List<Card>) {
		val current = _state.value as? RecordHandState.Recording ?: return
		val modal = _modalEffect.value as? RecordHandModalEffect.ShowCardSelector ?: return

		when (modal.target) {
			is CardSelectorTarget.HeroCard -> {
				val newHeroHand = if (cards.size >= 2) {
					HeroHand(card1 = cards[0], card2 = cards[1])
				} else {
					null
				}
				updateRecording { copy(heroHand = newHeroHand) }
			}

			is CardSelectorTarget.BoardCard -> {
				val street = modal.target.street
				updateRecording { copy(streets = streets.setCards(street, cards)) }
			}

			is CardSelectorTarget.SingleBoardCard -> {
				val street = modal.target.street
				val index = modal.target.cardIndex
				val card = cards.firstOrNull() ?: return
				val existingCards = current.streets.getCards(street).toMutableList()
				if (index < existingCards.size) {
					existingCards[index] = card
				}
				updateRecording { copy(streets = streets.setCards(street, existingCards)) }
			}

			is CardSelectorTarget.ShowdownCard -> {
				val seat = modal.target.seat
				val newHand = if (cards.size >= 2) HeroHand(cards[0], cards[1]) else null
				updateRecording { copy(showdown = showdown.set(seat, newHand)) }
			}
		}
		dismissModal()

		val updated = _state.value as? RecordHandState.Recording ?: return

		// 보드 카드 선택 후, 액션할 플레이어가 없으면 자동으로 다음 스트릿
		if (modal.target is CardSelectorTarget.BoardCard &&
			updated.streets.isBoardReady(updated.currentStreet) &&
			updated.actionOrder.isEmpty()
		) {
			if (updated.remainingSeats.size <= 1) {
				goToShowdown()
			} else {
				nextStep()
			}
		}

		// 쇼다운 카드 선택 완료 시 승자에게 팟 분배
		if (modal.target is CardSelectorTarget.ShowdownCard && updated.isShowdownComplete) {
			distributeWinnings()
		}
	}

	private fun distributeWinnings() {
		val current = _state.value as? RecordHandState.Recording ?: return
		val results = current.showdownResults
		if (results.isEmpty()) return

		val winners = results.filter { it.isWinner }
		if (winners.isEmpty()) return

		// 모든 플레이어의 총 투입 금액 계산 (폴드 포함)
		val investments = mutableMapOf<Int, Double>()
		val count = current.table?.playerCount ?: return
		for (seat in 1..count) {
			val player = current.players[seat] ?: continue
			val invested = player.initialStack - player.stack
			if (invested > 0) {
				investments[seat] = invested
			}
		}

		// 사이드팟 계산: 투입 금액 기준 정렬
		val sortedInvestments = investments.entries.sortedBy { it.value }
		var previousLevel = 0.0
		var remaining = investments.toMutableMap()
		val winnings = mutableMapOf<Int, Double>()

		for ((seat, invested) in sortedInvestments) {
			val level = invested
			val diff = level - previousLevel
			if (diff <= 0) continue

			// 이 레벨까지 참여한 플레이어 수
			val eligible = remaining.filter { it.value >= level }
			val potForLevel = diff * eligible.size

			// 이 레벨에서 이긴 사람에게 분배
			val levelWinners = winners.filter { it.seat in eligible }
			if (levelWinners.isNotEmpty()) {
				val share = potForLevel / levelWinners.size
				levelWinners.forEach { winner ->
					winnings[winner.seat] = (winnings[winner.seat] ?: 0.0) + share
				}
			}

			previousLevel = level
		}

		updateRecording {
			var updated = this
			winnings.forEach { (seat, amount) ->
				updated = updated.copy(
					players = updated.players.update(seat) {
						copy(stack = stack + amount)
					},
				)
			}
			updated
		}
	}

	fun dismissModal() {
		_modalEffect.value = RecordHandModalEffect.Idle
	}

	fun showTableEdit() {
		_modalEffect.value = RecordHandModalEffect.ShowTableEdit
	}

	// --- Setup ---

	fun updateHeroStack(amount: String) {
		val current = _state.value as? RecordHandState.Recording ?: return
		val heroSeat = current.table?.heroSeat ?: return
		updatePlayerStack(heroSeat, amount)
	}

	fun selectShowdownCard(seat: Int) {
		val current = _state.value as? RecordHandState.Recording ?: return
		val existingCards = current.showdown[seat]?.cards?.toSet() ?: emptySet()
		val usedCards = current.selectedCards - existingCards
		_modalEffect.value = RecordHandModalEffect.ShowCardSelector(
			target = CardSelectorTarget.ShowdownCard(seat),
			selectedCards = usedCards,
		)
	}

	fun setShowdownUnknown(seat: Int) {
		updateRecording {
			copy(showdown = showdown.setUnknown(seat))
		}
		// 쇼다운 완료 체크
		val updated = _state.value as? RecordHandState.Recording ?: return
		if (updated.isShowdownComplete) {
			distributeWinnings()
		}
	}

	fun updateTable(
		date: String,
		location: String?,
		gameType: com.hand.log.domain.model.GameType,
		startingStack: Double,
		blinds: Blinds?,
		playerCount: Int,
		heroSeat: Int,
	) {
		val current = _state.value as? RecordHandState.Recording ?: return
		val updatedTable = current.table?.copy(
			date = kotlinx.datetime.LocalDate.parse(date),
			location = location,
			gameType = gameType,
			startingStack = startingStack,
			blinds = blinds,
			playerCount = playerCount,
			heroSeat = heroSeat,
		)
		// 플레이어 수가 변경되었으면 RecordPlayers도 재생성
		val updatedPlayers = if (playerCount != current.table?.playerCount) {
			RecordPlayers.create(
				playerCount = playerCount,
				defaultStack = startingStack,
			)
		} else {
			current.players
		}
		updateRecording {
			copy(
				table = updatedTable,
				blinds = blinds,
				players = updatedPlayers,
			)
		}
		// 테이블 저장
		updatedTable?.let { table ->
			viewModelScope.launch {
				pokerTableRepository.saveTable(table)
			}
		}
	}

	fun updateButtonSeat(seat: Int) {
		updateRecording { copy(buttonSeat = seat) }
	}

	fun updateBlinds(sb: String, bb: String) {
		val sbValue = sb.toDoubleOrNull() ?: 0.0
		val bbValue = bb.toDoubleOrNull() ?: 0.0
		updateRecording {
			copy(
				blinds = Blinds(
					sb = sbValue,
					bb = bbValue,
					straddle = blinds?.straddle,
					isBigBlindAnte = blinds?.isBigBlindAnte ?: false,
				),
			)
		}
	}

	fun updatePlayerStack(seat: Int, amount: String) {
		val stack = amount.toDoubleOrNull() ?: 0.0
		updateRecording {
			copy(players = players.update(seat) { copy(stack = stack, initialStack = stack) })
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
		updateRecording {
			val seat = currentActionSeat ?: return@updateRecording copy(currentActionAmount = amount)
			val player = players[seat] ?: return@updateRecording copy(currentActionAmount = amount)
			val effectiveStack = player.stack + player.currentBet
			val parsed = amount.toDoubleOrNull() ?: 0.0
			// 스택 초과 시 입력 무시
			if (parsed > effectiveStack) return@updateRecording this
			copy(currentActionAmount = amount)
		}
	}

	fun confirmAction() {
		val current = _state.value as? RecordHandState.Recording ?: return
		val seat = current.currentActionSeat ?: return
		val type = current.currentActionType ?: return
		val player = current.players[seat] ?: return

		// 액션 타입 & 금액 결정
		var resolvedType = type
		val alreadyInvested = player.currentBet

		// amount: "이 스트릿에서의 총 베팅 금액"
		var amount: Double? = null
		val effectiveStackForCalc = player.stack + alreadyInvested // 스트릿 시작 시 스택

		when (type) {
			ActionType.FOLD, ActionType.CHECK -> {
				amount = null
			}
			ActionType.CALL -> {
				val streetActions = current.streets.getActions(current.currentStreet)
				val maxBet = streetActions.maxOfOrNull { it.amount ?: 0.0 } ?: current.blinds?.bb ?: 0.0
				if (maxBet >= effectiveStackForCalc) {
					resolvedType = ActionType.ALL_IN
					amount = effectiveStackForCalc
				} else {
					amount = maxBet
				}
			}
			ActionType.ALL_IN -> {
				amount = effectiveStackForCalc
			}
			ActionType.BET, ActionType.RAISE -> {
				val targetTotal = current.currentActionAmount.toDoubleOrNull() ?: 0.0
				if (targetTotal < current.minRaiseAmount) return
				if (targetTotal >= effectiveStackForCalc) {
					resolvedType = ActionType.ALL_IN
					amount = effectiveStackForCalc
				} else {
					amount = targetTotal
				}
			}
		}

		// 벳 레벨 계산: 올인이 현재 최대 베팅보다 클 때만 레이즈로 간주
		val streetActions = current.streets.getActions(current.currentStreet)
		val currentMaxBet = streetActions.maxOfOrNull { it.amount ?: 0.0 } ?: 0.0
		val isAggressive = resolvedType == ActionType.BET ||
			resolvedType == ActionType.RAISE ||
			(resolvedType == ActionType.ALL_IN && (amount ?: 0.0) > currentMaxBet)
		val actionBetLevel = if (isAggressive) current.currentBetLevel + 1 else 0

		// 스택 계산: 이전 베팅을 복원 후 새 베팅 차감
		// effectiveStack = 현재 스택 + 이미 투입한 금액 (이 스트릿 시작 시 스택)
		val effectiveStack = player.stack + alreadyInvested
		val newStack = if (amount != null) {
			(effectiveStack - amount).coerceAtLeast(0.0)
		} else {
			player.stack // 폴드/체크는 스택 변동 없음
		}

		// Streets & Players 업데이트
		val action = Action(
			playerSeat = seat,
			type = resolvedType,
			amount = amount,
			stackBefore = effectiveStack,
			stackAfter = newStack,
			betLevel = actionBetLevel,
		)
		val updatedStreets = current.streets.addAction(current.currentStreet, action)
		val newStatus = when (resolvedType) {
			ActionType.FOLD -> PlayerStatus.FOLDED
			ActionType.ALL_IN -> PlayerStatus.ALL_IN
			else -> player.status
		}
		val updatedPlayers = current.players.update(seat) {
			copy(
				stack = newStack,
				status = newStatus,
				currentBet = amount ?: currentBet,
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
			val afterAction = _state.value as? RecordHandState.Recording
			val remaining = afterAction?.remainingSeats?.size ?: 0
			if (remaining <= 1) {
				// 1명만 남으면 핸드 즉시 종료 → 쇼다운으로
				goToShowdown()
			} else {
				nextStep()
			}
		}
	}

	private fun goToShowdown() {
		updateRecording {
			copy(
				currentStep = RecordStep.SHOWDOWN,
				currentActionSeat = null,
				currentActionType = null,
				currentActionAmount = "",
			)
		}
	}

	fun removeLastAction() {
		val current = _state.value as? RecordHandState.Recording ?: return
		val streetActions = current.streets.getActions(current.currentStreet)
		if (streetActions.isEmpty()) return

		val removedAction = streetActions.last()
		val seat = removedAction.playerSeat

		// 이전 액션의 stackAfter로 복원, 없으면 stackBefore(스트릿 시작 시 스택)
		val previousAction = streetActions.dropLast(1).lastOrNull { it.playerSeat == seat }
		val restoredStack = previousAction?.stackAfter ?: removedAction.stackBefore ?: current.getPlayerStack(seat)

		val updatedPlayers = current.players.update(seat) {
			copy(
				stack = restoredStack,
				status = if (removedAction.type == ActionType.FOLD || removedAction.type == ActionType.ALL_IN) {
					PlayerStatus.ACTIVE
				} else {
					status
				},
				currentBet = previousAction?.amount ?: 0.0,
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
		// 남은 플레이어가 1명 이하면 바로 쇼다운으로
		val beforeStep = _state.value as? RecordHandState.Recording
		if (beforeStep != null && beforeStep.remainingSeats.size <= 1) {
			goToShowdown()
			return
		}

		updateRecording {
			val steps = RecordStep.entries
			val currentIndex = steps.indexOf(currentStep)
			if (currentIndex < steps.lastIndex) {
				val nextStep = steps[currentIndex + 1]

				// 쇼다운: 스트릿 전환 없이 상태만 변경
				if (nextStep == RecordStep.SHOWDOWN) {
					return@updateRecording copy(
						currentStep = RecordStep.SHOWDOWN,
						currentActionSeat = null,
						currentActionType = null,
						currentActionAmount = "",
					)
				}

				val initialAggressor: Int? = null

				// currentStep 변경 → currentStreet이 자동으로 파생됨
				val stepped = copy(currentStep = nextStep)
				val nextStreet = stepped.currentStreet

				val updated = stepped.copy(
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

		// 포스트플랍 진입 시 자동으로 보드 카드 선택 시트 표시
		val afterStep = _state.value as? RecordHandState.Recording ?: return
		if (afterStep.currentStreet != Street.PREFLOP && !afterStep.streets.isBoardReady(afterStep.currentStreet)) {
			selectBoardCard(afterStep.currentStreet)
		}
	}

	fun previousStep() {
		updateRecording {
			if (currentStep == RecordStep.SETUP) return@updateRecording this

			// 현재 스텝보다 이전 스텝 중 액션이 있는 마지막 스트릿을 찾음
			val lastActiveStep = findLastActiveStep()

			if (lastActiveStep != null) {
				val prevUpdated = copy(
					currentStep = lastActiveStep,
					currentActionType = null,
					currentActionAmount = "",
					lastAggressorSeat = null,
				)
				val lastAction = prevUpdated.streets.getActions(prevUpdated.currentStreet).lastOrNull()
				prevUpdated.copy(currentActionSeat = lastAction?.playerSeat)
			} else {
				// 이전에 액션이 없으면 SETUP으로
				copy(
					currentStep = RecordStep.SETUP,
					currentActionType = null,
					currentActionAmount = "",
				)
			}
		}
	}

	/** 현재 스텝보다 이전 스텝 중 액션이 있는 마지막 스텝을 찾음 */
	private fun RecordHandState.Recording.findLastActiveStep(): RecordStep? {
		val streetSteps = listOf(
			RecordStep.RIVER to Street.RIVER,
			RecordStep.TURN to Street.TURN,
			RecordStep.FLOP to Street.FLOP,
			RecordStep.PREFLOP to Street.PREFLOP,
		)
		for ((step, street) in streetSteps) {
			if (step.ordinal >= currentStep.ordinal) continue
			if (streets.getActions(street).isNotEmpty()) return step
		}
		return RecordStep.SETUP
	}

	fun toggleBbUnit() {
		updateRecording { copy(useBbUnit = !useBbUnit) }
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
					heroSeat = current.table?.heroSeat ?: 0,
					heroStack = current.heroStack,
					buttonSeat = current.buttonSeat,
					streets = current.streets.toHandStreets(),
					showdown = current.showdown.toShowdownEntries(),
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

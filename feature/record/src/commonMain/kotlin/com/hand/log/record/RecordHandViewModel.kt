package com.hand.log.record

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hand.log.domain.model.Action
import com.hand.log.domain.model.ActionType
import com.hand.log.domain.model.HandRanking
import com.hand.log.domain.model.ShowdownOutcome
import com.hand.log.domain.model.ShowdownResult
import com.hand.log.utils.poker.HandEvaluator
import com.hand.log.domain.model.Blinds
import com.hand.log.domain.model.Card
import com.hand.log.domain.model.GameType.Cash
import com.hand.log.domain.model.GameType.Tournament
import com.hand.log.domain.model.HandPlayer
import com.hand.log.domain.model.HandRecord
import com.hand.log.domain.model.HandStreets
import com.hand.log.ui.stringRes
import com.hand.log.ui.resultStringRes
import org.jetbrains.compose.resources.getString
import com.hand.log.domain.model.PocketCards
import com.hand.log.domain.model.PokerTable
import com.hand.log.domain.model.Street
import com.hand.log.domain.repository.AppSettingsRepository
import com.hand.log.domain.usecase.LoadRecordDataUseCase
import com.hand.log.domain.usecase.SaveHandAndUpdateStacksUseCase
import com.hand.log.record.contract.ActionPresets
import com.hand.log.record.contract.CardSelectorTarget
import com.hand.log.record.contract.RecordHandEffect
import com.hand.log.record.contract.RecordHandModalEffect
import com.hand.log.record.contract.RecordHandState
import com.hand.log.record.contract.RecordStep
import com.hand.log.record.contract.ResolvedShowdown
import com.hand.log.record.model.PlayerStatus
import com.hand.log.record.model.RecordPlayers
import com.hand.log.platform.etc.Logger
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class RecordHandViewModel(
	private val tableId: String,
	private val appSettingsRepository: AppSettingsRepository,
	private val loadRecordData: LoadRecordDataUseCase,
	private val saveHandAndUpdateStacks: SaveHandAndUpdateStacksUseCase,
) : ViewModel() {

	private val _state = MutableStateFlow<RecordHandState>(RecordHandState.Loading)
	val state: StateFlow<RecordHandState> = _state.onStart {
		loadTable()
	}.stateIn(
		scope = viewModelScope,
		started = SharingStarted.Eagerly,
		initialValue = RecordHandState.Loading,
	)

	private val _modalEffect = MutableStateFlow<RecordHandModalEffect>(RecordHandModalEffect.Idle)
	val modalEffect: StateFlow<RecordHandModalEffect> get() = _modalEffect

	private val _effect = MutableSharedFlow<RecordHandEffect>()
	val effect: SharedFlow<RecordHandEffect> get() = _effect.asSharedFlow()

	private val recording: RecordHandState.Recording?
		get() = _state.value as? RecordHandState.Recording

	private fun loadTable() {
		viewModelScope.launch {
			val data = loadRecordData(tableId)
			val table = data.table

			_state.update {
				val occupiedSeats = buildSet {
					table?.players?.forEach { add(it.seat) }
					table?.heroSeat?.let { add(it) }
				}.sorted()
				RecordHandState.Recording(
					tableId = tableId,
					table = table,
					players = RecordPlayers.create(
						playerCount = table?.maxPlayers ?: 0,
						defaultStack = 0.0,
					),
					buttonSeat = occupiedSeats.firstOrNull() ?: 1,
					blinds = (table?.gameType as? Cash)?.let {
						Blinds(sb = it.sb, bb = it.bb, straddle = it.straddle)
					},
					actionPresets = ActionPresets(
						preflopPresets = data.preflopPresets,
						postflopPresets = data.postflopPresets,
					),
				)
			}
			selectHeroCard()
		}
	}

	// ──────────────────────────────────────────────
	// Card Selection
	// ──────────────────────────────────────────────

	fun selectHeroCard() {
		val current = recording ?: return
		val heroCards = current.heroHand?.let { listOf(it.card1, it.card2) } ?: emptyList()
		showCardSelector(
			target = CardSelectorTarget.HeroCard(maxCards = 2),
			usedCards = current.selectedCards - heroCards.toSet(),
			initialCards = heroCards,
		)
	}

	fun selectAllBoardCards() {
		val current = recording ?: return
		val existingBoardCards = current.streets.boardCards
		showCardSelector(
			target = CardSelectorTarget.AllBoardCards(),
			usedCards = current.selectedCards - existingBoardCards.toSet(),
			initialCards = existingBoardCards,
		)
	}

	fun selectBoardCard(street: Street) {
		val current = recording ?: return
		val existingCards = current.streets.getCards(street)
		showCardSelector(
			target = CardSelectorTarget.BoardCard(street, maxCards = if (street == Street.FLOP) 3 else 1),
			usedCards = current.selectedCards - existingCards.toSet(),
			initialCards = existingCards,
		)
	}

	fun selectSingleBoardCard(street: Street, cardIndex: Int) {
		val current = recording ?: return
		val existingCard = current.streets.getCards(street).getOrNull(cardIndex)
		val existingCards = listOfNotNull(existingCard)
		showCardSelector(
			target = CardSelectorTarget.SingleBoardCard(street, cardIndex),
			usedCards = current.selectedCards - existingCards.toSet(),
			initialCards = existingCards,
		)
	}

	fun selectShowdownCard(seat: Int) {
		val current = recording ?: return
		val existingCards = current.players[seat]?.cards?.let { listOf(it.card1, it.card2) } ?: emptyList()
		val isAllIn = seat in current.players.allInSeats
		showCardSelector(
			target = CardSelectorTarget.ShowdownCard(seat, positionName = current.positionName(seat)),
			usedCards = current.selectedCards - existingCards.toSet(),
			initialCards = existingCards,
			allowUnknown = !isAllIn,
		)
	}

	private fun showCardSelector(
		target: CardSelectorTarget,
		usedCards: Set<Card>,
		initialCards: List<Card> = emptyList(),
		allowUnknown: Boolean = true,
	) {
		val isBoardSelector = target is CardSelectorTarget.AllBoardCards ||
			target is CardSelectorTarget.BoardCard ||
			target is CardSelectorTarget.SingleBoardCard
		_modalEffect.value = RecordHandModalEffect.ShowCardSelector(
			target = target,
			selectedCards = usedCards,
			initialCards = initialCards,
			allowUnknown = allowUnknown,
			heroHand = if (isBoardSelector) recording?.heroHand else null,
		)
	}

	fun onCardsSelected(cards: List<Card>) {
		val current = recording ?: return
		val modal = _modalEffect.value as? RecordHandModalEffect.ShowCardSelector ?: return

		when (modal.target) {
			is CardSelectorTarget.HeroCard -> {
				val newHeroHand = if (cards.size >= 2) PocketCards(cards[0], cards[1]) else null
				val heroSeat = current.table?.heroSeat ?: return
				updateRecording {
					copy(players = players.update(heroSeat) { copy(cards = newHeroHand) })
				}
				if (newHeroHand != null) {
					dismissModal()
					selectAllBoardCards()
					return
				}
			}

			is CardSelectorTarget.AllBoardCards -> {
				val flopCards = cards.take(3)
				val turnCard = cards.getOrNull(3)?.let { listOf(it) } ?: emptyList()
				val riverCard = cards.getOrNull(4)?.let { listOf(it) } ?: emptyList()
				updateRecording {
					var updated = streets
					if (flopCards.isNotEmpty()) updated = updated.setCards(Street.FLOP, flopCards)
					if (turnCard.isNotEmpty()) updated = updated.setCards(Street.TURN, turnCard)
					if (riverCard.isNotEmpty()) updated = updated.setCards(Street.RIVER, riverCard)
					copy(streets = updated)
				}
			}

			is CardSelectorTarget.BoardCard -> {
				updateRecording { copy(streets = streets.setCards(modal.target.street, cards)) }
			}

			is CardSelectorTarget.SingleBoardCard -> {
				val street = modal.target.street
				val index = modal.target.cardIndex
				val card = cards.firstOrNull() ?: return
				val existingCards = current.streets.getCards(street).toMutableList()
				if (index < existingCards.size) existingCards[index] = card
				updateRecording { copy(streets = streets.setCards(street, existingCards)) }
			}

			is CardSelectorTarget.ShowdownCard -> {
				val newHand = if (cards.size >= 2) PocketCards(cards[0], cards[1]) else null
				val seat = modal.target.seat
				updateRecording {
					copy(
						players = players.update(seat) {
							copy(cards = newHand, isCardsUnknown = newHand == null && isCardsUnknown)
						},
					)
				}
			}
		}
		dismissModal()

		val updated = recording ?: return

		// 보드 카드 선택 후, 액션할 플레이어가 없으면 자동으로 다음 스트릿
		if (modal.target is CardSelectorTarget.BoardCard &&
			updated.streets.isBoardReady(updated.currentStreet) &&
			updated.actionOrder.isEmpty()
		) {
			if (updated.remainingSeats.size <= 1 || updated.isAllInShowdown) goToShowdown() else nextStep()
		}

		// 쇼다운 카드 선택 완료 시 승자에게 팟 분배
		if (modal.target is CardSelectorTarget.ShowdownCard && updated.isShowdownComplete) {
			distributeWinnings()
		}
	}

	fun setShowdownUnknown(seat: Int) {
		updateRecording {
			copy(players = players.update(seat) { copy(isCardsUnknown = true, cards = null) })
		}
		val updated = recording ?: return
		if (updated.isShowdownComplete) distributeWinnings()
	}

	fun dismissModal() {
		_modalEffect.value = RecordHandModalEffect.Idle
	}

	fun showTableEdit() {
		val current = recording ?: return
		val table = current.table ?: return
		_modalEffect.value = RecordHandModalEffect.ShowTableEdit(table)
	}

	// ──────────────────────────────────────────────
	// Setup
	// ──────────────────────────────────────────────

	fun updateHeroStack(amount: String) {
		val current = recording ?: return
		val heroSeat = current.table?.heroSeat ?: return
		updatePlayerStack(heroSeat, amount)
	}

	fun onTableSaved(table: PokerTable) {
		val current = recording ?: return
		val updatedPlayers = if (table.maxPlayers != current.table?.maxPlayers) {
			RecordPlayers.create(playerCount = table.maxPlayers, defaultStack = 0.0)
		} else {
			current.players
		}
		updateRecording {
			val newBlinds = (table.gameType as? Cash)?.let {
				Blinds(sb = it.sb, bb = it.bb, straddle = it.straddle)
			}
			copy(table = table, blinds = newBlinds, players = updatedPlayers)
		}
		dismissModal()
	}

	fun updateButtonSeat(seat: Int) {
		updateRecording { copy(buttonSeat = seat) }
	}

	fun updateBb(bb: String) {
		val newBb = bb.toLongOrNull() ?: 0L
		val newSb = newBb / 2
		updateBlinds(newSb.toString(), bb)
	}

	fun updateSb(sb: String) {
		updateBlinds(sb, recording?.bbText.orEmpty())
	}

	fun updateBlinds(sb: String, bb: String) {
		updateRecording {
			val isBba = blinds?.isBigBlindAnte
				?: (table?.gameType as? Tournament)?.isBigBlindAnte
				?: false
			copy(
				blinds = Blinds(
					sb = sb.toDoubleOrNull() ?: 0.0,
					bb = bb.toDoubleOrNull() ?: 0.0,
					straddle = blinds?.straddle,
					isBigBlindAnte = isBba,
				),
			)
		}
	}

	fun updatePlayerStack(seat: Int, amount: String) {
		val totalStack = amount.toDoubleOrNull()
		updateRecording {
			if (totalStack == null) {
				copy(players = players.update(seat) { copy(stack = 0.0, initialStack = null) })
			} else {
				val blindCost = getBlindCost(seat)
				val effectiveStack = (totalStack - blindCost).coerceAtLeast(0.0)
				copy(players = players.update(seat) { copy(stack = effectiveStack, initialStack = totalStack) })
			}
		}
	}

	// ──────────────────────────────────────────────
	// Action
	// ──────────────────────────────────────────────

	fun selectActionSeat(seat: Int) {
		val current = recording ?: return

		if (current.currentStreet != Street.PREFLOP) {
			updateRecording { copy(currentActionSeat = seat) }
			return
		}

		// 이미 폴드/올인한 좌석은 무시
		if (seat in current.players.inactiveSeats) return

		// 이미 이번 스트릿에서 액션을 완료한 좌석은 무시
		val actedSeats = current.streets.getActions(Street.PREFLOP).map { it.playerSeat }.toSet()
		if (seat in actedSeats) return

		val currentSeat = current.currentActionSeat
		// 현재 좌석과 같으면 무시
		if (seat == currentSeat) return

		val actionOrder = current.preflopActionOrder
		val activeSeats = actionOrder.filter { it !in current.players.inactiveSeats && it !in actedSeats }

		// 선택한 좌석이 활성 좌석이 아니면 무시
		if (seat !in activeSeats) return

		val seatsToFold = if (currentSeat == null) {
			// 오프너 선택: 처음부터 선택한 좌석 직전까지
			actionOrder.takeWhile { it != seat }.filter { it in activeSeats }
		} else {
			// 액션 진행 중: 현재 좌석(포함) ~ 선택한 좌석 직전까지 (앞으로만)
			val currentIdx = activeSeats.indexOf(currentSeat)
			val targetIdx = activeSeats.indexOf(seat)
			if (targetIdx <= currentIdx) return // 이미 지나간 좌석으로는 건너뛸 수 없음
			activeSeats.subList(currentIdx, targetIdx)
		}

		updateRecording {
			var updated = this
			seatsToFold.forEach { foldSeat ->
				val player = updated.players[foldSeat] ?: return@forEach
				val effectiveStack = player.stack + player.currentBet
				val action = Action(
					playerSeat = foldSeat,
					type = ActionType.FOLD,
					stackBefore = effectiveStack,
					stackAfter = player.stack,
				)
				updated = updated.copy(
					streets = updated.streets.addAction(Street.PREFLOP, action),
					players = updated.players.update(foldSeat) {
						copy(status = PlayerStatus.FOLDED)
					},
				)
			}
			updated.copy(
				currentActionSeat = seat,
				currentActionType = null,
				currentActionAmount = "",
				currentActionChipAmount = null,
			)
		}
	}

	fun selectActionType(type: ActionType) {
		updateRecording { copy(currentActionType = type) }
		val requiresAmount = type == ActionType.BET || type == ActionType.RAISE
		if (!requiresAmount) confirmAction()
	}

	fun updateActionAmount(amount: String) {
		updateRecording {
			val seat = currentActionSeat
				?: return@updateRecording copy(currentActionAmount = amount, currentActionChipAmount = null)
			val player = players[seat]
				?: return@updateRecording copy(currentActionAmount = amount, currentActionChipAmount = null)
			val effectiveStack = player.stack + player.currentBet
			if (effectiveStack > 0) {
				val chipAmount = parseInputToChip(amount)
				if (chipAmount > effectiveStack) return@updateRecording this
			}
			copy(currentActionAmount = amount, currentActionChipAmount = null)
		}
	}

	fun confirmAction() {
		val current = recording ?: return
		val seat = current.currentActionSeat ?: return
		val type = current.currentActionType ?: return
		val player = current.players[seat] ?: return

		val effectiveStack = player.stack + player.currentBet
		val streetActions = current.streets.getActions(current.currentStreet)
		val streetMaxBet = streetActions.maxOfOrNull { it.amount ?: 0.0 } ?: 0.0

		val isUnlimited = player.initialStack == null

		// 1) 액션 타입 & 금액 결정
		val (resolvedType, amount) = resolveAction(
			type = type,
			isUnlimited = isUnlimited,
			effectiveStack = effectiveStack,
			streetMaxBet = if (streetMaxBet > 0.0) streetMaxBet else current.blinds?.bb ?: 0.0,
			raiseTarget = current.parseInputToChip(current.currentActionAmount),
			minRaise = current.minRaiseAmount,
		) ?: return

		// 2) 벳 레벨: 올인이 현재 최대 베팅보다 클 때만 레이즈로 간주
		val isAggressive = resolvedType == ActionType.BET ||
			resolvedType == ActionType.RAISE ||
			(resolvedType == ActionType.ALL_IN && (amount ?: 0.0) > streetMaxBet)
		val betLevel = if (isAggressive) current.currentBetLevel + 1 else 0

		// 3) 스택 계산
		val newStack = if (amount != null) {
			(effectiveStack - amount).coerceAtLeast(0.0)
		} else {
			player.stack
		}

		// 4) Streets & Players 업데이트
		val tablePlayer = current.table?.players?.find { it.seat == seat }
		val action = Action(
			playerSeat = seat,
			type = resolvedType,
			amount = amount,
			stackBefore = effectiveStack,
			stackAfter = newStack,
			betLevel = betLevel,
			playerName = tablePlayer?.name,
			savedPlayerId = tablePlayer?.savedPlayerId,
		)
		val updatedStreets = current.streets.addAction(current.currentStreet, action)
		val updatedPlayers = current.players.update(seat) {
			copy(
				stack = newStack,
				status = when (resolvedType) {
					ActionType.FOLD -> PlayerStatus.FOLDED
					ActionType.ALL_IN -> PlayerStatus.ALL_IN
					else -> status
				},
				currentBet = amount ?: currentBet,
			)
		}

		// 5) Aggressor 갱신 (자신의 이전 베팅 제외하고 판단)
		val maxBetExcludingSelf = streetActions
			.filter { it.playerSeat != seat }
			.maxOfOrNull { it.amount ?: 0.0 } ?: 0.0
		val isNewAggressor = resolvedType == ActionType.BET ||
			resolvedType == ActionType.RAISE ||
			(resolvedType == ActionType.ALL_IN && (amount ?: 0.0) > maxBetExcludingSelf)
		val newAggressor = if (isNewAggressor) seat else current.lastAggressorSeat

		// 6) 다음 좌석 결정
		val nextState = current.copy(streets = updatedStreets, players = updatedPlayers)
		val nextSeat = getNextActionSeat(nextState, seat, newAggressor)

		updateRecording {
			copy(
				streets = updatedStreets,
				players = updatedPlayers,
				currentActionSeat = nextSeat,
				currentActionType = null,
				currentActionAmount = "",
				currentActionChipAmount = null,
				lastAggressorSeat = newAggressor,
			)
		}

		// 7) 라운드 종료 시 다음 단계로
		if (nextSeat == null) advanceAfterRoundEnd()
	}

	/**
	 * 사용자가 선택한 액션 타입을 실제 타입과 금액으로 변환.
	 * BET/RAISE에서 minRaise 미충족 시 null 반환 (액션 취소).
	 */
	private fun resolveAction(
		type: ActionType,
		isUnlimited: Boolean,
		effectiveStack: Double,
		streetMaxBet: Double,
		raiseTarget: Double,
		minRaise: Double,
	): Pair<ActionType, Double?>? {
		return when (type) {
			ActionType.FOLD, ActionType.CHECK -> type to null

			ActionType.CALL -> if (!isUnlimited && streetMaxBet >= effectiveStack) {
				ActionType.ALL_IN to effectiveStack
			} else {
				ActionType.CALL to streetMaxBet
			}

			ActionType.ALL_IN -> if (isUnlimited) {
				ActionType.ALL_IN to streetMaxBet
			} else {
				ActionType.ALL_IN to effectiveStack
			}

			ActionType.BET, ActionType.RAISE -> when {
				raiseTarget < minRaise -> null
				!isUnlimited && raiseTarget >= effectiveStack -> ActionType.ALL_IN to effectiveStack
				else -> type to raiseTarget
			}
		}
	}

	/** 라운드 종료 후 다음 단계 진행 */
	private fun advanceAfterRoundEnd() {
		val current = recording ?: return
		if (current.remainingSeats.size <= 1 || current.isAllInShowdown) goToShowdown() else nextStep()
	}

	fun removeLastAction() {
		val current = recording ?: return
		val streetActions = current.streets.getActions(current.currentStreet)
		if (streetActions.isEmpty()) return

		val removedAction = streetActions.last()
		val seat = removedAction.playerSeat

		val previousAction = streetActions.dropLast(1).lastOrNull { it.playerSeat == seat }
		val restoredStack = previousAction?.stackAfter
			?: removedAction.stackBefore
			?: current.getPlayerStack(seat)
			?: 0.0

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

	// ──────────────────────────────────────────────
	// Navigation
	// ──────────────────────────────────────────────

	fun nextStep() {
		val beforeStep = recording
		if (beforeStep != null && beforeStep.remainingSeats.size <= 1) {
			goToShowdown()
			return
		}

		updateRecording {
			val steps = RecordStep.entries
			val currentIndex = steps.indexOf(currentStep)
			if (currentIndex >= steps.lastIndex) return@updateRecording this

			val nextStep = steps[currentIndex + 1]

			if (nextStep == RecordStep.SHOWDOWN) {
				return@updateRecording copy(
					currentStep = RecordStep.SHOWDOWN,
					currentActionSeat = null,
					currentActionType = null,
					currentActionAmount = "",
					currentActionChipAmount = null,
				)
			}

			val stepped = copy(currentStep = nextStep)
			var updatedPlayers = players.resetCurrentBets()

			// 프리플랍 진입 시 SB/BB 스택 차감
			if (stepped.currentStreet == Street.PREFLOP) {
				val sb = blinds?.sb ?: 0.0
				val bb = blinds?.bb ?: 0.0

				if (sb > 0) {
					updatedPlayers = updatedPlayers.update(stepped.sbSeat) {
						if (initialStack == null) {
							copy(currentBet = sb)
						} else {
							copy(stack = stack - sb, currentBet = sb)
						}
					}
				}
				if (bb > 0) {
					val ante = if (blinds?.isBigBlindAnte == true) bb else 0.0
					updatedPlayers = updatedPlayers.update(stepped.bbSeat) {
						if (initialStack == null) {
							copy(currentBet = bb)
						} else {
							copy(stack = stack - bb - ante, currentBet = bb)
						}
					}
				}
			}

			val updated = stepped.copy(
				streets = streets.ensureStreet(stepped.currentStreet),
				players = updatedPlayers,
				currentActionType = null,
				currentActionAmount = "",
				currentActionChipAmount = null,
				lastAggressorSeat = null,
			)
			// 프리플랍 진입 시에는 오프너 선택을 위해 currentActionSeat을 null로
			val firstSeat = if (stepped.currentStreet == Street.PREFLOP) {
				null
			} else {
				updated.actionOrder.firstOrNull()
			}
			updated.copy(currentActionSeat = firstSeat)
		}

		// 포스트플랍 진입 시 보드 카드 선택 시트 자동 표시
		val afterStep = recording ?: return
		if (afterStep.currentStreet != Street.PREFLOP && !afterStep.streets.isBoardReady(afterStep.currentStreet)) {
			selectBoardCard(afterStep.currentStreet)
		}
	}

	fun previousStep() {
		val current = recording ?: return
		if (current.currentStep == RecordStep.SETUP) return

		val targetStep = current.findLastActiveStep()
		requestStepBack(targetStep, undoLastAction = true)
	}

	fun navigateToStep(step: RecordStep) {
		val current = recording ?: return
		if (step.ordinal >= current.currentStep.ordinal) return
		requestStepBack(step)
	}

	private fun requestStepBack(targetStep: RecordStep, undoLastAction: Boolean = false) {
		viewModelScope.launch {
			val skip = appSettingsRepository.observeSkipStepBackWarning().first()
			if (skip) {
				executeStepBack(targetStep, undoLastAction)
			} else {
				_modalEffect.value = RecordHandModalEffect.ConfirmStepBack(targetStep, undoLastAction)
			}
		}
	}

	fun confirmStepBack(targetStep: RecordStep, skipWarning: Boolean, undoLastAction: Boolean = false) {
		if (skipWarning) {
			viewModelScope.launch {
				appSettingsRepository.setSkipStepBackWarning(true)
			}
		}
		executeStepBack(targetStep, undoLastAction)
		dismissModal()
	}

	private fun executeStepBack(targetStep: RecordStep, undoLastAction: Boolean = false) {
		updateRecording {
			val targetStreet = when (targetStep) {
				RecordStep.SETUP, RecordStep.PREFLOP -> Street.PREFLOP
				RecordStep.FLOP -> Street.FLOP
				RecordStep.TURN -> Street.TURN
				RecordStep.RIVER, RecordStep.SHOWDOWN -> Street.RIVER
			}
			val clearedStreets = if (targetStep == RecordStep.SETUP) {
				// SETUP으로 돌아갈 때는 보드 카드를 보존하고 액션만 제거
				streets.clearActions()
			} else {
				// 이후 스트릿의 액션만 제거하고 미리 입력한 보드 카드는 보존
				streets.clearActionsAfter(targetStreet)
			}

			// 남은 스트릿의 액션들로부터 플레이어 상태 복원
			val restoredPlayers = restorePlayersFromStreets(clearedStreets, targetStreet)

			// 쇼다운 카드/상태 초기화
			var clearedPlayers = restoredPlayers
			for (seat in occupiedSeats) {
				clearedPlayers = clearedPlayers.update(seat) {
					copy(cards = if (seat == table?.heroSeat) cards else null, isCardsUnknown = false)
				}
			}

			val updated = copy(
				currentStep = targetStep,
				streets = clearedStreets,
				players = clearedPlayers,
				currentActionType = null,
				currentActionAmount = "",
				currentActionChipAmount = null,
				lastAggressorSeat = null,
			)
			val lastAction = updated.streets.getActions(updated.currentStreet).lastOrNull()
			updated.copy(currentActionSeat = lastAction?.playerSeat)
		}

		if (undoLastAction) removeLastAction()
	}

	private fun RecordHandState.Recording.restorePlayersFromStreets(
		streets: HandStreets,
		targetStreet: Street,
	): RecordPlayers {
		// 타겟 스트릿까지의 모든 스트릿에서 각 플레이어의 마지막 액션을 찾음
		val streetsUpTo = Street.entries.filter { it.ordinal <= targetStreet.ordinal }
		val lastActionBySeat = mutableMapOf<Int, Action>()
		val lastActionStreetBySeat = mutableMapOf<Int, Street>()
		streetsUpTo.forEach { street ->
			streets.getActions(street).forEach { action ->
				lastActionBySeat[action.playerSeat] = action
				lastActionStreetBySeat[action.playerSeat] = street
			}
		}

		// 타겟 스트릿의 액션 (currentBet 계산용)
		val targetActions = streets.getActions(targetStreet)
		val lastTargetActionBySeat = mutableMapOf<Int, Action>()
		targetActions.forEach { action ->
			lastTargetActionBySeat[action.playerSeat] = action
		}

		var restored = players
		val allSeats = occupiedSeats
		allSeats.forEach { seat ->
			val player = players[seat] ?: return@forEach
			val lastAction = lastActionBySeat[seat]
			val lastActionStreet = lastActionStreetBySeat[seat]
			if (lastAction != null) {
				// 타겟 스트릿의 currentBet: 해당 스트릿에서 액션한 경우만 베팅액 유지
				val currentBet = lastTargetActionBySeat[seat]?.amount ?: 0.0
				restored = restored.update(seat) {
					copy(
						stack = lastAction.stackAfter ?: player.stack,
						currentBet = currentBet,
						status = when (lastAction.type) {
							ActionType.FOLD -> PlayerStatus.FOLDED
							ActionType.ALL_IN -> PlayerStatus.ALL_IN
							else -> PlayerStatus.ACTIVE
						},
					)
				}
			} else {
				// 어떤 스트릿에서도 액션이 없는 플레이어는 초기 상태로
				val blindCost = getBlindCost(seat)
				restored = restored.update(seat) {
					copy(
						stack = (initialStack ?: 0.0) - blindCost,
						currentBet = blindCost,
						status = PlayerStatus.ACTIVE,
					)
				}
			}
		}
		return restored
	}

	private fun goToShowdown() {
		val current = recording ?: return

		if (current.remainingSeats.size <= 1) {
			// 폴드 승리: 1명만 남음 → 자동 결과 처리
			val winnerSeat = current.remainingSeats.firstOrNull()

			updateRecording {
				// 히어로 외 플레이어를 미공개로 마킹
				var updatedPlayers = players
				remainingSeats.forEach { seat ->
					if (seat != table?.heroSeat) {
						updatedPlayers = updatedPlayers.update(seat) {
							copy(isCardsUnknown = true, cards = null)
						}
					}
				}
				if (winnerSeat != null) {
					var totalPot = 0.0
					for (s in occupiedSeats) {
						val player = updatedPlayers[s] ?: continue
						val initial = player.initialStack ?: continue
						totalPot += initial - player.stack
					}
					updatedPlayers = updatedPlayers.update(winnerSeat) {
						copy(stack = stack + totalPot)
					}
				}

				val foldResults = occupiedSeats.map { seat ->
					ShowdownResult(
						seat = seat,
						ranking = HandRanking.WIN_BY_FOLD,
						bestCards = emptyList(),
						outcome = if (seat == winnerSeat) ShowdownOutcome.WIN else ShowdownOutcome.LOSE,
					)
				}

				val updated = copy(
					currentStep = RecordStep.SHOWDOWN,
					currentActionSeat = null,
					currentActionType = null,
					currentActionAmount = "",
					currentActionChipAmount = null,
					players = updatedPlayers,
				)
				updated.copy(
					resolvedShowdown = ResolvedShowdown(
						results = foldResults,
						heroResult = updated.heroResult,
					),
				)
			}
		} else {
			// 일반 쇼다운: 히어로 외 플레이어는 미공개로 초기화
			updateRecording {
				var updatedPlayers = players
				remainingSeats.forEach { seat ->
					if (seat != table?.heroSeat) {
						updatedPlayers = updatedPlayers.update(seat) {
							copy(isCardsUnknown = true, cards = null)
						}
					}
				}

				copy(
					currentStep = RecordStep.SHOWDOWN,
					currentActionSeat = null,
					currentActionType = null,
					currentActionAmount = "",
					currentActionChipAmount = null,
					players = updatedPlayers,
				)
			}

			// 올인 런아웃으로 보드가 아직 다 나오지 않았다면 남은 보드 카드를 먼저 입력받는다
			val updated = recording ?: return
			if (updated.streets.boardCards.size < 5) {
				selectAllBoardCards()
			}
		}
	}

	/** 현재 스텝보다 이전 스텝 중 액션이 있는 마지막 스텝을 찾음 */
	private fun RecordHandState.Recording.findLastActiveStep(): RecordStep {
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
		updateRecording {
			val newUseBb = !useBbUnit
			if (currentActionAmount.isBlank()) {
				return@updateRecording copy(useBbUnit = newUseBb)
			}
			val chipAmount = currentActionChipAmount ?: parseInputToChip(currentActionAmount)
			val newState = copy(useBbUnit = newUseBb)
			val convertedAmount = newState.chipToInput(chipAmount)
			copy(
				useBbUnit = newUseBb,
				currentActionAmount = convertedAmount,
				currentActionChipAmount = chipAmount,
			)
		}
	}

	// ──────────────────────────────────────────────
	// Result & Save
	// ──────────────────────────────────────────────

	fun updateMemo(text: String) {
		updateRecording { copy(memo = text) }
	}

	fun saveHand() {
		val current = recording ?: return

		viewModelScope.launch {
			try {
				Logger.d(
					"saveHand: tableId=${current.tableId}, heroSeat=${current.table?.heroSeat}, heroResult=${current.heroResult}",
				)
				Logger.d(
					"saveHand: resolvedResults=${current.resolvedShowdown?.results?.size}, remainingSeats=${current.remainingSeats}",
				)
				Logger.d("saveHand: showdownEntries=${current.showdownEntries.size}")

				val heroSeat = current.table?.heroSeat ?: 0
				val results = current.resolvedShowdown?.results ?: current.showdownResults
				val handPlayers = current.occupiedSeats.map { seat ->
					val player = current.players[seat]
					val result = results.find { it.seat == seat }
					val tablePlayer = current.table?.players?.find { it.seat == seat }
					HandPlayer(
						seat = seat,
						cards = player?.cards,
						initialStack = player?.initialStack,
						ranking = result?.ranking,
						bestCards = result?.bestCards ?: emptyList(),
						outcome = result?.outcome,
						playerName = tablePlayer?.name,
						savedPlayerId = tablePlayer?.savedPlayerId,
						isHero = seat == heroSeat,
					)
				}

				val resultValue = current.resolvedShowdown?.heroResult ?: current.heroResult
				val tempRecord = HandRecord(
					id = "",
					tableId = current.tableId,
					createdAt = 0L,
					blinds = current.blinds,
					heroSeat = heroSeat,
					buttonSeat = current.buttonSeat,
					streets = current.streets,
					players = handPlayers,
					result = resultValue,
				)
				val resultLabel = buildResultLabel(tempRecord)
				val handRecord = tempRecord.copy(
					resultLabel = resultLabel,
					memo = current.memo.ifBlank { null },
				)

				Logger.d("saveHand: result=${handRecord.result}, isFoldWin=${handRecord.isFoldWin}")

				saveHandAndUpdateStacks(handRecord)
				Logger.d("saveHand: success")
				_effect.emit(RecordHandEffect.SaveSuccess)
			} catch (e: Exception) {
				Logger.e("saveHand: failed - ${e.message}", e)
				_effect.emit(RecordHandEffect.SaveError)
			}
		}
	}

	private suspend fun buildResultLabel(hand: HandRecord): String {
		val resultType = hand.resolvedHeroResultType
		val ranking = hand.heroRanking?.takeIf { it != HandRanking.WIN_BY_FOLD }
		val rankingStr = ranking?.let { getString(it.stringRes()) } ?: ""
		val resultRes = resultType.resultStringRes(rankingStr.isNotEmpty())
		return if (rankingStr.isNotEmpty()) getString(resultRes, rankingStr) else getString(resultRes)
	}

	// ──────────────────────────────────────────────
	// Showdown & Pot Distribution
	// ──────────────────────────────────────────────

	private fun distributeWinnings() {
		val current = recording ?: return
		val results = current.showdownResults
		if (results.isEmpty()) return

		val winnings = calculateWinnings(current)

		updateRecording {
			var updated = this
			winnings.forEach { (seat, amount) ->
				updated = updated.copy(
					players = updated.players.update(seat) {
						copy(stack = stack + amount)
					},
				)
			}
			// 확정된 결과 저장
			updated.copy(
				resolvedShowdown = ResolvedShowdown(
					results = results,
					potResults = updated.potResults,
					heroResult = updated.heroResult,
				),
			)
		}
	}

	// ──────────────────────────────────────────────
	// Helpers
	// ──────────────────────────────────────────────

	private fun getNextActionSeat(
		state: RecordHandState.Recording,
		currentSeat: Int,
		lastAggressor: Int?,
	): Int? = getNextActionSeat(state, currentSeat, lastAggressor, state.actionOrder.toSet())

	private fun updateRecording(block: RecordHandState.Recording.() -> RecordHandState.Recording) {
		_state.update { state ->
			if (state is RecordHandState.Recording) state.block() else state
		}
	}

}

internal fun getNextActionSeat(
	state: RecordHandState.Recording,
	currentSeat: Int,
	lastAggressor: Int?,
	activeSeats: Set<Int>,
): Int? {
	val fullOrder = if (state.currentStreet == Street.PREFLOP) {
		state.preflopActionOrder
	} else {
		state.postflopActionOrder
	}
	if (activeSeats.isEmpty()) return null

	val currentIdx = fullOrder.indexOf(currentSeat)
	if (currentIdx < 0) return null

	// aggressor 없으면 전원 1회씩 액션 완료 시 종료
	if (lastAggressor == null) {
		val actedSeats = state.streets.getActions(state.currentStreet).map { it.playerSeat }.toSet()
		if (activeSeats.all { it in actedSeats }) return null
	}

	for (i in 1..fullOrder.size) {
		val candidate = fullOrder[(currentIdx + i) % fullOrder.size]
		// 올인한 어그레서에 도달하면 라운드 종료 (activeSeats 체크 전에 판단)
		if (lastAggressor != null && candidate == lastAggressor) return null
		if (candidate !in activeSeats) continue
		return candidate
	}

	return null
}

internal fun calculateWinnings(state: RecordHandState.Recording): Map<Int, Double> {
	val boardCards = state.streets.boardCards
	val anteAmount = if (state.blinds?.isBigBlindAnte == true) (state.blinds?.bb ?: 0.0) else 0.0
	val investments = buildMap {
		for (seat in state.occupiedSeats) {
			val player = state.players[seat] ?: continue
			val initial = player.initialStack ?: continue
			var invested = initial - player.stack
			// BBA 앤티는 BB의 개인 투자에서 분리 (sidePots과 동일 패턴)
			if (seat == state.bbSeat && anteAmount > 0) {
				invested -= anteAmount
			}
			if (invested > 0) put(seat, invested)
		}
	}

	val entries = state.showdownEntries.filter { state.players[it.seat]?.isCardsUnknown != true }

	val sortedInvestments = investments.entries.sortedBy { it.value }
	var previousLevel = 0.0
	val winnings = mutableMapOf<Int, Double>()
	var isFirstLevel = true

	for ((_, invested) in sortedInvestments) {
		val diff = invested - previousLevel
		if (diff <= 0) continue

		val eligibleSeats = investments.filter { it.value >= invested }.keys
		var potForLevel = diff * eligibleSeats.size
		if (isFirstLevel && anteAmount > 0) {
			potForLevel += anteAmount
			isFirstLevel = false
		}

		val eligibleEntries = entries.filter { it.seat in eligibleSeats }
		val potWinners = if (eligibleEntries.size >= 2 && boardCards.size == 5) {
			val potResults = HandEvaluator.calculateShowdown(boardCards, eligibleEntries)
			potResults.filter { it.isWinner || it.isSplit }
		} else if (eligibleEntries.size == 1) {
			listOf(eligibleEntries.first()).map {
				ShowdownResult(seat = it.seat, ranking = HandRanking.HIGH_CARD, outcome = ShowdownOutcome.WIN)
			}
		} else {
			emptyList()
		}

		if (potWinners.isNotEmpty()) {
			val share = potForLevel / potWinners.size
			potWinners.forEach { winner ->
				winnings[winner.seat] = (winnings[winner.seat] ?: 0.0) + share
			}
		} else if (eligibleSeats.size == 1) {
			val sole = eligibleSeats.first()
			winnings[sole] = (winnings[sole] ?: 0.0) + potForLevel
		}
		previousLevel = invested
	}

	return winnings
}

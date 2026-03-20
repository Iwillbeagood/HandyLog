package com.hand.log.record

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hand.log.domain.model.Action
import com.hand.log.domain.model.ActionType
import com.hand.log.domain.model.Blinds
import com.hand.log.domain.model.Card
import com.hand.log.domain.model.HandRecord
import com.hand.log.domain.model.HeroHand
import com.hand.log.domain.model.PokerTable
import com.hand.log.domain.model.Street
import com.hand.log.domain.repository.AppSettingsRepository
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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

internal class RecordHandViewModel(
	private val tableId: String,
	private val pokerTableRepository: PokerTableRepository,
	private val handRecordRepository: HandRecordRepository,
	private val appSettingsRepository: AppSettingsRepository,
) : ViewModel() {

	private val _state = MutableStateFlow<RecordHandState>(RecordHandState.Loading)
	val state: StateFlow<RecordHandState> get() = _state

	private val _modalEffect = MutableStateFlow<RecordHandModalEffect>(RecordHandModalEffect.Idle)
	val modalEffect: StateFlow<RecordHandModalEffect> get() = _modalEffect

	private val _effect = MutableSharedFlow<RecordHandEffect>()
	val effect: SharedFlow<RecordHandEffect> get() = _effect.asSharedFlow()

	private val recording: RecordHandState.Recording?
		get() = _state.value as? RecordHandState.Recording

	init {
		loadTable()
	}

	private fun loadTable() {
		viewModelScope.launch {
			val table = pokerTableRepository.getTableById(tableId)
			val preflopPresets = appSettingsRepository.observeBetSizePresets().first()
			val postflopPresets = appSettingsRepository.observePotPercentPresets().first()

			_state.update {
				RecordHandState.Recording(
					tableId = tableId,
					table = table,
					players = RecordPlayers.create(
						playerCount = table?.playerCount ?: 0,
						defaultStack = table?.startingStack ?: 0.0,
						stacks = table?.players?.associate { it.seat to it.stack } ?: emptyMap(),
					),
					buttonSeat = 1,
					blinds = table?.blinds,
					preflopPresets = preflopPresets,
					postflopPresets = postflopPresets,
				)
			}
		}
	}

	// ──────────────────────────────────────────────
	// Card Selection
	// ──────────────────────────────────────────────

	fun selectHeroCard() {
		val current = recording ?: return
		val usedCards = current.selectedCards - current.heroCards.toSet()
		showCardSelector(
			title = "히어로 카드 선택",
			target = CardSelectorTarget.HeroCard(maxCards = 2),
			usedCards = usedCards,
		)
	}

	fun selectBoardCard(street: Street) {
		val current = recording ?: return
		val existingCards = current.streets.getCards(street).toSet()
		showCardSelector(
			title = "${street.label} 카드 선택",
			target = CardSelectorTarget.BoardCard(street, maxCards = if (street == Street.FLOP) 3 else 1),
			usedCards = current.selectedCards - existingCards,
		)
	}

	fun selectSingleBoardCard(street: Street, cardIndex: Int) {
		val current = recording ?: return
		val existingCard = current.streets.getCards(street).getOrNull(cardIndex)
		val usedCards = if (existingCard != null) {
			current.selectedCards - setOf(existingCard)
		} else {
			current.selectedCards
		}
		showCardSelector(
			title = "${street.label} 카드 변경",
			target = CardSelectorTarget.SingleBoardCard(street, cardIndex),
			usedCards = usedCards,
		)
	}

	fun selectShowdownCard(seat: Int) {
		val current = recording ?: return
		val existingCards = current.showdown[seat]?.cards?.toSet() ?: emptySet()
		val posName = current.positionName(seat)
		showCardSelector(
			title = "$posName 카드 선택",
			target = CardSelectorTarget.ShowdownCard(seat),
			usedCards = current.selectedCards - existingCards,
		)
	}

	private fun showCardSelector(title: String, target: CardSelectorTarget, usedCards: Set<Card>) {
		_modalEffect.value = RecordHandModalEffect.ShowCardSelector(
			title = title,
			target = target,
			selectedCards = usedCards,
		)
	}

	fun onCardsSelected(cards: List<Card>) {
		val current = recording ?: return
		val modal = _modalEffect.value as? RecordHandModalEffect.ShowCardSelector ?: return

		when (modal.target) {
			is CardSelectorTarget.HeroCard -> {
				val newHeroHand = if (cards.size >= 2) HeroHand(cards[0], cards[1]) else null
				updateRecording { copy(heroHand = newHeroHand) }
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
				val newHand = if (cards.size >= 2) HeroHand(cards[0], cards[1]) else null
				updateRecording { copy(showdown = showdown.set(modal.target.seat, newHand)) }
			}
		}
		dismissModal()

		val updated = recording ?: return

		// 보드 카드 선택 후, 액션할 플레이어가 없으면 자동으로 다음 스트릿
		if (modal.target is CardSelectorTarget.BoardCard &&
			updated.streets.isBoardReady(updated.currentStreet) &&
			updated.actionOrder.isEmpty()
		) {
			if (updated.remainingSeats.size <= 1) goToShowdown() else nextStep()
		}

		// 쇼다운 카드 선택 완료 시 승자에게 팟 분배
		if (modal.target is CardSelectorTarget.ShowdownCard && updated.isShowdownComplete) {
			distributeWinnings()
		}
	}

	fun setShowdownUnknown(seat: Int) {
		updateRecording { copy(showdown = showdown.setUnknown(seat)) }
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
		val updatedPlayers = if (table.playerCount != current.table?.playerCount) {
			RecordPlayers.create(playerCount = table.playerCount, defaultStack = table.startingStack)
		} else {
			current.players
		}
		updateRecording {
			copy(table = table, blinds = table.blinds, players = updatedPlayers)
		}
		dismissModal()
	}

	fun updateButtonSeat(seat: Int) {
		updateRecording { copy(buttonSeat = seat) }
	}

	fun updateBlinds(sb: String, bb: String) {
		updateRecording {
			copy(
				blinds = Blinds(
					sb = sb.toDoubleOrNull() ?: 0.0,
					bb = bb.toDoubleOrNull() ?: 0.0,
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

	// ──────────────────────────────────────────────
	// Action
	// ──────────────────────────────────────────────

	fun selectActionSeat(seat: Int) {
		val current = recording ?: return
		val isOpenerSelection = current.currentStreet == Street.PREFLOP &&
			current.streets.getActions(Street.PREFLOP).isEmpty() &&
			current.currentActionSeat == null

		if (isOpenerSelection) {
			// 오프너 선택: 선택한 좌석 이전의 모든 플레이어를 자동 폴드
			val actionOrder = current.preflopActionOrder
			val precedingSeats = actionOrder.takeWhile { it != seat }

			updateRecording {
				var updated = this
				precedingSeats.forEach { foldSeat ->
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
				updated.copy(currentActionSeat = seat)
			}
		} else {
			updateRecording { copy(currentActionSeat = seat) }
		}
	}

	fun selectActionType(type: ActionType) {
		updateRecording { copy(currentActionType = type) }
		val requiresAmount = type == ActionType.BET || type == ActionType.RAISE
		if (!requiresAmount) confirmAction()
	}

	fun updateActionAmount(amount: String) {
		updateRecording {
			val seat = currentActionSeat ?: return@updateRecording copy(currentActionAmount = amount)
			val player = players[seat] ?: return@updateRecording copy(currentActionAmount = amount)
			val effectiveStack = player.stack + player.currentBet
			val parsed = amount.toDoubleOrNull() ?: 0.0
			if (parsed > effectiveStack) return@updateRecording this
			copy(currentActionAmount = amount)
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

		// 1) 액션 타입 & 금액 결정
		val (resolvedType, amount) = resolveAction(
			type = type,
			effectiveStack = effectiveStack,
			streetMaxBet = if (streetMaxBet > 0.0) streetMaxBet else current.blinds?.bb ?: 0.0,
			raiseTarget = current.currentActionAmount.toDoubleOrNull() ?: 0.0,
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
		val action = Action(
			playerSeat = seat,
			type = resolvedType,
			amount = amount,
			stackBefore = effectiveStack,
			stackAfter = newStack,
			betLevel = betLevel,
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
		effectiveStack: Double,
		streetMaxBet: Double,
		raiseTarget: Double,
		minRaise: Double,
	): Pair<ActionType, Double?>? = when (type) {
		ActionType.FOLD, ActionType.CHECK -> type to null

		ActionType.CALL -> if (streetMaxBet >= effectiveStack) {
			ActionType.ALL_IN to effectiveStack
		} else {
			ActionType.CALL to streetMaxBet
		}

		ActionType.ALL_IN -> ActionType.ALL_IN to effectiveStack

		ActionType.BET, ActionType.RAISE -> when {
			raiseTarget < minRaise -> null
			raiseTarget >= effectiveStack -> ActionType.ALL_IN to effectiveStack
			else -> type to raiseTarget
		}
	}

	/** 라운드 종료 후 다음 단계 진행 */
	private fun advanceAfterRoundEnd() {
		val current = recording ?: return
		if (current.remainingSeats.size <= 1) goToShowdown() else nextStep()
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
				)
			}

			val stepped = copy(currentStep = nextStep)
			val updated = stepped.copy(
				streets = streets.ensureStreet(stepped.currentStreet),
				players = players.resetCurrentBets(),
				currentActionType = null,
				currentActionAmount = "",
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
		updateRecording {
			if (currentStep == RecordStep.SETUP) return@updateRecording this

			val targetStep = findLastActiveStep()
			val prevUpdated = copy(
				currentStep = targetStep,
				currentActionType = null,
				currentActionAmount = "",
				lastAggressorSeat = null,
			)
			val lastAction = prevUpdated.streets.getActions(prevUpdated.currentStreet).lastOrNull()
			prevUpdated.copy(currentActionSeat = lastAction?.playerSeat)
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
		updateRecording { copy(useBbUnit = !useBbUnit) }
	}

	// ──────────────────────────────────────────────
	// Result & Save
	// ──────────────────────────────────────────────

	fun updateMemo(text: String) {
		updateRecording { copy(memo = text) }
	}

	@OptIn(ExperimentalTime::class)
	fun saveHand() {
		val current = recording ?: return

		viewModelScope.launch {
			try {
				val handRecord = HandRecord(
					id = generateId(),
					tableId = current.tableId,
					createdAt = Clock.System.now().toEpochMilliseconds(),
					blinds = current.blinds,
					heroHand = current.heroHand,
					heroSeat = current.table?.heroSeat ?: 0,
					heroStack = current.heroStack,
					buttonSeat = current.buttonSeat,
					streets = current.streets,
					showdown = current.showdown.toShowdownEntries(),
					result = current.heroResult,
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

	// ──────────────────────────────────────────────
	// Showdown & Pot Distribution
	// ──────────────────────────────────────────────

	private fun distributeWinnings() {
		val current = recording ?: return
		val winners = current.showdownResults.filter { it.isWinner }
		if (winners.isEmpty()) return

		val count = current.table?.playerCount ?: return
		val investments = buildMap {
			for (seat in 1..count) {
				val player = current.players[seat] ?: continue
				val invested = player.initialStack - player.stack
				if (invested > 0) put(seat, invested)
			}
		}

		// 사이드팟 계산: 투입 금액 기준 정렬
		val sortedInvestments = investments.entries.sortedBy { it.value }
		var previousLevel = 0.0
		val winnings = mutableMapOf<Int, Double>()

		for ((_, invested) in sortedInvestments) {
			val diff = invested - previousLevel
			if (diff <= 0) continue

			val eligible = investments.filter { it.value >= invested }
			val potForLevel = diff * eligible.size
			val levelWinners = winners.filter { it.seat in eligible }
			if (levelWinners.isNotEmpty()) {
				val share = potForLevel / levelWinners.size
				levelWinners.forEach { winner ->
					winnings[winner.seat] = (winnings[winner.seat] ?: 0.0) + share
				}
			}
			previousLevel = invested
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

	// ──────────────────────────────────────────────
	// Helpers
	// ──────────────────────────────────────────────

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

		// aggressor 없으면 전원 1회씩 액션 완료 시 종료
		if (lastAggressor == null) {
			val actedSeats = state.streets.getActions(state.currentStreet).map { it.playerSeat }.toSet()
			if (activeSeats.all { it in actedSeats }) return null
		}

		for (i in 1..fullOrder.size) {
			val candidate = fullOrder[(currentIdx + i) % fullOrder.size]
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

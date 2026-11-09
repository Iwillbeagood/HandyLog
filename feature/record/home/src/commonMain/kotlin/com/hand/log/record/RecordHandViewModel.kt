package com.hand.log.record

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hand.log.domain.model.Action
import com.hand.log.domain.model.ActionType
import com.hand.log.domain.model.HandRanking
import com.hand.log.domain.model.Blinds
import com.hand.log.domain.model.Card
import com.hand.log.domain.model.GameType.Cash
import com.hand.log.domain.model.GameType.Tournament
import com.hand.log.domain.model.HandPlayer
import com.hand.log.domain.model.HandRecord
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

		if (modal.target is CardSelectorTarget.BoardCard &&
			updated.streets.isBoardReady(updated.currentStreet) &&
			updated.actionOrder.isEmpty()
		) {
			if (updated.remainingSeats.size <= 1 || updated.isAllInShowdown) goToShowdown() else nextStep()
		}
	}

	fun setShowdownUnknown(seat: Int) {
		updateRecording {
			copy(players = players.update(seat) { copy(isCardsUnknown = true, cards = null) })
		}
	}

	fun dismissModal() {
		_modalEffect.value = RecordHandModalEffect.Idle
	}

	fun showTableEdit() {
		val current = recording ?: return
		val table = current.table ?: return
		_modalEffect.value = RecordHandModalEffect.ShowTableEdit(table)
	}

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
		updateRecording { withInitialStack(seat, amount.toDoubleOrNull()) }
	}

	fun selectActionSeat(seat: Int) {
		val current = recording ?: return

		if (current.currentStreet != Street.PREFLOP) {
			updateRecording { copy(currentActionSeat = seat) }
			return
		}

		if (seat in current.players.inactiveSeats) return

		val actedSeats = current.streets.getActions(Street.PREFLOP).map { it.playerSeat }.toSet()
		if (seat in actedSeats) return

		val currentSeat = current.currentActionSeat
		if (seat == currentSeat) return

		val actionOrder = current.preflopActionOrder
		val activeSeats = actionOrder.filter { it !in current.players.inactiveSeats && it !in actedSeats }

		if (seat !in activeSeats) return

		val seatsToFold = if (currentSeat == null) {
			actionOrder.takeWhile { it != seat }.filter { it in activeSeats }
		} else {
			val currentIdx = activeSeats.indexOf(currentSeat)
			val targetIdx = activeSeats.indexOf(seat)
			if (targetIdx <= currentIdx) return
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
			if (player.initialStack != null && parseInputToChip(amount) > player.stack + player.currentBet) {
				return@updateRecording this
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

		val (resolvedType, amount) = resolveAction(
			type = type,
			isUnlimited = player.initialStack == null,
			effectiveStack = effectiveStack,
			streetMaxBet = if (streetMaxBet > 0.0) streetMaxBet else current.blinds?.bb ?: 0.0,
			raiseTarget = current.parseInputToChip(current.currentActionAmount),
			minRaise = current.minRaiseAmount,
		) ?: return

		val isAggressive = resolvedType == ActionType.BET ||
			resolvedType == ActionType.RAISE ||
			(resolvedType == ActionType.ALL_IN && (amount ?: 0.0) > streetMaxBet)
		val betLevel = if (isAggressive) current.currentBetLevel + 1 else 0

		val newStack = if (amount != null) {
			(effectiveStack - amount).coerceAtLeast(0.0)
		} else {
			player.stack
		}

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

		val nextState = current.copy(streets = updatedStreets, players = updatedPlayers)
		val nextSeat = getNextActionSeat(nextState, seat, nextState.lastAggressorSeat)

		updateRecording {
			copy(
				streets = updatedStreets,
				players = updatedPlayers,
				currentActionSeat = nextSeat,
				currentActionType = null,
				currentActionAmount = "",
				currentActionChipAmount = null,
			)
		}

		if (nextSeat == null) advanceAfterRoundEnd()
	}

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

	private fun advanceAfterRoundEnd() {
		val current = recording ?: return
		if (current.remainingSeats.size <= 1 || current.isAllInShowdown) goToShowdown() else nextStep()
	}

	fun removeLastAction() {
		updateRecording { withLastActionUndone() }
	}

	fun nextStep() {
		val beforeStep = recording
		if (beforeStep != null && beforeStep.remainingSeats.size <= 1) {
			goToShowdown()
			return
		}

		updateRecording { advancedToNextStep() }

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
		updateRecording { rewoundTo(targetStep) }
		if (undoLastAction) removeLastAction()
	}

	private fun goToShowdown() {
		updateRecording {
			val heroSeat = table?.heroSeat
			copy(
				currentStep = RecordStep.SHOWDOWN,
				currentActionSeat = null,
				currentActionType = null,
				currentActionAmount = "",
				currentActionChipAmount = null,
				players = remainingSeats.filter { it != heroSeat }.fold(players) { hidden, seat ->
					hidden.update(seat) { copy(isCardsUnknown = true, cards = null) }
				},
			)
		}

		val updated = recording ?: return
		if (updated.remainingSeats.size > 1 && updated.streets.boardCards.size < 5) {
			selectAllBoardCards()
		}
	}

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

	fun updateMemo(text: String) {
		updateRecording { copy(memo = text) }
	}

	fun saveHand() {
		val current = recording ?: return
		if (current.isSaving) return
		updateRecording { copy(isSaving = true) }

		viewModelScope.launch {
			try {
				Logger.d(
					"saveHand: tableId=${current.tableId}, heroSeat=${current.table?.heroSeat}, heroResult=${current.heroResult}",
				)

				val heroSeat = current.table?.heroSeat ?: 0
				val results = current.showdownResults
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

				val tempRecord = HandRecord(
					id = "",
					tableId = current.tableId,
					createdAt = 0L,
					blinds = current.blinds,
					heroSeat = heroSeat,
					buttonSeat = current.buttonSeat,
					streets = current.streets,
					players = handPlayers,
					result = current.heroResult,
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
				updateRecording { copy(isSaving = false) }
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

	if (lastAggressor == null) {
		val actedSeats = state.streets.getActions(state.currentStreet).map { it.playerSeat }.toSet()
		if (activeSeats.all { it in actedSeats }) return null
	}

	for (i in 1..fullOrder.size) {
		val candidate = fullOrder[(currentIdx + i) % fullOrder.size]
		if (lastAggressor != null && candidate == lastAggressor) return null
		if (candidate !in activeSeats) continue
		return candidate
	}

	return null
}

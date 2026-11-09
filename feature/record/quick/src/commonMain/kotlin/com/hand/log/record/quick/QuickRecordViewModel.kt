package com.hand.log.record.quick

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hand.log.domain.model.Blinds
import com.hand.log.domain.model.GameType
import com.hand.log.domain.model.HandRanking
import com.hand.log.domain.model.HandRecord
import com.hand.log.domain.model.Card
import com.hand.log.domain.model.quick.CardOwner
import com.hand.log.domain.model.quick.QuickHandConfig
import com.hand.log.domain.model.quick.QuickHandMachine
import com.hand.log.domain.model.quick.QuickHandSnapshot
import com.hand.log.domain.model.quick.QuickPending
import com.hand.log.record.quick.contract.CardPickerOrigin
import com.hand.log.record.quick.contract.DraftEntry
import com.hand.log.record.quick.model.DraftContext
import com.hand.log.record.quick.model.QuickDraft
import com.hand.log.record.quick.model.QuickLineParser
import com.hand.log.domain.model.quick.QuickPhase
import com.hand.log.domain.model.ActionType
import com.hand.log.domain.model.quick.QuickInput
import com.hand.log.domain.repository.AppSettingsRepository
import com.hand.log.domain.usecase.LoadRecordDataUseCase
import com.hand.log.domain.usecase.SaveHandAndUpdateStacksUseCase
import com.hand.log.platform.etc.Logger
import com.hand.log.record.quick.contract.QuickRecordEffect
import com.hand.log.record.quick.contract.QuickRecordModalEffect
import com.hand.log.record.quick.contract.QuickRecordState
import com.hand.log.record.quick.contract.QuickSetup
import com.hand.log.record.quick.contract.StreetCloseTarget
import com.hand.log.domain.model.Street
import com.hand.log.record.quick.model.HandKey
import com.hand.log.record.quick.model.rankAt
import com.hand.log.record.quick.model.toHandKeys
import com.hand.log.record.quick.model.QuickEditorRenderer
import com.hand.log.ui.poker.formatBbCount
import com.hand.log.ui.poker.formatWithComma
import com.hand.log.ui.resultStringRes
import com.hand.log.ui.stringRes
import com.hand.log.utils.poker.HandEvaluator
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
import org.jetbrains.compose.resources.getString

internal class QuickRecordViewModel(
	private val tableId: String?,
	private val loadRecordData: LoadRecordDataUseCase,
	private val saveHandAndUpdateStacks: SaveHandAndUpdateStacksUseCase,
	private val appSettingsRepository: AppSettingsRepository,
) : ViewModel() {

	private val _modalEffect = MutableStateFlow<QuickRecordModalEffect>(QuickRecordModalEffect.Idle)
	val modalEffect: StateFlow<QuickRecordModalEffect> get() = _modalEffect

	private val _effect = MutableSharedFlow<QuickRecordEffect>()
	val effect: SharedFlow<QuickRecordEffect> get() = _effect.asSharedFlow()

	private var config: QuickHandConfig? = null
	private var hasUnitBlinds = true
	private var setup = QuickSetup()
	private var savedInChips = false

	private val _state = MutableStateFlow<QuickRecordState>(QuickRecordState.Loading)
	val state: StateFlow<QuickRecordState> = _state.onStart {
		load()
	}.stateIn(
		scope = viewModelScope,
		started = SharingStarted.Eagerly,
		initialValue = QuickRecordState.Loading,
	)

	private val editing: QuickRecordState.Editing?
		get() = _state.value as? QuickRecordState.Editing

	private fun load() {
		if (config != null) return
		viewModelScope.launch {
			savedInChips = appSettingsRepository.observeQuickAmountInChips().first()
			if (tableId == null) {
				startWithSetup(setup)
				return@launch
			}
			val table = loadRecordData(tableId).table ?: return@launch
			val seats = (table.players.map { it.seat } + table.heroSeat).distinct().sorted()
			val gameType = table.gameType
			setup = (gameType as? GameType.Cash)?.let {
				QuickSetup(sbText = it.sb.toInputText(), bbText = it.bb.toInputText())
			} ?: QuickSetup()
			val blinds = when (gameType) {
				is GameType.Cash -> Blinds(sb = gameType.sb, bb = gameType.bb, straddle = gameType.straddle)
				is GameType.Tournament -> Blinds(sb = 0.5, bb = 1.0, isBigBlindAnte = gameType.isBigBlindAnte)
			}
			startEditing(
				newConfig = QuickHandConfig(
					tableId = tableId,
					heroSeat = table.heroSeat,
					seats = seats,
					blinds = blinds,
					tablePlayers = table.players,
				),
				blindsLabel = (gameType as? GameType.Cash)?.let { blindsLabel(it.sb, it.bb) },
				isTournament = gameType is GameType.Tournament,
			)
		}
	}

	private fun startWithSetup(newSetup: QuickSetup) {
		val blinds = newSetup.blinds ?: return
		if (!newSetup.isValid) return
		setup = newSetup
		startEditing(
			newConfig = QuickHandConfig(
				tableId = null,
				heroSeat = QuickHandConfig.HERO_SEAT_WITHOUT_TABLE,
				seats = (1..QuickHandConfig.DEFAULT_PLAYER_COUNT).toList(),
				blinds = blinds,
				stackInBb = newSetup.stackInBb,
			),
			blindsLabel = if (newSetup.hasBlinds) blindsLabel(blinds.sb, blinds.bb) else null,
			isTournament = false,
		)
	}

	private fun startEditing(
		newConfig: QuickHandConfig,
		blindsLabel: String?,
		isTournament: Boolean,
	) {
		hasUnitBlinds = blindsLabel != null || isTournament
		val startConfig = newConfig.copy(startInChips = savedInChips && hasUnitBlinds)
		config = startConfig
		val snapshot = QuickHandMachine(startConfig).replay(emptyList())
		_state.update {
			QuickRecordState.Editing(
				blindsLabel = blindsLabel,
				isTournament = isTournament,
				stackLabel = newConfig.stackInBb?.let(::formatBbCount),
				snapshot = snapshot,
				lines = QuickEditorRenderer.render(snapshot, showBbUnit = showsBbUnit(snapshot)),
			)
		}
		applyInputs(emptyList(), draft = emptyList(), history = emptyList())
	}

	private fun showsBbUnit(snapshot: QuickHandSnapshot): Boolean = hasUnitBlinds || !snapshot.amountInChips

	private fun Double.toInputText(): String =
		if (this % 1.0 == 0.0) toLong().toString() else toString()

	private fun blindsLabel(sb: Double, bb: Double): String = "${sb.toBlindText()}/${bb.toBlindText()}"

	private fun Double.toBlindText(): String = if (this % 1.0 == 0.0) {
		formatWithComma(
			toLong(),
		)
	} else {
		toString()
	}

	fun showSetup() {
		if (editing?.canEditSetup != true) return
		_modalEffect.update { QuickRecordModalEffect.ShowSetup(setup) }
	}

	fun updateSetup(newSetup: QuickSetup) {
		_modalEffect.update { modal ->
			if (modal is QuickRecordModalEffect.ShowSetup) modal.copy(setup = newSetup) else modal
		}
	}

	fun confirmSetup() {
		val modal = _modalEffect.value as? QuickRecordModalEffect.ShowSetup ?: return
		if (!modal.setup.isValid || editing?.canEditSetup != true) return
		if (!applySetup(modal.setup)) {
			viewModelScope.launch { _effect.emit(QuickRecordEffect.SetupConflict) }
			return
		}
		dismissModal()
		if (modal.switchToChipOnConfirm && hasUnitBlinds) pressKey(HandKey.AmountUnit)
	}

	private fun applySetup(newSetup: QuickSetup): Boolean {
		val current = editing ?: return false
		val currentConfig = config ?: return false
		val blinds = newSetup.blinds ?: return false
		if (!newSetup.isValid) return false
		val stackInBb = newSetup.stackInBb
		val newConfig = currentConfig.copy(
			blinds = blinds.copy(
				straddle = currentConfig.blinds.straddle,
				isBigBlindAnte = currentConfig.blinds.isBigBlindAnte,
			),
			stackInBb = stackInBb,
		)
		val replayed = QuickHandMachine(newConfig).replay(current.inputs)
		if (replayed.actionShape != current.snapshot.actionShape) return false
		setup = newSetup
		config = newConfig
		val newBlindsLabel = if (newSetup.hasBlinds) blindsLabel(blinds.sb, blinds.bb) else null
		hasUnitBlinds = newBlindsLabel != null || current.isTournament
		_state.update { state ->
			if (state !is QuickRecordState.Editing) return@update state
			state.copy(
				blindsLabel = newBlindsLabel,
				stackLabel = stackInBb?.let(::formatBbCount),
			)
		}
		applyInputs(current.inputs)
		return true
	}

	private val QuickHandSnapshot.actionShape: List<Any?>
		get() = Street.entries.map { street ->
			record.streets.getActions(street).map { it.playerSeat }
		} + phase

	fun pressKey(key: HandKey) {
		val current = editing ?: return
		if (key !in current.enabledKeys) return
		if (key == HandKey.AmountUnit && !current.amountInChips && !hasUnitBlinds) {
			if (current.canEditSetup) {
				_modalEffect.update { QuickRecordModalEffect.ShowSetup(setup, switchToChipOnConfirm = true) }
			}
			return
		}
		when (key) {
			HandKey.Backspace -> erase(current)
			HandKey.NextLine -> commitLine(current)
			HandKey.AmountUnit -> toggleAmountUnit(current)
			HandKey.CardPicker -> openDraftCardPicker(current)
			else -> {
				val context = draftContext(current.snapshot, current.stackLabel)
				val draft = QuickDraft.append(current.draft, key, context) ?: return
				if (QuickLineParser.isBoardComplete(draft, current.snapshot) || completesAction(current, key, draft, context)) {
					commitDraft(current, draft)
				} else {
					applyInputs(current.inputs, draft = draft)
				}
			}
		}
	}

	private fun draftContext(snapshot: QuickHandSnapshot, stackLabel: String?) = DraftContext(
		maxCards = if (snapshot.phase == QuickPhase.BOARD) snapshot.boardSlots else HOLE_CARD_COUNT,
		usedCards = snapshot.usedCards,
		numbersAreRanks = snapshot.phase == QuickPhase.BOARD,
		allInNeedsAmount = stackLabel == null,
	)

	private fun openDraftCardPicker(current: QuickRecordState.Editing) {
		val owner = current.draftCardOwner ?: return
		_modalEffect.update {
			QuickRecordModalEffect.ShowCardPicker(
				owner = owner,
				positionLabel = current.draftCardPosition?.label.orEmpty(),
				cardCount = draftContext(current.snapshot, current.stackLabel).maxCards,
				initialCards = emptyList(),
				usedCards = current.snapshot.usedCards,
				origin = CardPickerOrigin.KEYBOARD,
			)
		}
	}

	private fun fillDraftCards(current: QuickRecordState.Editing, cards: List<Card>) {
		val cardKeys = cards.filter { it.suit != null }.flatMap { it.toHandKeys() }
		if (cardKeys.isEmpty()) return
		val numbersAreRanks = current.snapshot.phase == QuickPhase.BOARD
		val rest = current.draft.filterIndexed { index, key ->
			key !is HandKey.SuitKey && current.draft.rankAt(index, numbersAreRanks) == null
		}
		val (positionKeys, otherKeys) = rest.partition { it is HandKey.Pos }
		val draft = positionKeys + cardKeys + otherKeys
		if (rest.isEmpty()) commitDraft(current, draft) else applyInputs(current.inputs, draft = draft)
	}

	private fun toggleAmountUnit(current: QuickRecordState.Editing) {
		applyInputs(current.inputs + QuickInput.ToggleAmountUnit)
		val inChips = editing?.amountInChips ?: return
		savedInChips = inChips
		viewModelScope.launch { appSettingsRepository.setQuickAmountInChips(inChips) }
	}

	private fun completesAction(
		current: QuickRecordState.Editing,
		key: HandKey,
		draft: List<HandKey>,
		context: DraftContext,
	): Boolean {
		if (current.snapshot.phase != QuickPhase.ACTION && current.snapshot.phase != QuickPhase.HERO_CARDS) return false
		if (current.snapshot.street == Street.PREFLOP) return false
		return when (key) {
			is HandKey.Sized -> draft.any { it is HandKey.Act }
			is HandKey.Act -> {
				if (key.type == ActionType.ALL_IN || !QuickDraft.needsAmount(key.type, context)) return true
				val line = QuickLineParser.ParsedLine.of(draft, numbersAreRanks = false) ?: return false
				line.amount.isNotEmpty() || line.potSize != null
			}
			else -> false
		}
	}

	private fun erase(current: QuickRecordState.Editing) {
		val entry = current.history.lastOrNull()
		when {
			current.draft.isNotEmpty() -> applyInputs(current.inputs, draft = current.draft.dropLast(1))
			entry != null && entry.after == current.inputs ->
				applyInputs(entry.before, draft = entry.tokens, history = current.history.dropLast(1))
			else -> applyInputs(current.inputs.dropLast(1))
		}
	}

	private fun commitLine(current: QuickRecordState.Editing) {
		if (current.draft.isNotEmpty()) {
			commitDraft(current, current.draft)
		} else if (QuickInput.NextLine in current.snapshot.allowed) {
			applyInputs(current.inputs + QuickInput.NextLine)
		} else if (current.streetCloseTarget != null) {
			applyInputs(current.inputs + QuickInput.CloseStreet)
		}
	}

	private fun commitDraft(current: QuickRecordState.Editing, draft: List<HandKey>) {
		val machine = QuickHandMachine(config ?: return)
		val inputs = committedInputs(machine, current.inputs, current.snapshot, draft)
		if (inputs == null) {
			applyInputs(current.inputs, draft = draft)
			val effect = if (current.hasCardConflict) QuickRecordEffect.CardConflict else QuickRecordEffect.InvalidLine
			viewModelScope.launch { _effect.emit(effect) }
			return
		}
		applyInputs(
			inputs,
			draft = emptyList(),
			history = current.history + DraftEntry(before = current.inputs, after = inputs, tokens = draft),
		)
	}

	private fun committedInputs(
		machine: QuickHandMachine,
		inputs: List<QuickInput>,
		snapshot: QuickHandSnapshot,
		draft: List<HandKey>,
	): List<QuickInput>? {
		val parsed = QuickLineParser.parse(draft, snapshot) ?: return null
		if (snapshot.phase == QuickPhase.ACTION && parsed.none { it is QuickInput.Act }) return null
		val committed = inputs + parsed
		return committed.takeIf { machine.replay(it).isCommittedFrom(snapshot) }
	}

	private fun legalActionsFor(
		machine: QuickHandMachine,
		inputs: List<QuickInput>,
		snapshot: QuickHandSnapshot,
		draft: List<HandKey>,
	): Set<ActionType> {
		val position = draft.filterIsInstance<HandKey.Pos>().lastOrNull()?.position
		val preview = if (position != null && position != snapshot.actorPosition) {
			machine.replay(inputs + QuickInput.Seat(position))
		} else {
			snapshot
		}
		return preview.allowed.filterIsInstance<QuickInput.Act>().map { it.type }.toSet()
	}

	private fun streetCloseFor(
		machine: QuickHandMachine,
		inputs: List<QuickInput>,
		snapshot: QuickHandSnapshot,
		draft: List<HandKey>,
	): StreetCloseTarget? {
		if (draft.isNotEmpty() || QuickInput.CloseStreet !in snapshot.allowed) return null
		val preview = machine.replay(inputs + QuickInput.CloseStreet)
		return when {
			preview.phase == QuickPhase.REVIEW -> null
			preview.street == snapshot.street -> StreetCloseTarget.HERO
			preview.street == Street.FLOP -> StreetCloseTarget.FLOP
			preview.street == Street.TURN -> StreetCloseTarget.TURN
			else -> StreetCloseTarget.RIVER
		}
	}

	private fun QuickHandSnapshot.isCommittedFrom(previous: QuickHandSnapshot): Boolean =
		pending !is QuickPending.Staged &&
			pending !is QuickPending.Amount &&
			(
				record.streets != previous.record.streets ||
					phase != previous.phase ||
					heroCards != previous.heroCards ||
					opponentHands != previous.opponentHands ||
					actorPosition != previous.actorPosition
				)

	fun showHeroSelect() {
		val current = editing ?: return
		if (!current.canSelectHero) return
		_modalEffect.update {
			QuickRecordModalEffect.ShowHeroSelect(
				positionOptions = current.heroSelectablePositions.map { it.label },
				heroPosition = current.heroLabel,
			)
		}
	}

	fun selectHero(label: String) {
		val current = editing ?: return
		val position = current.heroSelectablePositions.find { it.label == label } ?: return
		val machine = QuickHandMachine(config ?: return)
		applyInputs(machine.withHero(current.inputs, position))
		dismissModal()
	}

	private fun applyInputs(
		inputs: List<QuickInput>,
		draft: List<HandKey> = editing?.draft.orEmpty(),
		history: List<DraftEntry> = editing?.history.orEmpty(),
	) {
		val currentConfig = config ?: return
		val machine = QuickHandMachine(currentConfig)
		val snapshot = machine.replay(inputs)
		val finalRecord = if (snapshot.canSave) {
			machine.build(inputs, HandEvaluator::calculateShowdown)
		} else {
			null
		}
		_state.update { state ->
			if (state !is QuickRecordState.Editing) return@update state
			state.copy(
				inputs = inputs,
				snapshot = snapshot,
				lines = QuickEditorRenderer.render(snapshot, finalRecord, showsBbUnit(snapshot), draft),
				draft = draft,
				history = history,
				legalActions = legalActionsFor(machine, inputs, snapshot, draft),
				selectablePositions = snapshot.allowed.filterIsInstance<QuickInput.Seat>().map {
					it.position
				}.toSet(),
				streetCloseTarget = streetCloseFor(machine, inputs, snapshot, draft),
				isDraftCommittable = committedInputs(machine, inputs, snapshot, draft) != null,
				hasCardConflict = QuickDraft.hasCardConflict(draft, draftContext(snapshot, state.stackLabel)),
				isFinishOpen = snapshot.finish != null && (state.isFinishOpen || !state.isFinishing),
			)
		}
	}

	fun showFinish() = setFinishOpen(true)

	private fun setFinishOpen(open: Boolean) {
		_state.update { state ->
			if (state !is QuickRecordState.Editing || (open && !state.isFinishing)) return@update state
			state.copy(isFinishOpen = open)
		}
	}

	fun selectHeroPosition(label: String) {
		val position = editing?.snapshot?.positions?.find { it.label == label } ?: return
		applyFinishInput(QuickInput.Seat(position))
	}

	fun showFinishCardPicker(owner: CardOwner) {
		val current = editing ?: return
		if (current.isSaving) return
		val snapshot = current.snapshot
		val opponent = (owner as? CardOwner.Opponent)?.let { target ->
			snapshot.opponentHands.find { it.position == target.position } ?: return
		}
		val cards = when (owner) {
			CardOwner.Hero -> snapshot.heroCards
			CardOwner.Board -> current.finish?.board.orEmpty()
			is CardOwner.Opponent -> opponent?.cards.orEmpty()
		}
		val cardCount = when (owner) {
			CardOwner.Board -> current.finish?.board?.size ?: snapshot.boardSlots
			else -> HOLE_CARD_COUNT
		}
		if (cardCount == 0) return
		val position = when (owner) {
			CardOwner.Hero -> current.heroPosition
			CardOwner.Board -> null
			is CardOwner.Opponent -> opponent?.position
		}
		_modalEffect.update {
			QuickRecordModalEffect.ShowCardPicker(
				owner = owner,
				positionLabel = position?.label.orEmpty(),
				cardCount = cardCount,
				initialCards = cards.filter { it.suit != null },
				usedCards = snapshot.usedCards - cards.toSet(),
			)
		}
	}

	fun selectCards(owner: CardOwner, cards: List<Card>) {
		val current = editing ?: return
		if (current.isSaving) return
		val origin = (_modalEffect.value as? QuickRecordModalEffect.ShowCardPicker)?.origin
		dismissModal()
		if (origin == CardPickerOrigin.KEYBOARD) {
			fillDraftCards(current, cards)
			return
		}
		val machine = QuickHandMachine(config ?: return)
		applyInputs(machine.withCards(current.inputs, QuickInput.FillCards(owner, cards)))
	}

	fun clearCards(owner: CardOwner) {
		selectCards(owner, emptyList())
	}

	fun editActions() {
		val current = editing ?: return
		if (current.isSaving) return
		applyInputs(QuickHandMachine(config ?: return).reopenActions(current.inputs))
	}

	private fun applyFinishInput(input: QuickInput) {
		val current = editing ?: return
		if (current.isSaving) return
		applyInputs(QuickHandMachine(config ?: return).appendFinish(current.inputs, input))
	}

	fun save() {
		val current = editing ?: return
		val currentConfig = config ?: return
		if (!current.canSave) return
		val record = QuickHandMachine(currentConfig).build(current.inputs, HandEvaluator::calculateShowdown) ?: return
		_state.update { (it as? QuickRecordState.Editing)?.copy(isSaving = true) ?: it }
		viewModelScope.launch {
			try {
				saveHandAndUpdateStacks(record.copy(resultLabel = buildResultLabel(record)))
				_effect.emit(QuickRecordEffect.SaveSuccess)
			} catch (e: Exception) {
				Logger.e("quickRecord save failed - ${e.message}", e)
				_state.update { (it as? QuickRecordState.Editing)?.copy(isSaving = false) ?: it }
				_effect.emit(QuickRecordEffect.SaveError)
			}
		}
	}

	private suspend fun buildResultLabel(hand: HandRecord): String {
		val ranking = hand.heroRanking?.takeIf { it != HandRanking.WIN_BY_FOLD }
		val rankingText = ranking?.let { getString(it.stringRes()) }.orEmpty()
		val resultRes = hand.resolvedHeroResultType.resultStringRes(rankingText.isNotEmpty())
		return if (rankingText.isNotEmpty()) getString(resultRes, rankingText) else getString(resultRes)
	}

	fun requestExit() {
		val current = editing
		if (current?.showsFinishScreen == true) {
			if (!current.isSaving) setFinishOpen(false)
			return
		}
		if (current == null || (current.inputs.isEmpty() && current.draft.isEmpty())) {
			viewModelScope.launch { _effect.emit(QuickRecordEffect.NavigateBack) }
		} else {
			_modalEffect.update { QuickRecordModalEffect.ConfirmExit }
		}
	}

	fun confirmExit() {
		dismissModal()
		viewModelScope.launch { _effect.emit(QuickRecordEffect.NavigateBack) }
	}

	fun showGuide() {
		if (editing == null) return
		_modalEffect.update { QuickRecordModalEffect.ShowGuide }
	}

	fun dismissModal() {
		_modalEffect.update { QuickRecordModalEffect.Idle }
	}

	private companion object {
		const val HOLE_CARD_COUNT = 2
	}
}

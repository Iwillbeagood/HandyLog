package com.hand.log.record.quick.contract

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import com.hand.log.domain.model.ActionType
import com.hand.log.domain.model.Position
import com.hand.log.domain.model.quick.CardOwner
import com.hand.log.domain.model.quick.QuickFinish
import com.hand.log.domain.model.quick.QuickHandSnapshot
import com.hand.log.domain.model.quick.QuickInput
import com.hand.log.domain.model.quick.QuickPending
import com.hand.log.domain.model.quick.QuickPhase
import com.hand.log.domain.model.Street
import com.hand.log.record.quick.model.EditorLine
import com.hand.log.record.quick.model.HandKey
import com.hand.log.record.quick.model.HandKeyboardLayout

@Stable
internal sealed interface QuickRecordState {

	val finishScreen: QuickFinishForm?
		get() = null

	val showsFinishScreen: Boolean
		get() = finishScreen != null

	@Immutable
	data object Loading : QuickRecordState

	@Immutable
	data class Editing(
		val blindsLabel: String?,
		val stackLabel: String?,
		val isTournament: Boolean = false,
		val snapshot: QuickHandSnapshot,
		val lines: List<EditorLine>,
		val inputs: List<QuickInput> = emptyList(),
		val isSaving: Boolean = false,
		val draft: List<HandKey> = emptyList(),
		val history: List<DraftEntry> = emptyList(),
		val legalActions: Set<ActionType> = emptySet(),
		val selectablePositions: Set<Position> = emptySet(),
		val streetCloseTarget: StreetCloseTarget? = null,
		val isDraftCommittable: Boolean = false,
		val hasCardConflict: Boolean = false,
		val isFinishOpen: Boolean = false,
	) : QuickRecordState {

		val heroPosition: Position?
			get() = snapshot.heroPosition

		val heroLabel: String?
			get() = heroPosition?.label

		val positionRows: List<List<HandKey>>
			get() = HandKeyboardLayout.positionRows(snapshot.positions)

		val finish: QuickFinish?
			get() = snapshot.finish

		val isFinishing: Boolean
			get() = finish != null

		val heroPositionOptions: List<String>
			get() = if (finish?.canSelectHeroPosition == true) {
				snapshot.involvedPositions.map { it.label }
			} else {
				emptyList()
			}

		val showFinishCards: Boolean
			get() = finish?.heroPosition != null

		val showBoard: Boolean
			get() = finish?.board?.isNotEmpty() == true

		val finishForm: QuickFinishForm?
			get() = finish?.let {
				QuickFinishForm(
					heroPositionOptions = heroPositionOptions,
					heroPosition = it.heroPosition,
					heroCards = it.heroCards,
					showdown = it.showdown,
					board = it.board,
					showCards = showFinishCards,
					showBoard = showBoard,
					canSave = canSave,
				)
			}

		override val finishScreen: QuickFinishForm?
			get() = finishForm?.takeIf { isFinishOpen }

		val heroSelectablePositions: List<Position>
			get() = snapshot.involvedPositions.ifEmpty { snapshot.positions }

		val heroKey: HandKey?
			get() = heroPosition?.let { HandKey.Pos(it) }

		val amountInChips: Boolean
			get() = snapshot.amountInChips

		val actorKey: HandKey?
			get() {
				draft.lastOrNull { it is HandKey.Pos }?.let { return it }
				val pending = snapshot.pending
				val isChosen = pending !is QuickPending.Actor || pending.isChosen
				return snapshot.actorPosition?.takeIf { isChosen }?.let { HandKey.Pos(it) }
			}

		val canEditSetup: Boolean
			get() = !isSaving

		val canSelectHero: Boolean
			get() = !isSaving && snapshot.positions.isNotEmpty()

		val canUndo: Boolean
			get() = inputs.isNotEmpty() && !isSaving

		val canSave: Boolean
			get() = snapshot.canSave && !isSaving

		val sizedAsMultiple: Boolean
			get() = snapshot.isBetOpen

		val needsPositionFirst: Boolean
			get() = snapshot.phase == QuickPhase.ACTION &&
				snapshot.street == Street.PREFLOP &&
				draft.none { it is HandKey.Pos }

		val draftCardPosition: Position?
			get() = when {
				snapshot.phase == QuickPhase.HERO_CARDS -> heroPosition
				snapshot.phase == QuickPhase.ACTION && snapshot.street == Street.PREFLOP ->
					draft.filterIsInstance<HandKey.Pos>().lastOrNull()?.position
				else -> null
			}

		val draftCardOwner: CardOwner?
			get() = when (snapshot.phase) {
				QuickPhase.BOARD -> CardOwner.Board.takeIf { snapshot.boardSlots > 0 }
				QuickPhase.HERO_CARDS -> CardOwner.Hero
				QuickPhase.ACTION -> draftCardPosition?.let { position ->
					if (position == heroPosition) {
						CardOwner.Hero
					} else {
						snapshot.opponentHands.find { it.position == position }?.let {
							CardOwner.Opponent(
								it.position,
							)
						}
					}
				}
				QuickPhase.REVIEW -> null
			}

		val enabledKeys: Set<HandKey>
			get() = buildSet {
				if (isSaving) return@buildSet
				if (draft.isNotEmpty() || inputs.isNotEmpty()) add(HandKey.Backspace)
				if (isDraftCommittable || hasCardConflict || (draft.isEmpty() && (QuickInput.NextLine in snapshot.allowed || streetCloseTarget != null))) {
					add(
						HandKey.NextLine,
					)
				}
				if (QuickInput.ToggleAmountUnit in snapshot.allowed) add(HandKey.AmountUnit)
				if (draftCardOwner != null) add(HandKey.CardPicker)
				when (snapshot.phase) {
					QuickPhase.BOARD -> addAll(HandKeyboardLayout.cardKeys)
					QuickPhase.ACTION, QuickPhase.HERO_CARDS -> {
						selectablePositions.forEach { add(HandKey.Pos(it)) }
						if (needsPositionFirst) return@buildSet
						legalActions.forEach { add(HandKey.Act(it)) }
						val isPostflopAction = snapshot.phase == QuickPhase.ACTION && snapshot.street != Street.PREFLOP
						addAll(
							HandKeyboardLayout.lineKeys.filterNot {
								it is HandKey.Act || (isPostflopAction && (it is HandKey.Face || it is HandKey.SuitKey))
							},
						)
						if (snapshot.street != Street.PREFLOP) addAll(HandKeyboardLayout.potSizeKeys)
					}
					QuickPhase.REVIEW -> Unit
				}
			}
	}
}

@Immutable
internal data class DraftEntry(
	val before: List<QuickInput>,
	val after: List<QuickInput>,
	val tokens: List<HandKey>,
)

package com.hand.log.preflop.drill.contract

import com.hand.log.domain.model.Position
import com.hand.log.domain.model.preflop.ChartDrillResult
import com.hand.log.domain.model.preflop.DrillCellResult
import com.hand.log.domain.model.preflop.PreflopAction
import com.hand.log.domain.model.preflop.PreflopChart
import com.hand.log.domain.model.preflop.PreflopGrid
import com.hand.log.domain.model.preflop.PreflopHand
import com.hand.log.domain.model.preflop.PreflopStack

internal sealed interface DrillBrush {
	data class Paint(val action: PreflopAction) : DrillBrush
	data object Erase : DrillBrush
}

internal enum class DrillMode { RANDOM, SEQUENTIAL }

internal sealed interface DrillLine {
	data class Row(val index: Int) : DrillLine
	data class Column(val index: Int) : DrillLine
	data object Pairs : DrillLine

	val hands: List<PreflopHand>
		get() = when (this) {
			is Row -> PreflopGrid.rows[index]
			is Column -> PreflopGrid.rows.map { it[index] }
			Pairs -> PreflopGrid.rows.mapIndexed { i, row -> row[i] }
		}
}

internal data class DrillLineChecks(
	val rows: List<Boolean>,
	val columns: List<Boolean>,
	val pairs: Boolean,
)

internal data class DrillProgress(
	val current: Int,
	val total: Int,
) {
	val isLast: Boolean get() = current >= total

	val ratio: Float get() = current.toFloat() / total
}

internal data class DrillSummaryItem(
	val hero: Position,
	val accuracyPct: Int,
) {
	val accuracyRatio: Float get() = accuracyPct / 100f
}

internal data class DrillSpot(
	val stack: PreflopStack,
	val hero: Position,
	val answer: PreflopChart,
)

internal sealed interface DrillPhase {
	data object Setup : DrillPhase
	data class Painting(val spot: DrillSpot) : DrillPhase
	data class Graded(val spot: DrillSpot, val result: ChartDrillResult) : DrillPhase
	data class Summary(val stack: PreflopStack, val items: List<DrillSummaryItem>) : DrillPhase {
		val averageAccuracyPct: Int get() = if (items.isEmpty()) {
			0
		} else {
			items.sumOf {
				it.accuracyPct
			} / items.size
		}
	}
}

internal data class DrillCell(
	val action: PreflopAction?,
	val mark: DrillCellResult? = null,
)

internal data class DrillStrokePreview(
	val hand: PreflopHand,
	val row: Int,
	val col: Int,
	val action: PreflopAction?,
)

internal data class PreflopChartDrillState(
	val phase: DrillPhase = DrillPhase.Setup,
	val selectedStack: PreflopStack = PreflopStack.FREE,
	val selectedMode: DrillMode = DrillMode.RANDOM,
	val sequence: List<DrillSpot> = emptyList(),
	val sequenceIndex: Int = 0,
	val sequenceResults: Map<Position, ChartDrillResult> = emptyMap(),
	val isPro: Boolean = false,
	val painted: Map<String, PreflopAction> = emptyMap(),
	val checkedLines: Set<DrillLine> = emptySet(),
	val brush: PreflopAction? = null,
	val strokeHand: PreflopHand? = null,
	val showAnswer: Boolean = false,
	val isLoading: Boolean = true,
) {
	val stackOptions: List<PreflopStack> get() = PreflopStack.entries

	val modeOptions: List<DrillMode> get() = DrillMode.entries

	val progress: DrillProgress?
		get() = if (sequence.isEmpty()) null else DrillProgress(sequenceIndex + 1, sequence.size)

	val spotProgress: DrillProgress? get() = progress?.takeIf { spot != null }

	val isLastSpot: Boolean get() = progress?.isLast == true

	val lockedStacks: Set<PreflopStack>
		get() = if (isPro) {
			emptySet()
		} else {
			stackOptions.filterTo(
				mutableSetOf(),
			) { it != PreflopStack.FREE }
		}

	val spot: DrillSpot?
		get() = when (val p = phase) {
			DrillPhase.Setup, is DrillPhase.Summary -> null
			is DrillPhase.Painting -> p.spot
			is DrillPhase.Graded -> p.spot
		}

	val palette: List<PreflopAction> get() = spot?.answer?.presentActions.orEmpty()

	val canPickBrush: Boolean get() = palette.size > 1

	val canReset: Boolean get() = painted.isNotEmpty()

	val gridCells: Map<String, DrillCell>
		get() = when (val p = phase) {
			is DrillPhase.Graded -> p.result.cells.mapValues { (hand, mark) ->
				val action = if (mark == DrillCellResult.MISSED) p.spot.answer.actions[hand] else painted[hand]
				DrillCell(action, mark)
			}
			else -> painted.mapValues { DrillCell(it.value) }
		}

	val lineChecks: DrillLineChecks
		get() = DrillLineChecks(
			rows = PreflopGrid.rows.indices.map { DrillLine.Row(it) in checkedLines },
			columns = PreflopGrid.rows.indices.map { DrillLine.Column(it) in checkedLines },
			pairs = DrillLine.Pairs in checkedLines,
		)

	val strokePreview: DrillStrokePreview?
		get() {
			val hand = strokeHand ?: return null
			val row = PreflopGrid.rows.indexOfFirst { hand in it }
			if (row < 0) return null
			return DrillStrokePreview(hand, row, PreflopGrid.rows[row].indexOf(hand), painted[hand.notation])
		}
}

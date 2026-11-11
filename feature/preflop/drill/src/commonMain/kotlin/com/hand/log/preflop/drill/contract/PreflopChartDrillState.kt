package com.hand.log.preflop.drill.contract

import com.hand.log.domain.model.Position
import com.hand.log.domain.model.preflop.ChartDrillResult
import com.hand.log.domain.model.preflop.DrillCellResult
import com.hand.log.domain.model.preflop.PreflopAction
import com.hand.log.domain.model.preflop.PreflopChart
import com.hand.log.domain.model.preflop.PreflopStack

internal sealed interface DrillBrush {
	data class Paint(val action: PreflopAction) : DrillBrush
	data object Erase : DrillBrush
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
}

internal data class DrillCell(
	val action: PreflopAction?,
	val mark: DrillCellResult? = null,
)

internal data class PreflopChartDrillState(
	val phase: DrillPhase = DrillPhase.Setup,
	val selectedStack: PreflopStack = PreflopStack.FREE,
	val isPro: Boolean = false,
	val painted: Map<String, PreflopAction> = emptyMap(),
	val brush: DrillBrush? = null,
	val showAnswer: Boolean = false,
	val isLoading: Boolean = true,
) {
	val stackOptions: List<PreflopStack> get() = PreflopStack.entries

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
			DrillPhase.Setup -> null
			is DrillPhase.Painting -> p.spot
			is DrillPhase.Graded -> p.spot
		}

	val palette: List<PreflopAction> get() = spot?.answer?.presentActions.orEmpty()

	val brushOptions: List<DrillBrush> get() = palette.map { DrillBrush.Paint(it) } + DrillBrush.Erase

	val canReset: Boolean get() = painted.isNotEmpty()

	val gridCells: Map<String, DrillCell>
		get() = when (val p = phase) {
			is DrillPhase.Graded -> p.result.cells.mapValues { (hand, mark) ->
				val action = if (mark == DrillCellResult.MISSED) p.spot.answer.actions[hand] else painted[hand]
				DrillCell(action, mark)
			}
			else -> painted.mapValues { DrillCell(it.value) }
		}
}

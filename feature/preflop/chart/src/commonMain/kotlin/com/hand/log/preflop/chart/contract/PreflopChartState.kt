package com.hand.log.preflop.chart.contract

import com.hand.log.domain.model.Position
import com.hand.log.domain.model.preflop.PreflopChart
import com.hand.log.domain.model.preflop.PreflopPositions
import com.hand.log.domain.model.preflop.PreflopScenario
import com.hand.log.domain.model.preflop.PreflopStack

internal data class PreflopChartState(
	val stack: PreflopStack = PreflopStack.BB100,
	val scenario: PreflopScenario = PreflopScenario.RFI,
	val hero: Position = Position.BTN,
	val villain: Position? = null,
	val stackOptions: List<PreflopStack> = PreflopStack.entries,
	val scenarioOptions: List<PreflopScenario> = PreflopScenario.entries,
	val heroOptions: List<List<Position>> = PreflopPositions.order.map { listOf(it) },
	val villainOptions: List<List<Position>> = emptyList(),
	val chart: PreflopChart = PreflopChart.EMPTY,
	val isLoading: Boolean = true,
	val isPro: Boolean = true,
) {
	val lockedStacks: Set<PreflopStack>
		get() = if (isPro) {
			emptySet()
		} else {
			stackOptions.filterTo(
				mutableSetOf(),
			) { it != PreflopStack.FREE }
		}

	val isFixedMatchup: Boolean get() = scenario == PreflopScenario.VS_LIMP

	val selectedHeroGroup: List<Position>
		get() = heroOptions.find { hero in it } ?: heroOptions.firstOrNull().orEmpty()

	val showVillainChips: Boolean get() = villain != null && villainOptions.isNotEmpty()

	val selectedVillainGroup: List<Position>
		get() = villainOptions.find { it.contains(villain) } ?: villainOptions.firstOrNull().orEmpty()
}

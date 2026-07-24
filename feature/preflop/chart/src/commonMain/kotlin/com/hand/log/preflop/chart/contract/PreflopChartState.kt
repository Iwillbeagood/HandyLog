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
	val heroOptions: List<Position> = PreflopPositions.order,
	val villainOptions: List<Position> = emptyList(),
	val chart: PreflopChart = PreflopChart.EMPTY,
)

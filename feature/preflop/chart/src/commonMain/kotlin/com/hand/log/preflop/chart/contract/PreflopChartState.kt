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
	// 병합된 포지션 그룹 목록 — 하나의 칩이 여러 포지션(예: UTG/UTG+1)을 나타낼 수 있다.
	val heroOptions: List<List<Position>> = PreflopPositions.order.map { listOf(it) },
	val villainOptions: List<List<Position>> = emptyList(),
	val chart: PreflopChart = PreflopChart.EMPTY,
	val isLoading: Boolean = true,
)

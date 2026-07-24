package com.hand.log.preflop.chart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hand.log.domain.model.Position
import com.hand.log.domain.model.preflop.PreflopChart
import com.hand.log.domain.model.preflop.PreflopChartQuery
import com.hand.log.domain.model.preflop.PreflopPositions
import com.hand.log.domain.model.preflop.PreflopScenario
import com.hand.log.domain.model.preflop.PreflopStack
import com.hand.log.preflop.chart.contract.PreflopChartState
import com.hand.log.preflop.chart.data.PreflopChartRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class PreflopChartViewModel(
	private val repository: PreflopChartRepository,
) : ViewModel() {

	private var charts: Map<PreflopChartQuery, PreflopChart> = emptyMap()

	private val _state = MutableStateFlow(
		resolve(PreflopStack.BB100, PreflopScenario.RFI, Position.BTN, null),
	)
	val state: StateFlow<PreflopChartState> = _state

	init {
		viewModelScope.launch {
			charts = repository.load()
			_state.update { resolve(it.stack, it.scenario, it.hero, it.villain) }
		}
	}

	fun selectStack(stack: PreflopStack) {
		_state.update { resolve(stack, it.scenario, it.hero, it.villain) }
	}

	fun selectScenario(scenario: PreflopScenario) {
		_state.update { resolve(it.stack, scenario, it.hero, it.villain) }
	}

	fun selectHero(hero: Position) {
		_state.update { resolve(it.stack, it.scenario, hero, it.villain) }
	}

	fun selectVillain(villain: Position) {
		_state.update { resolve(it.stack, it.scenario, it.hero, villain) }
	}

	/** 선택값을 유효 범위로 정규화한 뒤 로드된 카탈로그에서 차트를 찾아 State 를 구성한다. */
	private fun resolve(
		stack: PreflopStack,
		scenario: PreflopScenario,
		hero: Position,
		desiredVillain: Position?,
	): PreflopChartState {
		// 포지션은 시나리오와 무관하게 고정(먼저 선택하는 흐름). 데이터 없는 조합은 빈 차트로 처리된다.
		val heroOptions = PreflopPositions.order
		val resolvedHero = if (hero in heroOptions) hero else Position.BTN

		val villainOptions = when (scenario) {
			PreflopScenario.RFI -> emptyList()
			PreflopScenario.FACING_RFI -> PreflopPositions.raisersBefore(resolvedHero)
			PreflopScenario.VS_3BET -> PreflopPositions.threeBettorsAfter(resolvedHero)
		}
		val resolvedVillain = when {
			villainOptions.isEmpty() -> null
			desiredVillain != null && desiredVillain in villainOptions -> desiredVillain
			else -> villainOptions.first()
		}

		val chart = charts[PreflopChartQuery(stack, scenario, resolvedHero, resolvedVillain)]
			?: PreflopChart.EMPTY

		return PreflopChartState(
			stack = stack,
			scenario = scenario,
			hero = resolvedHero,
			villain = resolvedVillain,
			heroOptions = heroOptions,
			villainOptions = villainOptions,
			chart = chart,
		)
	}
}

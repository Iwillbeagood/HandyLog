package com.hand.log.preflop.chart.navigation

import androidx.compose.runtime.LaunchedEffect
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.hand.log.domain.model.Position
import com.hand.log.domain.model.preflop.PreflopScenario
import com.hand.log.domain.model.preflop.PreflopSelection
import com.hand.log.domain.model.preflop.PreflopStack
import com.hand.log.navigation.navigation.PreflopChart
import com.hand.log.navigation.navigation.PreflopChartTable
import com.hand.log.preflop.chart.PreflopChartRoute
import com.hand.log.preflop.chart.PreflopChartTableRoute
import com.hand.log.preflop.chart.PreflopChartViewModel
import org.koin.compose.viewmodel.koinViewModel

fun EntryProviderScope<NavKey>.preflopChartNavGraph() {
	entry<PreflopChart> { key ->
		val viewModel: PreflopChartViewModel = koinViewModel()
		key.toSelectionOrNull()?.let { initial ->
			LaunchedEffect(initial) { viewModel.openSpot(initial) }
		}
		PreflopChartRoute(viewModel = viewModel)
	}

	entry<PreflopChartTable> { key ->
		val viewModel: PreflopChartViewModel = koinViewModel()
		key.toSelectionOrNull()?.let { initial ->
			LaunchedEffect(initial) { viewModel.openSpot(initial) }
		}
		PreflopChartTableRoute(viewModel = viewModel)
	}
}

/** NavKey 의 문자열 인자를 초기 선택으로 파싱. 핵심 값(스택/시나리오/히어로)이 없으면 null(저장된 선택 사용). */
private fun PreflopChart.toSelectionOrNull(): PreflopSelection? {
	val parsedStack = stack?.let { runCatching { PreflopStack.valueOf(it) }.getOrNull() } ?: return null
	val parsedScenario = scenario?.let {
		runCatching { PreflopScenario.valueOf(it) }.getOrNull()
	} ?: return null
	val parsedHero = hero?.let { runCatching { Position.valueOf(it) }.getOrNull() } ?: return null
	val parsedVillain = villain?.let { runCatching { Position.valueOf(it) }.getOrNull() }
	return PreflopSelection(parsedStack, parsedScenario, parsedHero, parsedVillain)
}

private fun PreflopChartTable.toSelectionOrNull(): PreflopSelection? {
	val parsedStack = stack?.let { runCatching { PreflopStack.valueOf(it) }.getOrNull() } ?: return null
	val parsedHero = hero?.let { runCatching { Position.valueOf(it) }.getOrNull() } ?: return null
	return PreflopSelection(parsedStack, PreflopScenario.RFI, parsedHero, null)
}

package com.hand.log.preflop.chart

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hand.log.navigation.interop.LocalNavigateActionInterop

@Composable
internal fun PreflopChartRoute(
	viewModel: PreflopChartViewModel,
) {
	val state by viewModel.state.collectAsStateWithLifecycle()
	val navAction = LocalNavigateActionInterop.current

	PreflopChartScreen(
		state = state,
		onBack = navAction::popBackStack,
		onStackSelect = viewModel::selectStack,
		onScenarioSelect = viewModel::selectScenario,
		onHeroSelect = viewModel::selectHero,
		onVillainSelect = viewModel::selectVillain,
	)
}

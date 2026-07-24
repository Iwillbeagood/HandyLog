package com.hand.log.preflop.chart.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.hand.log.navigation.navigation.PreflopChart
import com.hand.log.preflop.chart.PreflopChartRoute
import com.hand.log.preflop.chart.PreflopChartViewModel
import org.koin.compose.viewmodel.koinViewModel

fun EntryProviderScope<NavKey>.preflopChartNavGraph() {
	entry<PreflopChart> {
		val viewModel: PreflopChartViewModel = koinViewModel()
		PreflopChartRoute(viewModel = viewModel)
	}
}

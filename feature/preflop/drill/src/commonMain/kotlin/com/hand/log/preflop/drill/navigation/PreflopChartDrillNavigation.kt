package com.hand.log.preflop.drill.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.hand.log.navigation.navigation.PreflopChartDrill
import com.hand.log.preflop.drill.PreflopChartDrillRoute

fun EntryProviderScope<NavKey>.preflopChartDrillNavGraph() {
	entry<PreflopChartDrill> {
		PreflopChartDrillRoute()
	}
}

package com.hand.log.preflop.chart.contract

import com.hand.log.domain.model.ProFeature

internal sealed interface PreflopChartModalEffect {
	data object Idle : PreflopChartModalEffect
	data class ShowPaywall(val feature: ProFeature) : PreflopChartModalEffect
}

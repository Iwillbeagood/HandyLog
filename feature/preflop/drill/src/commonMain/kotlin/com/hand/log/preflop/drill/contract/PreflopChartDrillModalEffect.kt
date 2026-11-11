package com.hand.log.preflop.drill.contract

import com.hand.log.domain.model.ProFeature

internal sealed interface PreflopChartDrillModalEffect {
	data object Idle : PreflopChartDrillModalEffect
	data class ShowPaywall(val feature: ProFeature) : PreflopChartDrillModalEffect
}

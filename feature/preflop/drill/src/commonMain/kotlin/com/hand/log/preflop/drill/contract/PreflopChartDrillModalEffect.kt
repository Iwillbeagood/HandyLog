package com.hand.log.preflop.drill.contract

import com.hand.log.domain.model.ProFeature
import com.hand.log.domain.model.preflop.PreflopAction

internal sealed interface PreflopChartDrillModalEffect {
	data object Idle : PreflopChartDrillModalEffect
	data class ShowPaywall(val feature: ProFeature) : PreflopChartDrillModalEffect
	data class ShowBrushPicker(
		val actions: List<PreflopAction>,
		val selected: PreflopAction?,
	) : PreflopChartDrillModalEffect
}

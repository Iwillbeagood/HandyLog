package com.hand.log.settings.betsize.contract

import androidx.compose.runtime.Immutable

@Immutable
internal data class BetSizeState(
	val preflopPresets: List<Double> = emptyList(),
	val postflopPresets: List<Int> = emptyList(),
	val canAddPreflop: Boolean = true,
	val canAddPostflop: Boolean = true,
)

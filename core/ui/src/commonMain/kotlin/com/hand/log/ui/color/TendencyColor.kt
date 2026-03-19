package com.hand.log.ui.color

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.hand.log.domain.model.PlayerTendency

private val TendencyColors = mapOf(
	PlayerTendency.TIGHT to Color(0xFF4A90D9),
	PlayerTendency.LOOSE to Color(0xFFE8943A),
	PlayerTendency.AGGRESSIVE to Color(0xFFE84040),
	PlayerTendency.PASSIVE to Color(0xFF7B8FA0),
	PlayerTendency.NIT to Color(0xFF6BC5E8),
	PlayerTendency.MANIAC to Color(0xFFD94ABB),
	PlayerTendency.UNKNOWN to Color(0xFF808897),
)

@Composable
fun PlayerTendency.tendencyColor(): Color {
	return TendencyColors[this] ?: Color(0xFF808897)
}

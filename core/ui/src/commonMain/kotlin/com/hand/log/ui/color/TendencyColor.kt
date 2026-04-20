package com.hand.log.ui.color

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.hand.log.domain.model.PlayerTendency

private val TendencyColors = mapOf(
	PlayerTendency.TIGHT_AGGRESSIVE to Color(0xFFE84040),
	PlayerTendency.TIGHT_PASSIVE to Color(0xFF4A90D9),
	PlayerTendency.LOOSE_AGGRESSIVE to Color(0xFFE8943A),
	PlayerTendency.LOOSE_PASSIVE to Color(0xFF7B8FA0),
	PlayerTendency.SHARK to Color(0xFF2E7D32),
	PlayerTendency.REGULAR to Color(0xFF5C6BC0),
	PlayerTendency.FISH to Color(0xFFFF8A65),
	PlayerTendency.UNKNOWN to Color(0xFF808897),
)

@Composable
fun PlayerTendency.tendencyColor(): Color {
	return TendencyColors[this] ?: Color(0xFF808897)
}

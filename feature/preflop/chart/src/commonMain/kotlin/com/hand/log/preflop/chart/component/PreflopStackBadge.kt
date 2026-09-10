package com.hand.log.preflop.chart.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.hand.log.designsystem.etc.ThemePreview
import com.hand.log.designsystem.etc.ThemePreviews
import com.hand.log.designsystem.theme.HandyTheme
import com.hand.log.domain.model.preflop.PreflopStack

@Composable
internal fun PreflopStackBadge(stack: PreflopStack) {
	val colors = HandyTheme.colorScheme
	Text(
		text = stack.label,
		style = HandyTheme.typography.bold14,
		color = colors.primary,
		modifier = Modifier
			.clip(RoundedCornerShape(10.dp))
			.background(colors.primary.copy(alpha = 0.15f))
			.padding(horizontal = 12.dp, vertical = 6.dp),
	)
}

@ThemePreviews
@Composable
private fun PreflopStackBadgePreview() {
	ThemePreview {
		PreflopStackBadge(stack = PreflopStack.BB100)
	}
}

@ThemePreviews
@Composable
private fun PreflopStackBadgeOnlinePreview() {
	ThemePreview {
		PreflopStackBadge(stack = PreflopStack.ONLINE)
	}
}

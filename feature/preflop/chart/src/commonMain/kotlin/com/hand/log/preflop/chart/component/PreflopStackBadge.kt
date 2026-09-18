package com.hand.log.preflop.chart.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.hand.log.designsystem.etc.ThemePreview
import com.hand.log.designsystem.etc.ThemePreviews
import com.hand.log.designsystem.theme.HandyTheme
import com.hand.log.domain.model.preflop.PreflopStack
import handylog.core.res.generated.resources.Res
import handylog.core.res.generated.resources.chevron_down
import org.jetbrains.compose.resources.painterResource

@Composable
internal fun PreflopStackBadge(
	stack: PreflopStack,
	onClick: (() -> Unit)? = null,
) {
	val colors = HandyTheme.colorScheme
	Row(
		modifier = Modifier
			.clip(RoundedCornerShape(10.dp))
			.background(colors.primary.copy(alpha = 0.15f))
			.then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
			.padding(horizontal = 12.dp, vertical = 6.dp),
		verticalAlignment = Alignment.CenterVertically,
		horizontalArrangement = Arrangement.spacedBy(4.dp),
	) {
		Text(
			text = stack.label,
			style = HandyTheme.typography.bold14,
			color = colors.primary,
		)
		if (onClick != null) {
			Icon(
				painter = painterResource(Res.drawable.chevron_down),
				contentDescription = null,
				modifier = Modifier.size(14.dp),
				tint = colors.primary,
			)
		}
	}
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
private fun PreflopStackBadgeClickablePreview() {
	ThemePreview {
		PreflopStackBadge(stack = PreflopStack.ONLINE, onClick = {})
	}
}

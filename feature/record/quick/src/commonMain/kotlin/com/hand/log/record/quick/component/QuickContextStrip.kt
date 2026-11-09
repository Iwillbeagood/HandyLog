package com.hand.log.record.quick.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.hand.log.designsystem.etc.ThemePreview
import com.hand.log.designsystem.etc.ThemePreviews
import com.hand.log.designsystem.etc.clickableSingle
import com.hand.log.designsystem.theme.HandyTheme
import handylog.core.res.generated.resources.Res
import handylog.core.res.generated.resources.chevron_down
import handylog.core.res.generated.resources.crown
import handylog.core.res.generated.resources.pencil
import handylog.core.res.generated.resources.quick_record_hero
import handylog.core.res.generated.resources.quick_record_hero_select
import handylog.core.res.generated.resources.quick_record_hero_stack
import handylog.core.res.generated.resources.quick_record_setup_hint
import handylog.core.res.generated.resources.quick_record_tournament
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun QuickContextStrip(
	blindsLabel: String?,
	isTournament: Boolean,
	stackLabel: String?,
	heroLabel: String?,
	enabled: Boolean,
	onSetupClick: () -> Unit,
	onHeroClick: () -> Unit,
	modifier: Modifier = Modifier,
) {
	val summary = listOfNotNull(
		blindsLabel ?: stringResource(Res.string.quick_record_tournament).takeIf { isTournament },
		stackLabel?.let { stringResource(Res.string.quick_record_hero_stack, it) },
	).joinToString(SEPARATOR).ifEmpty { stringResource(Res.string.quick_record_setup_hint) }
	Row(
		modifier = modifier.fillMaxWidth(),
		verticalAlignment = Alignment.CenterVertically,
		horizontalArrangement = Arrangement.spacedBy(8.dp),
	) {
		StripButton(
			text = summary,
			icon = Res.drawable.pencil,
			color = HandyTheme.colorScheme.textSecondary,
			iconColor = HandyTheme.colorScheme.primary,
			enabled = enabled,
			onClick = onSetupClick,
			modifier = Modifier.weight(1f),
		)
		StripButton(
			text = heroLabel?.let { stringResource(Res.string.quick_record_hero, it) }
				?: stringResource(Res.string.quick_record_hero_select),
			icon = Res.drawable.chevron_down,
			leadingIcon = Res.drawable.crown,
			color = HandyTheme.colorScheme.gold,
			iconColor = HandyTheme.colorScheme.gold,
			enabled = enabled,
			onClick = onHeroClick,
		)
	}
}

@Composable
private fun StripButton(
	text: String,
	icon: DrawableResource,
	color: Color,
	iconColor: Color,
	enabled: Boolean,
	onClick: () -> Unit,
	modifier: Modifier = Modifier,
	leadingIcon: DrawableResource? = null,
) {
	Row(
		modifier = modifier
			.clip(RoundedCornerShape(10.dp))
			.background(HandyTheme.colorScheme.secondary)
			.clickableSingle(enabled = enabled, onClick = onClick)
			.padding(horizontal = 12.dp, vertical = 10.dp),
		verticalAlignment = Alignment.CenterVertically,
		horizontalArrangement = Arrangement.spacedBy(4.dp),
	) {
		leadingIcon?.let {
			Icon(
				painter = painterResource(it),
				contentDescription = null,
				tint = iconColor,
				modifier = Modifier.size(12.dp),
			)
		}
		Text(
			text = text,
			style = HandyTheme.typography.medium12,
			color = color,
			maxLines = 1,
			overflow = TextOverflow.Ellipsis,
			modifier = Modifier.weight(1f, fill = false),
		)
		Icon(
			painter = painterResource(icon),
			contentDescription = null,
			tint = iconColor,
			modifier = Modifier.size(12.dp),
		)
	}
}

private const val SEPARATOR = " · "

@ThemePreviews
@Composable
private fun QuickContextStripPreview() {
	ThemePreview {
		QuickContextStrip(
			blindsLabel = "500/1,000",
			isTournament = false,
			stackLabel = "100BB",
			heroLabel = "BTN",
			enabled = true,
			onSetupClick = {},
			onHeroClick = {},
			modifier = Modifier.padding(16.dp),
		)
	}
}

@ThemePreviews
@Composable
private fun QuickContextStripHeroUnsetPreview() {
	ThemePreview {
		QuickContextStrip(
			blindsLabel = null,
			isTournament = false,
			stackLabel = null,
			heroLabel = null,
			enabled = true,
			onSetupClick = {},
			onHeroClick = {},
			modifier = Modifier.padding(16.dp),
		)
	}
}

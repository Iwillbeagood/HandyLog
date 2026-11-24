package com.hand.log.preflop.drill.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.ui.unit.dp
import com.hand.log.designsystem.component.modal.HandyBottomSheet
import com.hand.log.designsystem.etc.ThemePreview
import com.hand.log.designsystem.etc.ThemePreviews
import com.hand.log.designsystem.theme.HandyTheme
import com.hand.log.domain.model.preflop.PreflopAction
import com.hand.log.preflop.chart.component.actionColors
import com.hand.log.preflop.chart.component.actionLabelRes
import handylog.core.res.generated.resources.Res
import handylog.core.res.generated.resources.check
import handylog.core.res.generated.resources.chevron_down
import handylog.core.res.generated.resources.preflop_drill_brush_select
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun DrillBrushButton(
	action: PreflopAction?,
	onClick: (() -> Unit)?,
	modifier: Modifier = Modifier,
) {
	val colors = HandyTheme.colorScheme
	Row(
		modifier = modifier
			.fillMaxHeight()
			.clip(RoundedCornerShape(8.dp))
			.background(colors.muted)
			.then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
			.padding(horizontal = 10.dp),
		horizontalArrangement = Arrangement.spacedBy(6.dp),
		verticalAlignment = Alignment.CenterVertically,
	) {
		if (action != null) {
			ActionSwatch(action)
			Text(
				text = stringResource(actionLabelRes(action)),
				style = HandyTheme.typography.bold12,
				color = colors.textPrimary,
				maxLines = 1,
			)
		}
		if (onClick != null) {
			Icon(
				painter = painterResource(Res.drawable.chevron_down),
				contentDescription = stringResource(Res.string.preflop_drill_brush_select),
				tint = colors.textSecondary,
				modifier = Modifier.size(14.dp),
			)
		}
	}
}

@Composable
internal fun DrillBrushSheet(
	actions: List<PreflopAction>,
	selected: PreflopAction?,
	onSelect: (PreflopAction) -> Unit,
	onDismiss: () -> Unit,
) {
	HandyBottomSheet(
		onDismissRequest = onDismiss,
		title = stringResource(Res.string.preflop_drill_brush_select),
	) {
		DrillBrushOptions(actions = actions, selected = selected, onSelect = onSelect)
	}
}

@Composable
private fun DrillBrushOptions(
	actions: List<PreflopAction>,
	selected: PreflopAction?,
	onSelect: (PreflopAction) -> Unit,
) {
	val colors = HandyTheme.colorScheme
	val shape = RoundedCornerShape(8.dp)
	Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
		actions.forEach { action ->
			val isSelected = action == selected
			Row(
				modifier = Modifier
					.fillMaxWidth()
					.clip(shape)
					.background(colors.muted)
					.border(1.5.dp, if (isSelected) colors.textPrimary else Color.Transparent, shape)
					.clickable { onSelect(action) }
					.padding(horizontal = 14.dp, vertical = 14.dp),
				horizontalArrangement = Arrangement.spacedBy(10.dp),
				verticalAlignment = Alignment.CenterVertically,
			) {
				ActionSwatch(action)
				Text(
					text = stringResource(actionLabelRes(action)),
					style = if (isSelected) HandyTheme.typography.bold14 else HandyTheme.typography.medium14,
					color = if (isSelected) colors.textPrimary else colors.textSecondary,
					modifier = Modifier.weight(1f),
				)
				if (isSelected) {
					Icon(
						painter = painterResource(Res.drawable.check),
						contentDescription = null,
						tint = colors.textPrimary,
						modifier = Modifier.size(16.dp),
					)
				}
			}
		}
	}
}

@Composable
private fun ActionSwatch(action: PreflopAction) {
	Box(
		modifier = Modifier
			.size(14.dp)
			.clip(RoundedCornerShape(3.dp))
			.background(actionColors(action).first),
	)
}

private val previewBrushActions = listOf(
	PreflopAction.RAISE_FOLD,
	PreflopAction.RAISE_CALL,
	PreflopAction.RAISE_4BET,
)

@ThemePreviews
@Composable
private fun DrillBrushButtonPreview() {
	ThemePreview {
		Row(modifier = Modifier.padding(16.dp)) {
			DrillBrushButton(action = PreflopAction.RAISE_FOLD, onClick = {})
		}
	}
}

@ThemePreviews
@Composable
private fun DrillBrushButtonSinglePreview() {
	ThemePreview {
		Row(modifier = Modifier.padding(16.dp)) {
			DrillBrushButton(action = PreflopAction.RAISE, onClick = null)
		}
	}
}

@ThemePreviews
@Composable
private fun DrillBrushOptionsPreview() {
	ThemePreview {
		Column(modifier = Modifier.padding(16.dp)) {
			DrillBrushOptions(
				actions = previewBrushActions,
				selected = PreflopAction.RAISE_FOLD,
				onSelect = {},
			)
		}
	}
}

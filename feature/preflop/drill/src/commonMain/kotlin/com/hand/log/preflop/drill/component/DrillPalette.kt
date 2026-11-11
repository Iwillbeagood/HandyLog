package com.hand.log.preflop.drill.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.hand.log.designsystem.etc.ThemePreview
import com.hand.log.designsystem.etc.ThemePreviews
import com.hand.log.designsystem.theme.HandyTheme
import com.hand.log.domain.model.preflop.PreflopAction
import com.hand.log.preflop.chart.component.actionColors
import com.hand.log.preflop.chart.component.actionLabelRes
import com.hand.log.preflop.drill.contract.DrillBrush
import handylog.core.res.generated.resources.Res
import handylog.core.res.generated.resources.preflop_drill_eraser
import handylog.core.res.generated.resources.x
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun DrillPalette(
	options: List<DrillBrush>,
	selected: DrillBrush?,
	onSelect: (DrillBrush) -> Unit,
) {
	FlowRow(
		horizontalArrangement = Arrangement.spacedBy(8.dp),
		verticalArrangement = Arrangement.spacedBy(8.dp),
	) {
		options.forEach { brush ->
			BrushItem(
				brush = brush,
				selected = brush == selected,
				onClick = { onSelect(brush) },
			)
		}
	}
}

@Composable
private fun BrushItem(
	brush: DrillBrush,
	selected: Boolean,
	onClick: () -> Unit,
) {
	val colors = HandyTheme.colorScheme
	val shape = RoundedCornerShape(8.dp)
	Row(
		modifier = Modifier
			.clip(shape)
			.background(colors.muted)
			.border(1.5.dp, if (selected) colors.textPrimary else Color.Transparent, shape)
			.clickable(onClick = onClick)
			.padding(horizontal = 10.dp, vertical = 8.dp),
		horizontalArrangement = Arrangement.spacedBy(6.dp),
		verticalAlignment = Alignment.CenterVertically,
	) {
		when (brush) {
			is DrillBrush.Paint -> {
				Box(
					modifier = Modifier
						.size(14.dp)
						.clip(RoundedCornerShape(3.dp))
						.background(actionColors(brush.action).first),
				)
				BrushLabel(stringResource(actionLabelRes(brush.action)), selected)
			}
			DrillBrush.Erase -> {
				Icon(
					painter = painterResource(Res.drawable.x),
					contentDescription = null,
					tint = colors.textSecondary,
					modifier = Modifier.size(14.dp),
				)
				BrushLabel(stringResource(Res.string.preflop_drill_eraser), selected)
			}
		}
	}
}

@Composable
private fun BrushLabel(text: String, selected: Boolean) {
	Text(
		text = text,
		style = if (selected) HandyTheme.typography.bold12 else HandyTheme.typography.medium12,
		color = if (selected) HandyTheme.colorScheme.textPrimary else HandyTheme.colorScheme.textSecondary,
	)
}

private val previewOptions = listOf(
	DrillBrush.Paint(PreflopAction.RAISE),
	DrillBrush.Paint(PreflopAction.RAISE_BLUFF),
	DrillBrush.Erase,
)

@ThemePreviews
@Composable
private fun DrillPalettePaintSelectedPreview() {
	ThemePreview {
		DrillPalette(
			options = previewOptions,
			selected = DrillBrush.Paint(PreflopAction.RAISE),
			onSelect = {},
		)
	}
}

@ThemePreviews
@Composable
private fun DrillPaletteEraseSelectedPreview() {
	ThemePreview {
		DrillPalette(
			options = previewOptions,
			selected = DrillBrush.Erase,
			onSelect = {},
		)
	}
}

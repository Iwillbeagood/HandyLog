package com.hand.log.preflop.drill.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hand.log.designsystem.component.RegularButton
import com.hand.log.designsystem.etc.ThemePreview
import com.hand.log.designsystem.etc.ThemePreviews
import com.hand.log.designsystem.theme.HandyTheme
import com.hand.log.domain.model.preflop.PreflopAction
import com.hand.log.domain.model.preflop.PreflopHand
import com.hand.log.preflop.drill.contract.DrillBrush
import com.hand.log.preflop.drill.contract.DrillCell
import com.hand.log.preflop.drill.contract.DrillSpot
import handylog.core.res.generated.resources.Res
import handylog.core.res.generated.resources.preflop_drill_grade
import handylog.core.res.generated.resources.preflop_drill_painting_desc
import handylog.core.res.generated.resources.preflop_drill_reset
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun PaintingContent(
	spot: DrillSpot,
	cells: Map<String, DrillCell>,
	brushOptions: List<DrillBrush>,
	brush: DrillBrush?,
	canReset: Boolean,
	onStackClick: () -> Unit,
	onBrushSelect: (DrillBrush) -> Unit,
	onStrokeStart: (PreflopHand) -> Unit,
	onStrokeMove: (PreflopHand) -> Unit,
	onStrokeEnd: () -> Unit,
	onReset: () -> Unit,
	onGrade: () -> Unit,
) {
	val colors = HandyTheme.colorScheme
	Column(modifier = Modifier.fillMaxSize()) {
		Column(
			modifier = Modifier
				.weight(1f)
				.fillMaxWidth()
				.verticalScroll(rememberScrollState())
				.padding(horizontal = 16.dp, vertical = 16.dp),
			verticalArrangement = Arrangement.spacedBy(16.dp),
		) {
			DrillSpotHeader(
				spot = spot,
				description = stringResource(Res.string.preflop_drill_painting_desc),
				onStackClick = onStackClick,
			)
			DrillPalette(
				options = brushOptions,
				selected = brush,
				onSelect = onBrushSelect,
			)
			DrillGrid(
				cells = cells,
				onStrokeStart = onStrokeStart,
				onStrokeMove = onStrokeMove,
				onStrokeEnd = onStrokeEnd,
			)
		}
		Row(
			modifier = Modifier
				.fillMaxWidth()
				.padding(horizontal = 16.dp, vertical = 12.dp),
			horizontalArrangement = Arrangement.spacedBy(8.dp),
		) {
			RegularButton(
				onClick = onReset,
				text = stringResource(Res.string.preflop_drill_reset),
				textStyle = HandyTheme.typography.bold16,
				containerColor = colors.muted,
				contentColor = colors.textPrimary,
				enabled = canReset,
				modifier = Modifier.weight(1f),
			)
			RegularButton(
				onClick = onGrade,
				text = stringResource(Res.string.preflop_drill_grade),
				textStyle = HandyTheme.typography.bold16,
				modifier = Modifier.weight(1f),
			)
		}
	}
}

@ThemePreviews
@Composable
private fun PaintingContentPreview() {
	ThemePreview {
		PaintingContent(
			spot = previewSpot,
			cells = previewPaintedCells,
			brushOptions = previewSpot.answer.presentActions.map { DrillBrush.Paint(it) } + DrillBrush.Erase,
			brush = DrillBrush.Paint(PreflopAction.RAISE),
			canReset = true,
			onStackClick = {},
			onBrushSelect = {},
			onStrokeStart = {},
			onStrokeMove = {},
			onStrokeEnd = {},
			onReset = {},
			onGrade = {},
		)
	}
}

@ThemePreviews
@Composable
private fun PaintingContentEmptyPreview() {
	ThemePreview {
		PaintingContent(
			spot = previewSpot,
			cells = emptyMap(),
			brushOptions = listOf(DrillBrush.Paint(PreflopAction.RAISE), DrillBrush.Erase),
			brush = DrillBrush.Erase,
			canReset = false,
			onStackClick = {},
			onBrushSelect = {},
			onStrokeStart = {},
			onStrokeMove = {},
			onStrokeEnd = {},
			onReset = {},
			onGrade = {},
		)
	}
}

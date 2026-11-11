package com.hand.log.preflop.drill.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import com.hand.log.preflop.drill.contract.DrillStrokePreview
import handylog.core.res.generated.resources.Res
import handylog.core.res.generated.resources.preflop_drill_grade
import handylog.core.res.generated.resources.preflop_drill_painting_desc
import handylog.core.res.generated.resources.preflop_drill_reset
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun PaintingContent(
	spot: DrillSpot,
	cells: Map<String, DrillCell>,
	paintBrushes: List<DrillBrush.Paint>,
	brush: DrillBrush?,
	canReset: Boolean,
	strokePreview: DrillStrokePreview?,
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
				.fillMaxWidth()
				.padding(start = 16.dp, top = 16.dp, end = 16.dp),
			verticalArrangement = Arrangement.spacedBy(12.dp),
		) {
			DrillSpotHeader(
				spot = spot,
				description = stringResource(Res.string.preflop_drill_painting_desc),
				onStackClick = onStackClick,
			)
			DrillPalette(
				paintBrushes = paintBrushes,
				selected = brush,
				onSelect = onBrushSelect,
			)
		}
		DrillGrid(
			cells = cells,
			fillHeight = true,
			strokePreview = strokePreview,
			onStrokeStart = onStrokeStart,
			onStrokeMove = onStrokeMove,
			onStrokeEnd = onStrokeEnd,
			modifier = Modifier
				.weight(1f)
				.padding(start = 8.dp, top = 12.dp, end = 8.dp),
		)
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
			paintBrushes = previewSpot.answer.presentActions.map { DrillBrush.Paint(it) },
			brush = DrillBrush.Paint(PreflopAction.RAISE),
			canReset = true,
			strokePreview = null,
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
			paintBrushes = listOf(DrillBrush.Paint(PreflopAction.RAISE)),
			brush = DrillBrush.Erase,
			canReset = false,
			strokePreview = null,
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
private fun PaintingContentStrokingPreview() {
	ThemePreview {
		PaintingContent(
			spot = previewSpot,
			cells = previewPaintedCells,
			paintBrushes = previewSpot.answer.presentActions.map { DrillBrush.Paint(it) },
			brush = DrillBrush.Paint(PreflopAction.RAISE),
			canReset = true,
			strokePreview = previewStrokePreview,
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

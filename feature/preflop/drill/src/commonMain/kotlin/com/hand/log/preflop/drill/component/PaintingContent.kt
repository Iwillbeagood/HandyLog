package com.hand.log.preflop.drill.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import handylog.core.res.generated.resources.undo
import org.jetbrains.compose.resources.painterResource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import com.hand.log.preflop.drill.contract.DrillCell
import com.hand.log.preflop.drill.contract.DrillLine
import com.hand.log.preflop.drill.contract.DrillLineChecks
import com.hand.log.preflop.drill.contract.DrillStrokePreview
import handylog.core.res.generated.resources.Res
import handylog.core.res.generated.resources.preflop_drill_grade
import handylog.core.res.generated.resources.preflop_drill_reset
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun PaintingContent(
	cells: Map<String, DrillCell>,
	brush: PreflopAction?,
	canPickBrush: Boolean,
	lineChecks: DrillLineChecks,
	canReset: Boolean,
	strokePreview: DrillStrokePreview?,
	onBrushClick: () -> Unit,
	onLineToggle: (DrillLine) -> Unit,
	onStrokeStart: (PreflopHand) -> Unit,
	onStrokeMove: (PreflopHand) -> Unit,
	onStrokeEnd: () -> Unit,
	onReset: () -> Unit,
	onGrade: () -> Unit,
) {
	val colors = HandyTheme.colorScheme
	Column(modifier = Modifier.fillMaxSize()) {
		DrillGrid(
			cells = cells,
			fillHeight = true,
			strokePreview = strokePreview,
			lineChecks = lineChecks,
			onLineToggle = onLineToggle,
			onStrokeStart = onStrokeStart,
			onStrokeMove = onStrokeMove,
			onStrokeEnd = onStrokeEnd,
			modifier = Modifier
				.weight(1f)
				.padding(start = 8.dp, top = 16.dp, end = 8.dp),
		)
		Row(
			modifier = Modifier
				.fillMaxWidth()
				.height(IntrinsicSize.Min)
				.padding(horizontal = 16.dp, vertical = 12.dp),
			horizontalArrangement = Arrangement.spacedBy(8.dp),
		) {
			DrillBrushButton(action = brush, onClick = onBrushClick.takeIf { canPickBrush })
			Box(
				modifier = Modifier
					.fillMaxHeight()
					.aspectRatio(1f)
					.clip(RoundedCornerShape(8.dp))
					.background(colors.muted)
					.clickable(enabled = canReset, onClick = onReset),
				contentAlignment = Alignment.Center,
			) {
				Icon(
					painter = painterResource(Res.drawable.undo),
					contentDescription = stringResource(Res.string.preflop_drill_reset),
					tint = if (canReset) colors.textPrimary else colors.textSecondary.copy(alpha = 0.4f),
					modifier = Modifier.size(20.dp),
				)
			}
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
			cells = previewPaintedCells,
			brush = PreflopAction.RAISE,
			canPickBrush = true,
			lineChecks = previewLineChecks,
			canReset = true,
			strokePreview = null,
			onBrushClick = {},
			onLineToggle = {},
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
			cells = emptyMap(),
			brush = PreflopAction.RAISE,
			canPickBrush = false,
			lineChecks = previewLineChecks,
			canReset = false,
			strokePreview = null,
			onBrushClick = {},
			onLineToggle = {},
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
			cells = previewPaintedCells,
			brush = PreflopAction.RAISE,
			canPickBrush = true,
			lineChecks = previewLineChecks,
			canReset = true,
			strokePreview = previewStrokePreview,
			onBrushClick = {},
			onLineToggle = {},
			onStrokeStart = {},
			onStrokeMove = {},
			onStrokeEnd = {},
			onReset = {},
			onGrade = {},
		)
	}
}

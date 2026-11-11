package com.hand.log.preflop.drill.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.hand.log.designsystem.etc.ThemePreview
import com.hand.log.designsystem.etc.ThemePreviews
import com.hand.log.designsystem.theme.HandyTheme
import com.hand.log.domain.model.preflop.DrillCellResult
import com.hand.log.domain.model.preflop.PreflopAction
import com.hand.log.domain.model.preflop.PreflopGrid
import com.hand.log.domain.model.preflop.PreflopHand
import com.hand.log.preflop.chart.component.actionColors
import com.hand.log.preflop.drill.contract.DrillCell
import com.hand.log.preflop.drill.contract.DrillStrokePreview
import kotlin.math.roundToInt

private val CELL_GAP = 2.dp
private val BUBBLE_SIZE = 56.dp
private val BUBBLE_LIFT = 12.dp
private const val GRID_SIZE = 13
private const val MAX_CELL_ASPECT = 1.6f

@Composable
internal fun DrillGrid(
	cells: Map<String, DrillCell>,
	modifier: Modifier = Modifier,
	fillHeight: Boolean = false,
	strokePreview: DrillStrokePreview? = null,
	onStrokeStart: ((PreflopHand) -> Unit)? = null,
	onStrokeMove: (PreflopHand) -> Unit = {},
	onStrokeEnd: () -> Unit = {},
) {
	BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
		val cellWidth = (maxWidth - CELL_GAP * (GRID_SIZE - 1)) / GRID_SIZE
		val cellHeight = if (fillHeight && constraints.hasBoundedHeight) {
			minOf((maxHeight - CELL_GAP * (GRID_SIZE - 1)) / GRID_SIZE, cellWidth * MAX_CELL_ASPECT)
		} else {
			cellWidth
		}

		Column(
			modifier = if (onStrokeStart != null) {
				Modifier.strokeInput(onStrokeStart, onStrokeMove, onStrokeEnd)
			} else {
				Modifier
			},
			verticalArrangement = Arrangement.spacedBy(CELL_GAP),
		) {
			PreflopGrid.rows.forEach { row ->
				Row(horizontalArrangement = Arrangement.spacedBy(CELL_GAP)) {
					row.forEach { hand ->
						DrillHandCell(
							hand = hand,
							cell = cells[hand.notation],
							width = cellWidth,
							height = cellHeight,
						)
					}
				}
			}
		}

		if (strokePreview != null) {
			val density = LocalDensity.current
			val gridWidthPx = with(density) { maxWidth.roundToPx() }
			StrokePreviewBubble(
				preview = strokePreview,
				modifier = Modifier.offset {
					val bubblePx = BUBBLE_SIZE.roundToPx()
					val cellCenterX = (cellWidth + CELL_GAP).toPx() * strokePreview.col + cellWidth.toPx() / 2
					val cellTop = (cellHeight + CELL_GAP).toPx() * strokePreview.row
					IntOffset(
						x = (cellCenterX - bubblePx / 2f).roundToInt().coerceIn(0, gridWidthPx - bubblePx),
						y = (cellTop - BUBBLE_LIFT.toPx() - bubblePx).roundToInt(),
					)
				},
			)
		}
	}
}

@Composable
private fun Modifier.strokeInput(
	onStrokeStart: (PreflopHand) -> Unit,
	onStrokeMove: (PreflopHand) -> Unit,
	onStrokeEnd: () -> Unit,
): Modifier {
	val start by rememberUpdatedState(onStrokeStart)
	val move by rememberUpdatedState(onStrokeMove)
	val end by rememberUpdatedState(onStrokeEnd)
	val haptic = LocalHapticFeedback.current
	return pointerInput(Unit) {
		val gapPx = CELL_GAP.toPx()
		awaitEachGesture {
			val down = awaitFirstDown()
			down.consume()
			var last = handAt(down.position, size, gapPx)
			haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
			start(last)
			var change = awaitPointerEvent().changes.firstOrNull { it.id == down.id }
			while (change != null && change.pressed) {
				change.consume()
				val hand = handAt(change.position, size, gapPx)
				if (hand != last) {
					last = hand
					haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
					move(hand)
				}
				change = awaitPointerEvent().changes.firstOrNull { it.id == down.id }
			}
			change?.consume()
			end()
		}
	}
}

private fun handAt(position: Offset, size: IntSize, gapPx: Float): PreflopHand {
	val xPitch = (size.width + gapPx) / GRID_SIZE
	val yPitch = (size.height + gapPx) / GRID_SIZE
	val col = (position.x / xPitch).toInt().coerceIn(0, GRID_SIZE - 1)
	val row = (position.y / yPitch).toInt().coerceIn(0, GRID_SIZE - 1)
	return PreflopGrid.rows[row][col]
}

@Composable
private fun cellColors(action: PreflopAction?): Pair<Color, Color> {
	val colors = HandyTheme.colorScheme
	return if (action == null) {
		colors.muted.copy(alpha = 0.4f) to colors.textSecondary
	} else {
		actionColors(
			action,
		)
	}
}

@Composable
private fun StrokePreviewBubble(
	preview: DrillStrokePreview,
	modifier: Modifier = Modifier,
) {
	val colors = HandyTheme.colorScheme
	val (fill, text) = cellColors(preview.action)
	val shape = RoundedCornerShape(12.dp)
	Box(
		modifier = modifier
			.size(BUBBLE_SIZE)
			.shadow(8.dp, shape)
			.clip(shape)
			.background(colors.card)
			.background(fill)
			.border(1.dp, colors.border, shape),
		contentAlignment = Alignment.Center,
	) {
		Text(
			text = preview.hand.notation,
			style = HandyTheme.typography.bold20,
			color = text,
			textAlign = TextAlign.Center,
			maxLines = 1,
		)
	}
}

@Composable
private fun DrillHandCell(
	hand: PreflopHand,
	cell: DrillCell?,
	width: Dp,
	height: Dp,
) {
	val colors = HandyTheme.colorScheme
	val (background, foreground) = cellColors(cell?.action)
	val shape = RoundedCornerShape(3.dp)
	val cellModifier = when (cell?.mark) {
		DrillCellResult.WRONG_ACTION, DrillCellResult.EXTRA ->
			Modifier.background(background).border(1.5.dp, colors.error, shape)
		DrillCellResult.MISSED ->
			Modifier.background(background.copy(alpha = 0.3f)).border(1.5.dp, background, shape)
		DrillCellResult.CORRECT, null -> Modifier.background(background)
	}
	Box(
		modifier = Modifier
			.size(width = width, height = height)
			.clip(shape)
			.then(cellModifier),
		contentAlignment = Alignment.Center,
	) {
		Text(
			text = hand.notation,
			style = HandyTheme.typography.bold8,
			color = if (cell?.mark == DrillCellResult.MISSED) colors.textSecondary else foreground,
			textAlign = TextAlign.Center,
			maxLines = 1,
		)
	}
}

@ThemePreviews
@Composable
private fun DrillGridPaintingPreview() {
	ThemePreview {
		DrillGrid(
			cells = previewPaintedCells,
			onStrokeStart = {},
		)
	}
}

@ThemePreviews
@Composable
private fun DrillGridStrokePreview() {
	ThemePreview {
		DrillGrid(
			cells = previewPaintedCells,
			strokePreview = previewStrokePreview,
			onStrokeStart = {},
			modifier = Modifier.padding(top = 72.dp),
		)
	}
}

@ThemePreviews
@Composable
private fun StrokePreviewBubbleUnpaintedPreview() {
	ThemePreview {
		StrokePreviewBubble(
			preview = previewStrokePreview.copy(action = null),
			modifier = Modifier.padding(16.dp),
		)
	}
}

@ThemePreviews
@Composable
private fun DrillGridGradedPreview() {
	ThemePreview {
		DrillGrid(
			cells = mapOf(
				"AA" to DrillCell(PreflopAction.RAISE, DrillCellResult.CORRECT),
				"AKs" to DrillCell(PreflopAction.RAISE_BLUFF, DrillCellResult.WRONG_ACTION),
				"KQs" to DrillCell(PreflopAction.RAISE, DrillCellResult.MISSED),
				"72o" to DrillCell(PreflopAction.RAISE, DrillCellResult.EXTRA),
			),
		)
	}
}

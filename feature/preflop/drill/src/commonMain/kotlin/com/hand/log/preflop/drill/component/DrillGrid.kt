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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
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

private val CELL_GAP = 2.dp
private const val GRID_SIZE = 13

@Composable
internal fun DrillGrid(
	cells: Map<String, DrillCell>,
	modifier: Modifier = Modifier,
	onStrokeStart: ((PreflopHand) -> Unit)? = null,
	onStrokeMove: (PreflopHand) -> Unit = {},
	onStrokeEnd: () -> Unit = {},
) {
	BoxWithConstraints(
		modifier = modifier
			.fillMaxWidth()
			.then(
				if (onStrokeStart != null) {
					Modifier.strokeInput(onStrokeStart, onStrokeMove, onStrokeEnd)
				} else {
					Modifier
				},
			),
	) {
		val cellSize = (maxWidth - CELL_GAP * (GRID_SIZE - 1)) / GRID_SIZE

		Column(verticalArrangement = Arrangement.spacedBy(CELL_GAP)) {
			PreflopGrid.rows.forEach { row ->
				Row(horizontalArrangement = Arrangement.spacedBy(CELL_GAP)) {
					row.forEach { hand ->
						DrillHandCell(
							hand = hand,
							cell = cells[hand.notation],
							size = cellSize,
						)
					}
				}
			}
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
	return pointerInput(Unit) {
		val gapPx = CELL_GAP.toPx()
		awaitEachGesture {
			val down = awaitFirstDown()
			down.consume()
			var last = handAt(down.position, size, gapPx)
			start(last)
			var change = awaitPointerEvent().changes.firstOrNull { it.id == down.id }
			while (change != null && change.pressed) {
				change.consume()
				val hand = handAt(change.position, size, gapPx)
				if (hand != last) {
					last = hand
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
	val pitch = (size.width + gapPx) / GRID_SIZE
	val col = (position.x / pitch).toInt().coerceIn(0, GRID_SIZE - 1)
	val row = (position.y / pitch).toInt().coerceIn(0, GRID_SIZE - 1)
	return PreflopGrid.rows[row][col]
}

@Composable
private fun DrillHandCell(
	hand: PreflopHand,
	cell: DrillCell?,
	size: Dp,
) {
	val colors = HandyTheme.colorScheme
	val action = cell?.action
	val (background, foreground) = if (action == null) {
		colors.muted.copy(alpha = 0.4f) to colors.textSecondary
	} else {
		actionColors(action)
	}
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
			.size(size)
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
		DrillGrid(cells = previewPaintedCells, onStrokeStart = {})
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

package com.hand.log.preflop.chart.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.hand.log.designsystem.etc.ThemePreview
import com.hand.log.designsystem.etc.ThemePreviews
import com.hand.log.designsystem.theme.HandyTheme
import com.hand.log.domain.model.preflop.PreflopAction
import com.hand.log.domain.model.preflop.PreflopChart
import com.hand.log.domain.model.preflop.PreflopGrid
import com.hand.log.domain.model.preflop.PreflopHand

private val CELL_GAP = 2.dp

@Composable
fun PreflopChartGrid(
	chart: PreflopChart,
	modifier: Modifier = Modifier,
	highlightHand: PreflopHand? = null,
) {
	BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
		val cellSize = (maxWidth - CELL_GAP * 12) / 13

		Column(verticalArrangement = Arrangement.spacedBy(CELL_GAP)) {
			PreflopGrid.rows.forEach { row ->
				Row(horizontalArrangement = Arrangement.spacedBy(CELL_GAP)) {
					row.forEach { hand ->
						HandCell(
							hand = hand,
							action = chart.actionFor(hand),
							size = cellSize,
							highlighted = hand == highlightHand,
						)
					}
				}
			}
		}
	}
}

@Composable
private fun HandCell(
	hand: PreflopHand,
	action: PreflopAction?,
	size: Dp,
	highlighted: Boolean,
) {
	val colors = HandyTheme.colorScheme

	// Not in Range(action == null) → 폴드 핸드. 표기를 딤 처리해 최소한으로 보이게 한다.
	val (background, foreground) = if (action == null) {
		colors.muted.copy(alpha = 0.4f) to colors.textSecondary
	} else {
		actionColors(action)
	}
	val shape = RoundedCornerShape(3.dp)
	Box(
		modifier = Modifier
			.size(size)
			.then(if (highlighted) Modifier.zIndex(1f) else Modifier)
			.clip(shape)
			.background(background)
			.then(if (highlighted) Modifier.border(2.dp, colors.gold, shape) else Modifier),
		contentAlignment = Alignment.Center,
	) {
		Text(
			text = hand.notation,
			style = HandyTheme.typography.bold8,
			color = foreground,
			textAlign = TextAlign.Center,
			maxLines = 1,
		)
	}
}

private val previewChart = PreflopChart(
	actions = mapOf(
		"AA" to PreflopAction.RAISE,
		"AKs" to PreflopAction.THREE_BET,
		"KQs" to PreflopAction.FOUR_BET,
		"99" to PreflopAction.CALL,
		"A2s" to PreflopAction.ALL_IN,
	),
)

@ThemePreviews
@Composable
private fun PreflopChartGridPreview() {
	ThemePreview {
		PreflopChartGrid(chart = previewChart)
	}
}

@ThemePreviews
@Composable
private fun PreflopChartGridHighlightedPreview() {
	ThemePreview {
		PreflopChartGrid(
			chart = previewChart,
			highlightHand = PreflopGrid.rows[1][1],
		)
	}
}

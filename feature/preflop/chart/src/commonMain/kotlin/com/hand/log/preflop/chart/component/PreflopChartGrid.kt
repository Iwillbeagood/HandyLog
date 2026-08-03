package com.hand.log.preflop.chart.component

import androidx.compose.foundation.background
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
import com.hand.log.designsystem.etc.ThemePreview
import com.hand.log.designsystem.etc.ThemePreviews
import com.hand.log.designsystem.theme.HandyTheme
import com.hand.log.domain.model.preflop.PreflopAction
import com.hand.log.domain.model.preflop.PreflopChart
import com.hand.log.domain.model.preflop.PreflopGrid
import com.hand.log.domain.model.preflop.PreflopHand

private val CELL_GAP = 2.dp

@Composable
internal fun PreflopChartGrid(
	chart: PreflopChart,
	modifier: Modifier = Modifier,
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
) {
	val colors = HandyTheme.colorScheme

	// Not in Range(action == null) → 폴드 핸드. 표기를 딤 처리해 최소한으로 보이게 한다.
	val (background, foreground) = if (action == null) {
		colors.muted.copy(alpha = 0.4f) to colors.textSecondary
	} else {
		actionColors(action)
	}
	Box(
		modifier = Modifier
			.size(size)
			.clip(RoundedCornerShape(3.dp))
			.background(background),
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

@ThemePreviews
@Composable
private fun PreflopChartGridPreview() {
	ThemePreview {
		PreflopChartGrid(
			chart = PreflopChart(
				actions = mapOf(
					"AA" to PreflopAction.RAISE,
					"AKs" to PreflopAction.THREE_BET,
					"KQs" to PreflopAction.FOUR_BET,
					"99" to PreflopAction.CALL,
					"A2s" to PreflopAction.ALL_IN,
				),
			),
		)
	}
}

package com.hand.log.preflop.drill.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hand.log.designsystem.etc.ThemePreview
import com.hand.log.designsystem.etc.ThemePreviews
import com.hand.log.designsystem.theme.HandyTheme
import com.hand.log.domain.model.Position
import com.hand.log.domain.model.preflop.PreflopAction
import com.hand.log.domain.model.preflop.PreflopChart
import com.hand.log.domain.model.preflop.PreflopGrid
import com.hand.log.domain.model.preflop.PreflopStack
import com.hand.log.domain.model.preflop.gradeChartDrill
import com.hand.log.preflop.drill.contract.DrillCell
import com.hand.log.preflop.drill.contract.DrillLine
import com.hand.log.preflop.drill.contract.DrillPhase
import com.hand.log.preflop.drill.contract.DrillProgress
import com.hand.log.preflop.drill.contract.DrillSpot
import com.hand.log.preflop.drill.contract.DrillStrokePreview
import com.hand.log.preflop.drill.contract.DrillSummaryItem
import com.hand.log.preflop.drill.contract.PreflopChartDrillState

@Composable
internal fun DrillProgressBar(progress: DrillProgress) {
	val colors = HandyTheme.colorScheme
	Box(
		modifier = Modifier
			.fillMaxWidth()
			.height(3.dp)
			.background(colors.muted),
	) {
		Box(
			modifier = Modifier
				.fillMaxWidth(progress.ratio)
				.fillMaxHeight()
				.background(colors.primary),
		)
	}
}

internal val previewAnswer = PreflopChart(
	mapOf(
		"AA" to PreflopAction.RAISE,
		"KK" to PreflopAction.RAISE,
		"AKs" to PreflopAction.RAISE,
		"AKo" to PreflopAction.RAISE,
		"KQs" to PreflopAction.RAISE,
		"A5s" to PreflopAction.RAISE_BLUFF,
		"A4s" to PreflopAction.RAISE_BLUFF,
	),
)

internal val previewSpot = DrillSpot(
	stack = PreflopStack.BB100,
	hero = Position.CO,
	answer = previewAnswer,
)

internal val previewProgress = DrillProgress(current = 3, total = 8)

internal val previewSummaryItems = listOf(
	DrillSummaryItem(Position.UTG, 92),
	DrillSummaryItem(Position.UTG1, 88),
	DrillSummaryItem(Position.UTG2, 85),
	DrillSummaryItem(Position.LJ, 79),
	DrillSummaryItem(Position.HJ, 81),
	DrillSummaryItem(Position.CO, 74),
	DrillSummaryItem(Position.BTN, 66),
	DrillSummaryItem(Position.SB, 58),
)

internal val previewPainted = PreflopGrid.rows[0].associate {
	it.notation to PreflopAction.RAISE
} + mapOf(
	"KK" to PreflopAction.RAISE,
	"72o" to PreflopAction.RAISE_BLUFF,
)

internal val previewStrokePreview = DrillStrokePreview(
	hand = PreflopGrid.rows[0][1],
	row = 0,
	col = 1,
	action = PreflopAction.RAISE,
)

internal val previewPaintedCells = previewPainted.mapValues { DrillCell(it.value) }

internal val previewLineChecks = PreflopChartDrillState(
	painted = previewPainted,
	checkedLines = setOf(DrillLine.Row(0)),
).lineChecks

internal val previewResult = gradeChartDrill(previewAnswer.actions, previewPainted)

internal val previewGradedCells = PreflopChartDrillState(
	phase = DrillPhase.Graded(previewSpot, previewResult),
	painted = previewPainted,
).gridCells

@ThemePreviews
@Composable
private fun DrillProgressBarPreview() {
	ThemePreview {
		DrillProgressBar(progress = previewProgress)
	}
}

package com.hand.log.preflop.drill.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.hand.log.designsystem.component.FadeAnimatedContent
import com.hand.log.designsystem.component.HandyToggleGroup
import com.hand.log.designsystem.component.RegularButton
import com.hand.log.designsystem.etc.ThemePreview
import com.hand.log.designsystem.etc.ThemePreviews
import com.hand.log.designsystem.theme.HandyTheme
import com.hand.log.domain.model.preflop.ChartDrillResult
import com.hand.log.preflop.chart.component.PreflopChartGrid
import com.hand.log.preflop.chart.component.PreflopChartLegend
import com.hand.log.preflop.drill.contract.DrillCell
import com.hand.log.preflop.drill.contract.DrillSpot
import handylog.core.res.generated.resources.Res
import handylog.core.res.generated.resources.preflop_drill_accuracy
import handylog.core.res.generated.resources.preflop_drill_answer_chart
import handylog.core.res.generated.resources.preflop_drill_correct
import handylog.core.res.generated.resources.preflop_drill_extra
import handylog.core.res.generated.resources.preflop_drill_graded_desc
import handylog.core.res.generated.resources.preflop_drill_missed
import handylog.core.res.generated.resources.preflop_drill_my_chart
import handylog.core.res.generated.resources.preflop_drill_next
import handylog.core.res.generated.resources.preflop_drill_retry
import handylog.core.res.generated.resources.preflop_drill_wrong_action
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun GradedContent(
	spot: DrillSpot,
	result: ChartDrillResult,
	cells: Map<String, DrillCell>,
	showAnswer: Boolean,
	onStackClick: () -> Unit,
	onShowAnswer: (Boolean) -> Unit,
	onRetry: () -> Unit,
	onNext: () -> Unit,
) {
	val colors = HandyTheme.colorScheme
	val myChartLabel = stringResource(Res.string.preflop_drill_my_chart)
	val answerLabel = stringResource(Res.string.preflop_drill_answer_chart)
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
				description = stringResource(Res.string.preflop_drill_graded_desc),
				onStackClick = onStackClick,
			)
			ScoreSummary(result)
			HandyToggleGroup(
				options = listOf(false, true),
				selected = showAnswer,
				onSelect = onShowAnswer,
				label = { if (it) answerLabel else myChartLabel },
			)
			PreflopChartLegend(actions = spot.answer.presentActions)
			FadeAnimatedContent(targetState = showAnswer) { answer ->
				if (answer) {
					PreflopChartGrid(chart = spot.answer)
				} else {
					DrillGrid(cells = cells)
				}
			}
		}
		Row(
			modifier = Modifier
				.fillMaxWidth()
				.padding(horizontal = 16.dp, vertical = 12.dp),
			horizontalArrangement = Arrangement.spacedBy(8.dp),
		) {
			RegularButton(
				onClick = onRetry,
				text = stringResource(Res.string.preflop_drill_retry),
				textStyle = HandyTheme.typography.bold16,
				containerColor = colors.muted,
				contentColor = colors.textPrimary,
				modifier = Modifier.weight(1f),
			)
			RegularButton(
				onClick = onNext,
				text = stringResource(Res.string.preflop_drill_next),
				textStyle = HandyTheme.typography.bold16,
				modifier = Modifier.weight(1f),
			)
		}
	}
}

@Composable
private fun ScoreSummary(result: ChartDrillResult) {
	val colors = HandyTheme.colorScheme
	Column(
		modifier = Modifier
			.fillMaxWidth()
			.clip(RoundedCornerShape(12.dp))
			.background(colors.card)
			.padding(16.dp),
		horizontalAlignment = Alignment.CenterHorizontally,
		verticalArrangement = Arrangement.spacedBy(12.dp),
	) {
		Column(horizontalAlignment = Alignment.CenterHorizontally) {
			Text(
				text = "${result.accuracyPct}%",
				style = HandyTheme.typography.bold32,
				color = colors.primary,
			)
			Text(
				text = stringResource(Res.string.preflop_drill_accuracy),
				style = HandyTheme.typography.regular12,
				color = colors.textSecondary,
			)
		}
		Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
			StatItem(stringResource(Res.string.preflop_drill_correct), result.correct, colors.primary)
			StatItem(stringResource(Res.string.preflop_drill_wrong_action), result.wrongAction, colors.error)
			StatItem(stringResource(Res.string.preflop_drill_missed), result.missed, colors.gold)
			StatItem(stringResource(Res.string.preflop_drill_extra), result.extra, colors.error)
		}
	}
}

@Composable
private fun RowScope.StatItem(
	label: String,
	value: Int,
	valueColor: Color,
) {
	Column(
		modifier = Modifier
			.weight(1f)
			.fillMaxHeight(),
		horizontalAlignment = Alignment.CenterHorizontally,
		verticalArrangement = Arrangement.spacedBy(2.dp),
	) {
		Text(
			text = "$value",
			style = HandyTheme.typography.bold18,
			color = valueColor,
		)
		Text(
			text = label,
			style = HandyTheme.typography.regular12,
			color = HandyTheme.colorScheme.textSecondary,
			textAlign = TextAlign.Center,
		)
	}
}

@ThemePreviews
@Composable
private fun GradedContentMyChartPreview() {
	ThemePreview {
		GradedContent(
			spot = previewSpot,
			result = previewResult,
			cells = previewGradedCells,
			showAnswer = false,
			onStackClick = {},
			onShowAnswer = {},
			onRetry = {},
			onNext = {},
		)
	}
}

@ThemePreviews
@Composable
private fun GradedContentAnswerPreview() {
	ThemePreview {
		GradedContent(
			spot = previewSpot,
			result = previewResult,
			cells = previewGradedCells,
			showAnswer = true,
			onStackClick = {},
			onShowAnswer = {},
			onRetry = {},
			onNext = {},
		)
	}
}

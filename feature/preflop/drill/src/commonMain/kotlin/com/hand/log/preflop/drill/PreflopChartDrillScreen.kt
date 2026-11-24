package com.hand.log.preflop.drill

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.hand.log.designsystem.component.BaseScaffold
import com.hand.log.designsystem.component.FadeAnimatedContent
import com.hand.log.designsystem.component.HandyTopAppbar
import com.hand.log.designsystem.component.TopAppbarType
import com.hand.log.designsystem.etc.ThemePreview
import com.hand.log.designsystem.etc.ThemePreviews
import com.hand.log.domain.model.preflop.PreflopAction
import com.hand.log.domain.model.preflop.PreflopHand
import com.hand.log.domain.model.preflop.PreflopStack
import com.hand.log.preflop.chart.component.PreflopStackBadge
import com.hand.log.preflop.drill.component.DrillProgressBar
import com.hand.log.preflop.drill.component.GradedContent
import com.hand.log.preflop.drill.component.PaintingContent
import com.hand.log.preflop.drill.component.SetupContent
import com.hand.log.preflop.drill.component.SummaryContent
import com.hand.log.preflop.drill.component.previewProgress
import com.hand.log.preflop.drill.component.previewSummaryItems
import com.hand.log.preflop.drill.component.previewPainted
import com.hand.log.preflop.drill.component.previewResult
import com.hand.log.preflop.drill.component.previewSpot
import com.hand.log.preflop.drill.component.previewStrokePreview
import com.hand.log.preflop.drill.contract.DrillLine
import com.hand.log.preflop.drill.contract.DrillMode
import com.hand.log.preflop.drill.contract.DrillPhase
import com.hand.log.preflop.drill.contract.PreflopChartDrillState
import handylog.core.res.generated.resources.Res
import handylog.core.res.generated.resources.preflop_drill_spot_title
import handylog.core.res.generated.resources.preflop_drill_title
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun PreflopChartDrillScreen(
	state: PreflopChartDrillState,
	onBack: () -> Unit,
	onStackSelect: (PreflopStack) -> Unit,
	onModeSelect: (DrillMode) -> Unit,
	onStart: () -> Unit,
	onBackToSetup: () -> Unit,
	onBrushClick: () -> Unit,
	onLineToggle: (DrillLine) -> Unit,
	onStrokeStart: (PreflopHand) -> Unit,
	onStrokeMove: (PreflopHand) -> Unit,
	onStrokeEnd: () -> Unit,
	onReset: () -> Unit,
	onGrade: () -> Unit,
	onShowAnswer: (Boolean) -> Unit,
	onRetry: () -> Unit,
	onNext: () -> Unit,
	onRestartSequence: () -> Unit,
) {
	BaseScaffold(
		topBar = {
			val spot = state.spot
			HandyTopAppbar(
				title = if (spot != null) {
					stringResource(Res.string.preflop_drill_spot_title, spot.hero.label)
				} else {
					stringResource(Res.string.preflop_drill_title)
				},
				navigationType = TopAppbarType.Close,
				onBackEvent = onBack,
				titleTrailing = spot?.let { { PreflopStackBadge(stack = it.stack) } },
				subContent = state.spotProgress?.let { { DrillProgressBar(progress = it) } },
			)
		},
	) {
		FadeAnimatedContent(
			targetState = state.phase,
			modifier = Modifier.fillMaxSize(),
		) { phase ->
			when (phase) {
				DrillPhase.Setup -> SetupContent(
					stackOptions = state.stackOptions,
					selectedStack = state.selectedStack,
					lockedStacks = state.lockedStacks,
					modeOptions = state.modeOptions,
					selectedMode = state.selectedMode,
					isLoading = state.isLoading,
					onStackSelect = onStackSelect,
					onModeSelect = onModeSelect,
					onStart = onStart,
				)
				is DrillPhase.Painting -> PaintingContent(
					cells = state.gridCells,
					brush = state.brush,
					canPickBrush = state.canPickBrush,
					lineChecks = state.lineChecks,
					canReset = state.canReset,
					strokePreview = state.strokePreview,
					onBrushClick = onBrushClick,
					onLineToggle = onLineToggle,
					onStrokeStart = onStrokeStart,
					onStrokeMove = onStrokeMove,
					onStrokeEnd = onStrokeEnd,
					onReset = onReset,
					onGrade = onGrade,
				)
				is DrillPhase.Graded -> GradedContent(
					spot = phase.spot,
					result = phase.result,
					cells = state.gridCells,
					showAnswer = state.showAnswer,
					isLastSpot = state.isLastSpot,
					onShowAnswer = onShowAnswer,
					onRetry = onRetry,
					onNext = onNext,
				)
				is DrillPhase.Summary -> SummaryContent(
					summary = phase,
					onChangeSetup = onBackToSetup,
					onRestart = onRestartSequence,
				)
			}
		}
	}
}

@Composable
private fun PreviewScreen(state: PreflopChartDrillState) {
	PreflopChartDrillScreen(
		state = state,
		onBack = {},
		onStackSelect = {},
		onModeSelect = {},
		onStart = {},
		onBackToSetup = {},
		onBrushClick = {},
		onLineToggle = {},
		onStrokeStart = {},
		onStrokeMove = {},
		onStrokeEnd = {},
		onReset = {},
		onGrade = {},
		onShowAnswer = {},
		onRetry = {},
		onNext = {},
		onRestartSequence = {},
	)
}

@ThemePreviews
@Composable
private fun PreflopChartDrillSetupPreview() {
	ThemePreview {
		PreviewScreen(PreflopChartDrillState(isLoading = false))
	}
}

@ThemePreviews
@Composable
private fun PreflopChartDrillPaintingPreview() {
	ThemePreview {
		PreviewScreen(
			PreflopChartDrillState(
				phase = DrillPhase.Painting(previewSpot),
				painted = previewPainted,
				brush = PreflopAction.RAISE,
				isLoading = false,
			),
		)
	}
}

@ThemePreviews
@Composable
private fun PreflopChartDrillStrokingPreview() {
	ThemePreview {
		PreviewScreen(
			PreflopChartDrillState(
				phase = DrillPhase.Painting(previewSpot),
				painted = previewPainted,
				brush = PreflopAction.RAISE,
				strokeHand = previewStrokePreview.hand,
				isLoading = false,
			),
		)
	}
}

@ThemePreviews
@Composable
private fun PreflopChartDrillGradedPreview() {
	ThemePreview {
		PreviewScreen(
			PreflopChartDrillState(
				phase = DrillPhase.Graded(previewSpot, previewResult),
				painted = previewPainted,
				isLoading = false,
			),
		)
	}
}

@ThemePreviews
@Composable
private fun PreflopChartDrillSequentialPreview() {
	ThemePreview {
		PreviewScreen(
			PreflopChartDrillState(
				phase = DrillPhase.Painting(previewSpot),
				selectedMode = DrillMode.SEQUENTIAL,
				sequence = List(previewProgress.total) { previewSpot },
				sequenceIndex = previewProgress.current - 1,
				painted = previewPainted,
				brush = PreflopAction.RAISE,
				isLoading = false,
			),
		)
	}
}

@ThemePreviews
@Composable
private fun PreflopChartDrillSummaryPreview() {
	ThemePreview {
		PreviewScreen(
			PreflopChartDrillState(
				phase = DrillPhase.Summary(PreflopStack.BB100, previewSummaryItems),
				isLoading = false,
			),
		)
	}
}

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
import com.hand.log.preflop.drill.component.GradedContent
import com.hand.log.preflop.drill.component.PaintingContent
import com.hand.log.preflop.drill.component.SetupContent
import com.hand.log.preflop.drill.component.previewPainted
import com.hand.log.preflop.drill.component.previewResult
import com.hand.log.preflop.drill.component.previewSpot
import com.hand.log.preflop.drill.contract.DrillBrush
import com.hand.log.preflop.drill.contract.DrillPhase
import com.hand.log.preflop.drill.contract.PreflopChartDrillState
import handylog.core.res.generated.resources.Res
import handylog.core.res.generated.resources.preflop_drill_title
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun PreflopChartDrillScreen(
	state: PreflopChartDrillState,
	onBack: () -> Unit,
	onStackSelect: (PreflopStack) -> Unit,
	onStart: () -> Unit,
	onBackToSetup: () -> Unit,
	onBrushSelect: (DrillBrush) -> Unit,
	onStrokeStart: (PreflopHand) -> Unit,
	onStrokeMove: (PreflopHand) -> Unit,
	onStrokeEnd: () -> Unit,
	onReset: () -> Unit,
	onGrade: () -> Unit,
	onShowAnswer: (Boolean) -> Unit,
	onRetry: () -> Unit,
	onNext: () -> Unit,
) {
	BaseScaffold(
		topBar = {
			HandyTopAppbar(
				title = stringResource(Res.string.preflop_drill_title),
				navigationType = TopAppbarType.Close,
				onBackEvent = onBack,
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
					isLoading = state.isLoading,
					onStackSelect = onStackSelect,
					onStart = onStart,
				)
				is DrillPhase.Painting -> PaintingContent(
					spot = phase.spot,
					cells = state.gridCells,
					brushOptions = state.brushOptions,
					brush = state.brush,
					canReset = state.canReset,
					onStackClick = onBackToSetup,
					onBrushSelect = onBrushSelect,
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
					onStackClick = onBackToSetup,
					onShowAnswer = onShowAnswer,
					onRetry = onRetry,
					onNext = onNext,
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
		onStart = {},
		onBackToSetup = {},
		onBrushSelect = {},
		onStrokeStart = {},
		onStrokeMove = {},
		onStrokeEnd = {},
		onReset = {},
		onGrade = {},
		onShowAnswer = {},
		onRetry = {},
		onNext = {},
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
				brush = DrillBrush.Paint(PreflopAction.RAISE),
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

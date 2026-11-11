package com.hand.log.preflop.drill

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hand.log.navigation.interop.LocalNavigateActionInterop
import com.hand.log.preflop.drill.contract.PreflopChartDrillModalEffect
import com.hand.log.ui.ProPaywallSheet
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun PreflopChartDrillRoute(
	viewModel: PreflopChartDrillViewModel = koinViewModel(),
) {
	val state by viewModel.state.collectAsStateWithLifecycle()
	val modalEffect by viewModel.modalEffect.collectAsStateWithLifecycle()
	val navAction = LocalNavigateActionInterop.current

	PreflopChartDrillScreen(
		state = state,
		onBack = navAction::popBackStack,
		onStackSelect = viewModel::selectStack,
		onStart = viewModel::start,
		onBackToSetup = viewModel::backToSetup,
		onBrushSelect = viewModel::selectBrush,
		onStrokeStart = viewModel::strokeStart,
		onStrokeMove = viewModel::strokeMove,
		onStrokeEnd = viewModel::strokeEnd,
		onReset = viewModel::reset,
		onGrade = viewModel::grade,
		onShowAnswer = viewModel::showAnswer,
		onRetry = viewModel::retry,
		onNext = viewModel::nextSpot,
	)

	when (val effect = modalEffect) {
		PreflopChartDrillModalEffect.Idle -> {}
		is PreflopChartDrillModalEffect.ShowPaywall -> {
			ProPaywallSheet(
				feature = effect.feature,
				onDismiss = viewModel::dismissModal,
				onUpgrade = {
					viewModel.dismissModal()
					navAction.navigateToProUpgrade()
				},
			)
		}
	}
}

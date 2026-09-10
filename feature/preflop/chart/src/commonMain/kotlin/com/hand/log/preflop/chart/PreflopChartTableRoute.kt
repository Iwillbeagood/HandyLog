package com.hand.log.preflop.chart

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hand.log.navigation.interop.LocalNavigateActionInterop
import com.hand.log.preflop.chart.contract.PreflopChartModalEffect
import com.hand.log.ui.ProPaywallSheet

@Composable
internal fun PreflopChartTableRoute(
	viewModel: PreflopChartViewModel,
) {
	val state by viewModel.state.collectAsStateWithLifecycle()
	val modalEffect by viewModel.modalEffect.collectAsStateWithLifecycle()
	val navAction = LocalNavigateActionInterop.current

	PreflopChartTableScreen(
		state = state,
		onBack = navAction::popBackStack,
		onSeatSelect = viewModel::selectSeat,
		onPrev = viewModel::prevHero,
		onNext = viewModel::nextHero,
	)

	when (val effect = modalEffect) {
		PreflopChartModalEffect.Idle -> {}
		is PreflopChartModalEffect.ShowPaywall -> {
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

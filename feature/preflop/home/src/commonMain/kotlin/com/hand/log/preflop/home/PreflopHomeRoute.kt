package com.hand.log.preflop.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hand.log.navigation.interop.LocalNavigateActionInterop
import com.hand.log.preflop.home.contract.PreflopHomeEffect
import com.hand.log.preflop.home.contract.PreflopHomeModalEffect
import com.hand.log.ui.ProPaywallSheet
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun PreflopHomeRoute(
	viewModel: PreflopHomeViewModel = koinViewModel(),
) {
	val navAction = LocalNavigateActionInterop.current
	val state by viewModel.state.collectAsStateWithLifecycle()
	val modalEffect by viewModel.modalEffect.collectAsStateWithLifecycle()

	LaunchedEffect(Unit) {
		viewModel.effect.collect { effect ->
			when (effect) {
				PreflopHomeEffect.NavigateToQuiz -> navAction.navigateToPreflopQuiz()
			}
		}
	}

	PreflopHomeScreen(
		quizLocked = state.quizLocked,
		onChartClick = navAction::navigateToPreflopChart,
		onQuizClick = viewModel::onQuizClick,
	)

	when (val effect = modalEffect) {
		PreflopHomeModalEffect.Idle -> {}
		is PreflopHomeModalEffect.ShowPaywall -> {
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

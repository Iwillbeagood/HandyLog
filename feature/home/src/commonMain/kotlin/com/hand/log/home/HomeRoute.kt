package com.hand.log.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hand.log.designsystem.component.HmFadeAnimatedVisibility
import com.hand.log.designsystem.component.TopAppBarScaffold
import com.hand.log.home.contract.HomeEffect
import com.hand.log.home.contract.HomeModalEffect
import com.hand.log.navigation.interop.LocalNavigateActionInterop
import com.hand.log.navigation.interop.rememberShowSnackBar

@Composable
internal fun HomeRoute(
	viewModel: HomeViewModel
) {
    val homeState by viewModel.homeState.collectAsStateWithLifecycle()
    val homeModalEffect by viewModel.homeModalEffect.collectAsStateWithLifecycle()
	val navAction = LocalNavigateActionInterop.current
	val showSnackbar = rememberShowSnackBar()

    HomeContent(
        homeState = homeState,
        onGoBack = navAction::popBackStack
    )

    HomeModalContent(
        homeModalEffect = homeModalEffect,
        onDismissRequest = viewModel::dismissDialog,
    )

	LaunchedEffect(true) {
		viewModel.homeEffect.collect { effect ->
			when (effect) {
				is HomeEffect.ShowSnackBar -> showSnackbar(effect.messageType)
			}
		}
	}
}

@Composable
private fun HomeContent(
    homeState: HomeState,
    onGoBack: () -> Unit,
) {
	TopAppBarScaffold(
		title = "TODO: 타이틀을 알맞게 설정해야 합니다.",
		onBackEvent = onGoBack,
    ) {
        HmFadeAnimatedVisibility(homeState is HomeState.HomeData) {
            if (homeState is HomeState.HomeData) {
                HomeScreen(
                    homeState = homeState
                )
            }
        }
    }
}

@Composable
private fun HomeModalContent(
	homeModalEffect: HomeModalEffect,
	onDismissRequest: () -> Unit,
) {
    when (homeModalEffect) {
        HomeModalEffect.Idle -> {}
    }
}

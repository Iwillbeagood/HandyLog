package com.hand.log.home

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hand.log.home.contract.HomeEffect
import com.hand.log.designsystem.component.HmFadeAnimatedVisibility
import com.hand.log.home.contract.HomeModalEffect
import com.hand.log.home.contract.HomeState
import com.hand.log.navigation.interop.LocalNavigateActionInterop
import com.hand.log.ui.table.TableFormSheet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun HomeRoute(
	viewModel: HomeViewModel,
) {
	val homeState by viewModel.homeState.collectAsStateWithLifecycle()
	val homeModalEffect by viewModel.homeModalEffect.collectAsStateWithLifecycle()
	val navAction = LocalNavigateActionInterop.current

	var showSetupSheet by remember { mutableStateOf(false) }

	LaunchedEffect(Unit) {
		viewModel.homeEffect.collect { effect ->
			when (effect) {
				is HomeEffect.NavigateToTable -> {
					navAction.navigateToTableDetail(effect.tableId)
				}
				is HomeEffect.ShowSnackBar -> { /* TODO */ }
			}
		}
	}

	HomeContent(
		homeState = homeState,
		onNavigateToTableDetail = navAction::navigateToTableDetail,
		onFabClick = { showSetupSheet = true },
	)

	HomeModalContent(
		homeModalEffect = homeModalEffect,
		onDismissRequest = viewModel::dismissDialog,
	)

	if (showSetupSheet) {
		TableFormSheet(
			onDismissRequest = {
				showSetupSheet = false
			},
			onSubmit = { date, location, gameType, startingStack, blinds, playerCount, heroSeat ->
				viewModel.saveTable(
					date = date,
					location = location,
					gameType = gameType,
					startingStack = startingStack,
					blinds = blinds,
					playerCount = playerCount,
					heroSeat = heroSeat,
				)
			},
		)
	}
}

@Composable
private fun HomeContent(
	homeState: HomeState,
	onNavigateToTableDetail: (String) -> Unit,
	onFabClick: () -> Unit,
) {
	HmFadeAnimatedVisibility(homeState is HomeState.HomeData) {
		if (homeState is HomeState.HomeData) {
			HomeScreen(
				homeState = homeState,
				onNavigateToTableDetail = onNavigateToTableDetail,
				onTableAdd = onFabClick,
			)
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

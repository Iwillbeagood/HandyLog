package com.hand.log.home

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hand.log.domain.model.PokerTable
import com.hand.log.home.contract.HomeEffect
import com.hand.log.home.contract.HomeModalEffect
import com.hand.log.navigation.interop.LocalMainActionInterop
import com.hand.log.navigation.interop.LocalNavigateActionInterop
import com.hand.log.tableedit.TableEditSheet
import com.hand.log.ui.ProPaywallSheet
import handylog.core.res.generated.resources.Res
import handylog.core.res.generated.resources.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun HomeRoute(
	viewModel: HomeViewModel,
) {
	val homeState by viewModel.homeState.collectAsStateWithLifecycle()
	val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
	val homeModalEffect by viewModel.homeModalEffect.collectAsStateWithLifecycle()
	val navAction = LocalNavigateActionInterop.current
	val mainAction = LocalMainActionInterop.current

	LaunchedEffect(Unit) {
		viewModel.homeEffect.collect { effect ->
			when (effect) {
				is HomeEffect.NavigateToTable -> {
					navAction.navigateToTableDetail(effect.tableId)
				}
				is HomeEffect.TableDeleted -> {
					mainAction.onShowToast(Res.string.home_table_deleted)
				}
			}
		}
	}

	HomeScreen(
		homeState = homeState,
		selectedTab = selectedTab,
		onTabSelect = viewModel::selectTab,
		onNavigateToTableDetail = navAction::navigateToTableDetail,
		onNavigateToHandDetail = navAction::navigateToHandDetail,
		onTableAdd = viewModel::showTableEditSheet,
	)

	HomeModalContent(
		homeModalEffect = homeModalEffect,
		onDismissRequest = viewModel::dismissDialog,
		onTableSaved = { table ->
			viewModel.onTableSaved(table)
		},
	)
}

@Composable
private fun HomeModalContent(
	homeModalEffect: HomeModalEffect,
	onDismissRequest: () -> Unit,
	onTableSaved: (PokerTable) -> Unit,
) {
	when (homeModalEffect) {
		HomeModalEffect.Idle -> {}
		HomeModalEffect.TableEditSheet -> {
			TableEditSheet(
				onSaved = { table, _ -> onTableSaved(table) },
				onDismiss = onDismissRequest,
			)
		}
		is HomeModalEffect.ShowPaywall -> {
			ProPaywallSheet(
				feature = homeModalEffect.feature,
				onDismiss = onDismissRequest,
			)
		}
	}
}

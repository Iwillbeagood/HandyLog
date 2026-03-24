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
import com.hand.log.home.contract.HomeModalEffect
import com.hand.log.navigation.interop.LocalMainActionInterop
import com.hand.log.navigation.interop.LocalNavigateActionInterop
import com.hand.log.tableedit.TableEditSheet
import handylog.core.res.generated.resources.Res
import handylog.core.res.generated.resources.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun HomeRoute(
	viewModel: HomeViewModel,
) {
	val homeState by viewModel.homeState.collectAsStateWithLifecycle()
	val homeModalEffect by viewModel.homeModalEffect.collectAsStateWithLifecycle()
	val navAction = LocalNavigateActionInterop.current
	val mainAction = LocalMainActionInterop.current

	var showSetupSheet by remember { mutableStateOf(false) }

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
		onNavigateToTableDetail = navAction::navigateToTableDetail,
		onTableAdd = { showSetupSheet = true },
	)

	HomeModalContent(
		homeModalEffect = homeModalEffect,
		onDismissRequest = viewModel::dismissDialog,
	)

	if (showSetupSheet) {
		TableEditSheet(
			onSaved = {
				navAction.navigateToTableDetail(it.id)
			},
			onDismiss = { showSetupSheet = false },
		)
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

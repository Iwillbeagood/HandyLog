package com.hand.log.table

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hand.log.designsystem.component.HmFadeAnimatedVisibility
import com.hand.log.navigation.interop.LocalNavigateActionInterop
import com.hand.log.table.component.PlayerSetupSheet
import com.hand.log.table.contract.TableDetailModalEffect
import com.hand.log.table.contract.TableDetailState

@Composable
internal fun TableDetailRoute(
	viewModel: TableDetailViewModel,
) {
	val state by viewModel.state.collectAsStateWithLifecycle()
	val modalEffect by viewModel.modalEffect.collectAsStateWithLifecycle()
	val navAction = LocalNavigateActionInterop.current

	HmFadeAnimatedVisibility(state is TableDetailState.TableData) {
		if (state is TableDetailState.TableData) {
			val tableData = state as TableDetailState.TableData
			TableDetailScreen(
				state = tableData,
				onBack = navAction::popBackStack,
				onNavigateToRecordHand = { navAction.navigateToRecordHand(tableData.table.id) },
				onDeleteHand = viewModel::deleteHand,
				onShowPlayerSetup = viewModel::showPlayerSetup,
			)
		}
	}

	TableDetailModalContent(
		state = state,
		modalEffect = modalEffect,
		onDismiss = viewModel::dismissModal,
		onUpdatePlayers = viewModel::updatePlayers,
	)
}

@Composable
private fun TableDetailModalContent(
	state: TableDetailState,
	modalEffect: TableDetailModalEffect,
	onDismiss: () -> Unit,
	onUpdatePlayers: (List<com.hand.log.domain.model.Player>) -> Unit,
) {
	when (modalEffect) {
		TableDetailModalEffect.Idle -> {}
		TableDetailModalEffect.ShowPlayerSetup -> {
			if (state is TableDetailState.TableData) {
				PlayerSetupSheet(
					playerCount = state.table.playerCount,
					players = state.table.players,
					onSave = { players ->
						onUpdatePlayers(players)
						onDismiss()
					},
					onDismiss = onDismiss,
				)
			}
		}
	}
}

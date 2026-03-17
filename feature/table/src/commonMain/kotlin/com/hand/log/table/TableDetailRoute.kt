package com.hand.log.table

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hand.log.designsystem.component.HmFadeAnimatedVisibility
import com.hand.log.navigation.interop.LocalNavigateActionInterop
import com.hand.log.domain.model.Blinds
import com.hand.log.domain.model.GameType
import com.hand.log.table.component.PlayerSetupSheet
import com.hand.log.table.contract.TableDetailModalEffect
import com.hand.log.table.contract.TableDetailState
import com.hand.log.ui.table.TableFormSheet

@OptIn(ExperimentalMaterial3Api::class)
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
				onSeatClick = viewModel::showPlayerSetup,
				onShowTableEdit = viewModel::showTableEdit,
			)
		}
	}

	TableDetailModalContent(
		state = state,
		modalEffect = modalEffect,
		onDismiss = viewModel::dismissModal,
		onUpdatePlayers = viewModel::updatePlayers,
		onUpdateTable = viewModel::updateTable,
	)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TableDetailModalContent(
	state: TableDetailState,
	modalEffect: TableDetailModalEffect,
	onDismiss: () -> Unit,
	onUpdatePlayers: (List<com.hand.log.domain.model.Player>) -> Unit,
	onUpdateTable: (String, String?, GameType, Double, Blinds?, Int, Int) -> Unit,
) {
	when (modalEffect) {
		TableDetailModalEffect.Idle -> {}
		is TableDetailModalEffect.ShowPlayerSetup -> {
			if (state is TableDetailState.TableData) {
				PlayerSetupSheet(
					initialSeat = modalEffect.initialSeat,
					isHero = modalEffect.initialSeat == state.table.heroSeat,
					playerCount = state.table.playerCount,
					startingStack = state.table.startingStack,
					players = state.table.players,
					onSave = { players ->
						onUpdatePlayers(players)
						onDismiss()
					},
					onDismiss = onDismiss,
				)
			}
		}
		TableDetailModalEffect.ShowTableEdit -> {
			if (state is TableDetailState.TableData) {
				val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

				TableFormSheet(
					sheetState = sheetState,
					table = state.table,
					onDismissRequest = onDismiss,
					onSubmit = { date, location, gameType, stack, blinds, playerCount, heroSeat ->
						onUpdateTable(date, location, gameType, stack, blinds, playerCount, heroSeat)
						onDismiss()
					},
				)
			}
		}
	}
}

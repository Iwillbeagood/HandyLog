package com.hand.log.table

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hand.log.navigation.interop.LocalNavigateActionInterop
import com.hand.log.domain.model.Player
import com.hand.log.playersetup.PlayerSetupSheet
import com.hand.log.playersetup.PlayerSetupViewModel
import com.hand.log.table.contract.TableDetailModalEffect
import com.hand.log.table.contract.TableDetailState
import com.hand.log.tableedit.TableEditSheet
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun TableDetailRoute(
	viewModel: TableDetailViewModel,
) {
	val state by viewModel.state.collectAsStateWithLifecycle()
	val modalEffect by viewModel.modalEffect.collectAsStateWithLifecycle()
	val navAction = LocalNavigateActionInterop.current

	val playerSetupViewModel: PlayerSetupViewModel = koinViewModel()

	if (state is TableDetailState.TableData) {
		val tableData = state as TableDetailState.TableData
		TableDetailScreen(
			state = tableData,
			onBack = navAction::popBackStack,
			onNavigateToRecordHand = { navAction.navigateToRecordHand(tableData.table.id) },
			onNavigateToHandDetail = navAction::navigateToHandDetail,
			onDeleteHand = viewModel::deleteHand,
			onSeatClick = viewModel::showPlayerSetup,
			onShowTableEdit = viewModel::showTableEdit,
		)
	}

	TableDetailModalContent(
		modalEffect = modalEffect,
		playerSetupViewModel = playerSetupViewModel,
		onDismiss = viewModel::dismissModal,
		onUpdatePlayers = viewModel::updatePlayers,
		onTableSaved = { viewModel.onTableSaved(it) },
	)
}

@Composable
private fun TableDetailModalContent(
	modalEffect: TableDetailModalEffect,
	playerSetupViewModel: PlayerSetupViewModel,
	onDismiss: () -> Unit,
	onUpdatePlayers: (List<Player>) -> Unit,
	onTableSaved: (com.hand.log.domain.model.PokerTable) -> Unit,
) {
	when (modalEffect) {
		TableDetailModalEffect.Idle -> {}

		is TableDetailModalEffect.ShowPlayerSetup -> {
			PlayerSetupSheet(
				viewModel = playerSetupViewModel,
				initialSeat = modalEffect.initialSeat,
				isHero = modalEffect.isHero,
				startingStack = modalEffect.startingStack,
				players = modalEffect.players,
				onSave = onUpdatePlayers,
				onDismiss = onDismiss,
			)
		}

		is TableDetailModalEffect.ShowTableEdit -> {
			TableEditSheet(
				table = modalEffect.table,
				onSaved = onTableSaved,
				onDismiss = onDismiss,
			)
		}
	}
}

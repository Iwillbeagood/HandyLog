package com.hand.log.table

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hand.log.navigation.interop.LocalNavigateActionInterop
import com.hand.log.domain.model.Blinds
import com.hand.log.domain.model.GameType
import com.hand.log.domain.model.Player
import com.hand.log.playersetup.PlayerSetupSheet
import com.hand.log.playersetup.PlayerSetupViewModel
import com.hand.log.table.contract.TableDetailModalEffect
import com.hand.log.table.contract.TableDetailState
import com.hand.log.ui.table.TableFormSheet
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
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
		onUpdateTable = viewModel::updateTable,
	)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TableDetailModalContent(
	modalEffect: TableDetailModalEffect,
	playerSetupViewModel: PlayerSetupViewModel,
	onDismiss: () -> Unit,
	onUpdatePlayers: (List<Player>) -> Unit,
	onUpdateTable: (String, String?, GameType, Double, Blinds?, Int, Int) -> Unit,
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
			TableFormSheet(
				table = modalEffect.table,
				onDismissRequest = onDismiss,
				onSubmit = onUpdateTable,
			)
		}
	}
}

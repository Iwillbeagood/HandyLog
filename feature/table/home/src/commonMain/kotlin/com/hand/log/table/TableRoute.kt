package com.hand.log.table

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hand.log.navigation.interop.LocalNavigateActionInterop
import com.hand.log.navigation.interop.LocalMainActionInterop
import com.hand.log.playersetup.PlayerSetupSheet
import com.hand.log.table.contract.TableEffect
import com.hand.log.table.contract.TableModalEffect
import com.hand.log.designsystem.component.modal.DefaultDialog
import com.hand.log.tableedit.TableEditSheet
import org.jetbrains.compose.resources.stringResource
import handylog.core.res.generated.resources.Res
import handylog.core.res.generated.resources.*

@Composable
internal fun TableRoute(
	viewModel: TableViewModel,
	openSeat: Int = 0,
) {
	val state by viewModel.state.collectAsStateWithLifecycle()
	val modalEffect by viewModel.modalEffect.collectAsStateWithLifecycle()
	val navAction = LocalNavigateActionInterop.current
	val mainAction = LocalMainActionInterop.current

	LaunchedEffect(openSeat) {
		if (openSeat > 0) {
			viewModel.showPlayerSetup(openSeat)
		}
	}

	TableScreen(
		state = state,
		onBack = navAction::popBackStack,
		onNavigateToRecordHand = viewModel::navigateToRecordHand,
		onNavigateToHandDetail = navAction::navigateToHandDetail,
		onShowDeleteConfirm = viewModel::showDeleteConfirm,
		onSeatClick = viewModel::showPlayerSetup,
		onShowTableEdit = viewModel::showTableEdit,
	)

	TableModalContent(
		modalEffect = modalEffect,
		onDismiss = viewModel::dismissModal,
		onPlayerSaved = viewModel::onPlayerSaved,
		onPlayerDeleted = viewModel::onPlayerDeleted,
		onTableSaved = { isEdit -> viewModel.onTableSaved(isEdit) },
		onDeleteTable = viewModel::deleteTable,
	)

	LaunchedEffect(Unit) {
		viewModel.effect.collect { effect ->
			when (effect) {
				is TableEffect.PlayerAdded -> mainAction.onShowToast(Res.string.table_detail_player_added)
				is TableEffect.PlayerUpdated -> mainAction.onShowToast(Res.string.table_detail_player_updated)
				is TableEffect.PlayerDeleted -> mainAction.onShowToast(Res.string.table_detail_player_deleted)
				is TableEffect.HandDeleted -> mainAction.onShowToast(Res.string.table_detail_hand_deleted)
				is TableEffect.TableCreated -> mainAction.onShowToast(Res.string.table_detail_table_created)
				is TableEffect.TableUpdated -> mainAction.onShowToast(Res.string.table_detail_table_updated)
				is TableEffect.TableDeleted -> {
					mainAction.onShowToast(Res.string.home_table_deleted)
					navAction.popBackStack()
				}
				is TableEffect.NavigateToRecordHand -> navAction.navigateToRecordHand(effect.tableId)
			}
		}
	}
}

@Composable
private fun TableModalContent(
	modalEffect: TableModalEffect,
	onDismiss: () -> Unit,
	onPlayerSaved: (Boolean) -> Unit,
	onPlayerDeleted: () -> Unit,
	onTableSaved: (Boolean) -> Unit,
	onDeleteTable: () -> Unit,
) {
	when (modalEffect) {
		TableModalEffect.Idle -> {}

		is TableModalEffect.ShowPlayerSetup -> {
			PlayerSetupSheet(
				tableId = modalEffect.tableId,
				initialSeat = modalEffect.initialSeat,
				player = modalEffect.player,
				occupiedSeats = modalEffect.occupiedSeats,
				maxSeat = modalEffect.maxPlayers,
				onSaved = onPlayerSaved,
				onDeleted = onPlayerDeleted,
				onDismiss = onDismiss,
			)
		}

		is TableModalEffect.ShowTableEdit -> {
			TableEditSheet(
				table = modalEffect.table,
				onSaved = { _, isEdit -> onTableSaved(isEdit) },
				onDismiss = onDismiss,
			)
		}

		TableModalEffect.ShowDeleteConfirm -> {
			DefaultDialog(
				title = stringResource(Res.string.table_delete_title),
				content = stringResource(Res.string.table_delete_description),
				onDismissRequest = onDismiss,
				onConfirmClick = onDeleteTable,
				onDismissClick = onDismiss,
			)
		}
	}
}

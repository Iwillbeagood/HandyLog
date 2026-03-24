package com.hand.log.table

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hand.log.navigation.interop.LocalNavigateActionInterop
import com.hand.log.domain.model.Player
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
) {
	val state by viewModel.state.collectAsStateWithLifecycle()
	val modalEffect by viewModel.modalEffect.collectAsStateWithLifecycle()
	val navAction = LocalNavigateActionInterop.current
	val mainAction = LocalMainActionInterop.current

	TableScreen(
		state = state,
		onBack = navAction::popBackStack,
		onNavigateToRecordHand = viewModel::navigateToRecordHand,
		onNavigateToHandDetail = navAction::navigateToHandDetail,
		onDeleteHand = viewModel::deleteHand,
		onShowDeleteConfirm = viewModel::showDeleteConfirm,
		onSeatClick = viewModel::showPlayerSetup,
		onShowTableEdit = viewModel::showTableEdit,
	)

	TableModalContent(
		modalEffect = modalEffect,
		onDismiss = viewModel::dismissModal,
		onUpdatePlayers = viewModel::updatePlayers,
		onTableSaved = viewModel::onTableSaved,
		onDeleteTable = viewModel::deleteTable,
	)

	LaunchedEffect(Unit) {
		viewModel.effect.collect { effect ->
			when (effect) {
				is TableEffect.PlayerSaved -> mainAction.onShowToast(Res.string.table_detail_player_saved)
				is TableEffect.HandDeleted -> mainAction.onShowToast(Res.string.table_detail_hand_deleted)
				is TableEffect.TableUpdated -> mainAction.onShowToast(
					Res.string.table_detail_table_updated,
				)
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
	onUpdatePlayers: (List<Player>) -> Unit,
	onTableSaved: () -> Unit,
	onDeleteTable: () -> Unit,
) {
	when (modalEffect) {
		TableModalEffect.Idle -> {}

		is TableModalEffect.ShowPlayerSetup -> {
			PlayerSetupSheet(
				initialSeat = modalEffect.initialSeat,
				isHero = modalEffect.isHero,
				startingStack = modalEffect.startingStack,
				players = modalEffect.players,
				onSave = onUpdatePlayers,
				onDismiss = onDismiss,
			)
		}

		is TableModalEffect.ShowTableEdit -> {
			TableEditSheet(
				table = modalEffect.table,
				onSaved = { onTableSaved() },
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

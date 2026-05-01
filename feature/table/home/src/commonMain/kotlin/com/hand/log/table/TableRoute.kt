package com.hand.log.table

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hand.log.navigation.interop.LocalNavigateActionInterop
import com.hand.log.navigation.interop.LocalMainActionInterop
import com.hand.log.playersetup.PlayerSetupSheet
import com.hand.log.table.component.HeroSeatSwapSheet
import com.hand.log.table.component.PlayerPositionSetupSheet
import com.hand.log.table.component.TableBalanceSheet
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
		onShowDeleteConfirm = viewModel::showDeleteConfirm,
		onSeatClick = viewModel::showPlayerSetup,
		onShowTableEdit = viewModel::showTableEdit,
		onBalanceClick = viewModel::showTableBalance,
	)

	TableModalContent(
		modalEffect = modalEffect,
		onDismiss = viewModel::dismissModal,
		onPlayerSaved = viewModel::onPlayerSaved,
		onPlayerDeleted = viewModel::onPlayerDeleted,
		onTableSaved = viewModel::onTableSaved,
		onDeleteTable = viewModel::deleteTable,
		onPlayerPositionsConfirmed = viewModel::savePlayerPositions,
		onPositionSetupDismiss = viewModel::dismissPositionSetup,
		onBalanceConfirmed = viewModel::applyTableBalance,
		onSwapHeroSeat = viewModel::swapHeroSeat,
	)

	LaunchedEffect(Unit) {
		viewModel.effect.collect { effect ->
			when (effect) {
				is TableEffect.ShowToast -> mainAction.onShowToast(effect.message)
				is TableEffect.ShowToastAndPop -> {
					mainAction.onShowToast(effect.message)
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
	onPlayerPositionsConfirmed: (Set<Int>) -> Unit,
	onPositionSetupDismiss: () -> Unit,
	onBalanceConfirmed: (heroSeat: Int, otherSeats: Set<Int>) -> Unit,
	onSwapHeroSeat: (Int) -> Unit,
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

		is TableModalEffect.ShowHeroSeatSwap -> {
			HeroSeatSwapSheet(
				maxPlayers = modalEffect.maxPlayers,
				heroSeat = modalEffect.heroSeat,
				onSeatSelected = onSwapHeroSeat,
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

		is TableModalEffect.ShowTableBalance -> {
			TableBalanceSheet(
				table = modalEffect.table,
				onConfirm = onBalanceConfirmed,
				onDismiss = onDismiss,
			)
		}

		is TableModalEffect.ShowPlayerPositionSetup -> {
			PlayerPositionSetupSheet(
				maxPlayers = modalEffect.maxPlayers,
				heroSeat = modalEffect.heroSeat,
				playerCount = modalEffect.playerCount,
				onConfirm = onPlayerPositionsConfirmed,
				onDismiss = onPositionSetupDismiss,
			)
		}
	}
}

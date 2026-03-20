package com.hand.log.tableedit

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hand.log.domain.model.PokerTable
import com.hand.log.tableedit.contract.TableEditEffect
import com.hand.log.designsystem.component.modal.HandyBottomSheet
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun TableEditSheet(
	table: PokerTable? = null,
	onSaved: (PokerTable) -> Unit,
	onDismiss: () -> Unit,
) {
	TableEditSheetContent(
		table = table,
		onSaved = onSaved,
		onDismiss = onDismiss,
	)
}

@Composable
private fun TableEditSheetContent(
	table: PokerTable? = null,
	onSaved: (PokerTable) -> Unit,
	onDismiss: () -> Unit,
	viewModel: TableEditViewModel = koinViewModel(),
) {
	val state by viewModel.state.collectAsStateWithLifecycle()

	LaunchedEffect(table?.id) {
		viewModel.initialize(table)
	}

	LaunchedEffect(Unit) {
		viewModel.effect.collect { effect ->
			when (effect) {
				is TableEditEffect.SaveComplete -> {
					onSaved(effect.table)
					onDismiss()
				}
			}
		}
	}

	HandyBottomSheet(
		onDismissRequest = onDismiss,
		title = state.title,
		confirmText = state.buttonText,
		onConfirm = viewModel::submit,
		confirmEnabled = state.isSubmitEnabled,
	) {
		TableFormFields(
			date = state.date,
			onDateChange = viewModel::updateDate,
			location = state.location,
			onLocationChange = viewModel::updateLocation,
			gameType = state.gameType,
			onGameTypeChange = viewModel::updateGameType,
			startingStack = state.startingStack,
			onStartingStackChange = viewModel::updateStartingStack,
			sbText = state.sbText,
			onSbChange = viewModel::updateSb,
			bbText = state.bbText,
			onBbChange = viewModel::updateBb,
			straddleEnabled = state.straddleEnabled,
			onStraddleEnabledChange = viewModel::updateStraddleEnabled,
			straddleText = state.straddleText,
			onStraddleChange = viewModel::updateStraddle,
			bigBlindAnteEnabled = state.bigBlindAnteEnabled,
			onBigBlindAnteChange = viewModel::updateBigBlindAnte,
			playerCount = state.playerCount,
			onPlayerCountChange = viewModel::updatePlayerCount,
			heroSeat = state.heroSeat,
			onHeroSeatChange = viewModel::updateHeroSeat,
		)
	}
}

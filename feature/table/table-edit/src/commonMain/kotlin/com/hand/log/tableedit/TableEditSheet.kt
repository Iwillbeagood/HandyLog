package com.hand.log.tableedit

import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hand.log.domain.model.PokerTable
import com.hand.log.tableedit.contract.TableEditEffect
import com.hand.log.designsystem.component.modal.HandyBottomSheet
import handylog.core.res.generated.resources.Res
import handylog.core.res.generated.resources.*
import org.jetbrains.compose.resources.stringResource
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TableEditSheetContent(
	table: PokerTable? = null,
	onSaved: (PokerTable) -> Unit,
	onDismiss: () -> Unit,
	viewModel: TableEditViewModel = koinViewModel(),
) {
	val state by viewModel.state.collectAsStateWithLifecycle()
	var showDatePicker by remember { mutableStateOf(false) }
	val datePickerState = rememberDatePickerState()

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
		title = if (state.isEditMode) {
			stringResource(
				Res.string.table_edit_title,
			)
		} else {
			stringResource(Res.string.table_edit_create_title)
		},
		confirmText = if (state.isEditMode) {
			stringResource(
				Res.string.table_edit_button,
			)
		} else {
			stringResource(Res.string.table_edit_create_button)
		},
		onConfirm = viewModel::submit,
		confirmEnabled = state.isSubmitEnabled,
	) {
		TableFormFields(
			date = state.date,
			onDateClick = { showDatePicker = true },
			location = state.location,
			onLocationChange = viewModel::updateLocation,
			isCash = state.isCash,
			onIsCashChange = viewModel::updateIsCash,
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
			maxPlayers = state.maxPlayers,
			onMaxPlayersChange = viewModel::updateMaxPlayers,
			playerCount = state.playerCount,
			onPlayerCountChange = viewModel::updatePlayerCount,
			heroSeat = state.heroSeat,
			onHeroSeatChange = viewModel::updateHeroSeat,
		)
	}

	if (showDatePicker) {
		DatePickerDialog(
			onDismissRequest = { showDatePicker = false },
			confirmButton = {
				TextButton(onClick = {
					datePickerState.selectedDateMillis?.let(viewModel::updateDateMillis)
					showDatePicker = false
				}) {
					Text(stringResource(Res.string.btn_confirm))
				}
			},
			dismissButton = {
				TextButton(onClick = { showDatePicker = false }) {
					Text(stringResource(Res.string.btn_cancel))
				}
			},
		) {
			DatePicker(state = datePickerState)
		}
	}
}

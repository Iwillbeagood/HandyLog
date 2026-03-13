package com.hand.log.record

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hand.log.designsystem.component.HmFadeAnimatedVisibility
import com.hand.log.navigation.interop.LocalNavigateActionInterop
import com.hand.log.record.contract.RecordHandEffect
import com.hand.log.record.contract.RecordHandState

@Composable
internal fun RecordHandRoute(
	viewModel: RecordHandViewModel,
) {
	val state by viewModel.state.collectAsStateWithLifecycle()
	val navAction = LocalNavigateActionInterop.current

	LaunchedEffect(Unit) {
		viewModel.effect.collect { effect ->
			when (effect) {
				is RecordHandEffect.SaveSuccess -> navAction.popBackStack()
				is RecordHandEffect.SaveError -> {}
			}
		}
	}

	HmFadeAnimatedVisibility(state is RecordHandState.Recording) {
		if (state is RecordHandState.Recording) {
			RecordHandScreen(
				state = state as RecordHandState.Recording,
				onBack = navAction::popBackStack,
				onSelectHeroCard = viewModel::selectHeroCard,
				onSelectBoardCard = viewModel::selectBoardCard,
				onCardSelected = viewModel::onCardSelected,
				onCloseCardSelector = viewModel::closeCardSelector,
				onUpdateHeroStack = viewModel::updateHeroStack,
				onUpdateButtonSeat = viewModel::updateButtonSeat,
				onUpdateBlinds = viewModel::updateBlinds,
				onSelectActionSeat = viewModel::selectActionSeat,
				onSelectActionType = viewModel::selectActionType,
				onUpdateActionAmount = viewModel::updateActionAmount,
				onConfirmAction = viewModel::confirmAction,
				onRemoveLastAction = viewModel::removeLastAction,
				onNextStep = viewModel::nextStep,
				onPreviousStep = viewModel::previousStep,
				onUpdateResult = viewModel::updateResult,
				onUpdateMemo = viewModel::updateMemo,
				onSave = viewModel::saveHand,
			)
		}
	}
}

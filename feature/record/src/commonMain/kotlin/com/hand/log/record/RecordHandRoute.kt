package com.hand.log.record

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hand.log.designsystem.component.HmFadeAnimatedVisibility
import com.hand.log.navigation.interop.LocalNavigateActionInterop
import com.hand.log.record.contract.CardSelectorTarget
import com.hand.log.record.contract.RecordHandEffect
import com.hand.log.record.contract.RecordHandModalEffect
import com.hand.log.record.contract.RecordHandState
import com.hand.log.ui.poker.CardSelectorSheet

@Composable
internal fun RecordHandRoute(
	viewModel: RecordHandViewModel,
) {
	val state by viewModel.state.collectAsStateWithLifecycle()
	val modalEffect by viewModel.modalEffect.collectAsStateWithLifecycle()
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
				onUpdateHeroStack = viewModel::updateHeroStack,
				onUpdateButtonSeat = viewModel::updateButtonSeat,
				onUpdateBlinds = viewModel::updateBlinds,
				onSelectActionSeat = viewModel::selectActionSeat,
				onSelectActionType = viewModel::selectActionType,
				onUpdateActionAmount = viewModel::updateActionAmount,
				onUpdatePlayerStack = viewModel::updatePlayerStack,
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

	// Card Selector Modal
	when (val modal = modalEffect) {
		RecordHandModalEffect.Idle -> {}
		is RecordHandModalEffect.ShowCardSelector -> {
			val title = when (modal.target) {
				is CardSelectorTarget.HeroCard -> "히어로 카드 선택"
				is CardSelectorTarget.BoardCard -> "${modal.target.street.label} 카드 선택"
			}
			CardSelectorSheet(
				title = title,
				maxCards = modal.target.maxCards,
				selectedCards = modal.selectedCards,
				onCardsSelected = viewModel::onCardsSelected,
				onDismiss = viewModel::dismissModal,
			)
		}
	}
}

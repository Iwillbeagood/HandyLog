package com.hand.log.record

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import com.hand.log.domain.model.Blinds
import com.hand.log.domain.model.Card
import com.hand.log.domain.model.GameType
import com.hand.log.navigation.interop.LocalNavigateActionInterop
import com.hand.log.record.contract.CardSelectorTarget
import com.hand.log.record.contract.RecordHandEffect
import com.hand.log.record.contract.RecordHandModalEffect
import com.hand.log.record.contract.RecordHandState
import com.hand.log.record.contract.RecordStep
import com.hand.log.ui.poker.CardSelectorSheet
import com.hand.log.ui.table.TableFormSheet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun RecordHandRoute(
	viewModel: RecordHandViewModel,
) {
	val state by viewModel.state.collectAsStateWithLifecycle()
	val modalEffect by viewModel.modalEffect.collectAsStateWithLifecycle()
	val navAction = LocalNavigateActionInterop.current

	val recording = state as? RecordHandState.Recording
	val handleBack = remember(recording?.currentStep, recording?.streets) {
		{
			val current = recording ?: return@remember
			val hasActions = current.streets.getActions(current.currentStreet).isNotEmpty()
			when {
				hasActions && current.currentStep != RecordStep.SETUP -> {
					viewModel.removeLastAction()
				}
				current.currentStep != RecordStep.SETUP -> {
					viewModel.previousStep()
				}
				else -> {
					navAction.popBackStack()
				}
			}
		}
	}

	NavigationBackHandler(
		state = rememberNavigationEventState(NavigationEventInfo.None),
		isBackEnabled = recording != null,
		onBackCompleted = handleBack,
	)

	RecordHandContent(
		state = state,
		viewModel = viewModel,
		onBack = handleBack,
	)

	RecordHandModalContent(
		modalEffect = modalEffect,
		onCardsSelected = viewModel::onCardsSelected,
		onSetShowdownUnknown = viewModel::setShowdownUnknown,
		onUpdateTable = viewModel::updateTable,
		onDismiss = viewModel::dismissModal,
	)

	LaunchedEffect(Unit) {
		viewModel.effect.collect { effect ->
			when (effect) {
				is RecordHandEffect.SaveSuccess -> navAction.popBackStack()
				is RecordHandEffect.SaveError -> {}
			}
		}
	}
}

@Composable
private fun RecordHandContent(
	state: RecordHandState,
	viewModel: RecordHandViewModel,
	onBack: () -> Unit,
) {
	if (state is RecordHandState.Recording) {
		RecordHandScreen(
			state = state,
			onBack = onBack,
			onSelectHeroCard = viewModel::selectHeroCard,
			onSelectBoardCard = viewModel::selectBoardCard,
			onSelectSingleBoardCard = viewModel::selectSingleBoardCard,
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
			onSelectShowdownCard = viewModel::selectShowdownCard,
			onUpdateResult = viewModel::updateResult,
			onUpdateMemo = viewModel::updateMemo,
			onShowTableEdit = viewModel::showTableEdit,
			onToggleBbUnit = viewModel::toggleBbUnit,
			onSave = viewModel::saveHand,
		)
	}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RecordHandModalContent(
	modalEffect: RecordHandModalEffect,
	onCardsSelected: (List<Card>) -> Unit,
	onSetShowdownUnknown: (Int) -> Unit,
	onUpdateTable: (String, String?, GameType, Double, Blinds?, Int, Int) -> Unit,
	onDismiss: () -> Unit,
) {
	when (modalEffect) {
		RecordHandModalEffect.Idle -> {}

		is RecordHandModalEffect.ShowCardSelector -> {
			val isShowdown = modalEffect.target is CardSelectorTarget.ShowdownCard
			CardSelectorSheet(
				title = modalEffect.title,
				maxCards = modalEffect.target.maxCards,
				selectedCards = modalEffect.selectedCards,
				onCardsSelected = onCardsSelected,
				onDismiss = onDismiss,
				onUnknownSelected = if (isShowdown) {
					{ onSetShowdownUnknown(modalEffect.target.seat) }
				} else {
					null
				},
			)
		}

		is RecordHandModalEffect.ShowTableEdit -> {
			TableFormSheet(
				table = modalEffect.table,
				onDismissRequest = onDismiss,
				onSubmit = onUpdateTable,
			)
		}
	}
}

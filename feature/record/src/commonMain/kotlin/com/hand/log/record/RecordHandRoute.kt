package com.hand.log.record

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import handylog.core.res.generated.resources.Res
import handylog.core.res.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import androidx.compose.ui.focus.FocusRequester
import com.hand.log.domain.model.Card
import com.hand.log.domain.model.PokerTable
import com.hand.log.record.component.StepBackConfirmDialog
import com.hand.log.navigation.interop.LocalNavigateActionInterop
import com.hand.log.navigation.interop.LocalMainActionInterop
import com.hand.log.record.contract.CardSelectorTarget
import com.hand.log.record.contract.RecordHandEffect
import com.hand.log.record.contract.RecordHandModalEffect
import com.hand.log.record.contract.RecordHandState
import com.hand.log.record.contract.RecordStep
import com.hand.log.ui.stringRes
import com.hand.log.tableedit.TableEditSheet
import com.hand.log.ui.poker.CardSelectorSheet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun RecordHandRoute(
	viewModel: RecordHandViewModel,
) {
	val state by viewModel.state.collectAsStateWithLifecycle()
	val modalEffect by viewModel.modalEffect.collectAsStateWithLifecycle()
	val navAction = LocalNavigateActionInterop.current
	val mainAction = LocalMainActionInterop.current

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

	val bbFocusRequester = remember { FocusRequester() }

	RecordHandContent(
		state = state,
		viewModel = viewModel,
		onBack = handleBack,
		bbFocusRequester = bbFocusRequester,
	)

	RecordHandModalContent(
		modalEffect = modalEffect,
		onCardsSelected = viewModel::onCardsSelected,
		onSetShowdownUnknown = viewModel::setShowdownUnknown,
		onTableSaved = viewModel::onTableSaved,
		onConfirmStepBack = viewModel::confirmStepBack,
		onDismiss = viewModel::dismissModal,
	)

	LaunchedEffect(Unit) {
		viewModel.effect.collect { effect ->
			when (effect) {
				is RecordHandEffect.SaveSuccess -> navAction.popBackStack()
				is RecordHandEffect.SaveError -> mainAction.onShowToast(Res.string.record_save_error)
				is RecordHandEffect.FocusBb -> bbFocusRequester.requestFocus()
			}
		}
	}
}

@Composable
private fun RecordHandContent(
	state: RecordHandState,
	viewModel: RecordHandViewModel,
	onBack: () -> Unit,
	bbFocusRequester: FocusRequester = FocusRequester(),
) {
	if (state is RecordHandState.Recording) {
		RecordHandScreen(
			state = state,
			onBack = onBack,
			onSelectHeroCard = viewModel::selectHeroCard,
			onSelectAllBoardCards = viewModel::selectAllBoardCards,
			onSelectBoardCard = viewModel::selectBoardCard,
			onSelectSingleBoardCard = viewModel::selectSingleBoardCard,
			onUpdateHeroStack = viewModel::updateHeroStack,
			onUpdateButtonSeat = viewModel::updateButtonSeat,
			onUpdateBb = viewModel::updateBb,
			onUpdateSb = viewModel::updateSb,
			onSelectActionSeat = viewModel::selectActionSeat,
			onSelectActionType = viewModel::selectActionType,
			onUpdateActionAmount = viewModel::updateActionAmount,
			onUpdatePlayerStack = viewModel::updatePlayerStack,
			onConfirmAction = viewModel::confirmAction,
			onRemoveLastAction = viewModel::removeLastAction,
			onNextStep = viewModel::nextStep,
			onPreviousStep = viewModel::previousStep,
			onNavigateToStep = viewModel::navigateToStep,
			onSelectShowdownCard = viewModel::selectShowdownCard,
			onUpdateMemo = viewModel::updateMemo,
			onToggleBbUnit = viewModel::toggleBbUnit,
			onSave = viewModel::saveHand,
			bbFocusRequester = bbFocusRequester,
		)
	}
}

@Composable
private fun RecordHandModalContent(
	modalEffect: RecordHandModalEffect,
	onCardsSelected: (List<Card>) -> Unit,
	onSetShowdownUnknown: (Int) -> Unit,
	onTableSaved: (PokerTable) -> Unit,
	onConfirmStepBack: (RecordStep, Boolean, Boolean) -> Unit,
	onDismiss: () -> Unit,
) {
	when (modalEffect) {
		RecordHandModalEffect.Idle -> {}

		is RecordHandModalEffect.ShowCardSelector -> {
			val isShowdown = modalEffect.target is CardSelectorTarget.ShowdownCard
			val title = when (val target = modalEffect.target) {
				is CardSelectorTarget.HeroCard -> stringResource(Res.string.card_selector_hero)
				is CardSelectorTarget.AllBoardCards -> stringResource(Res.string.board_cards)
				is CardSelectorTarget.BoardCard -> stringResource(
					Res.string.card_selector_board,
					stringResource(target.street.stringRes()),
				)
				is CardSelectorTarget.SingleBoardCard -> stringResource(
					Res.string.card_selector_board_change,
					stringResource(target.street.stringRes()),
				)
				is CardSelectorTarget.ShowdownCard -> stringResource(
					Res.string.card_selector_showdown,
					target.positionName,
				)
			}
			val isAllBoard = modalEffect.target is CardSelectorTarget.AllBoardCards
			CardSelectorSheet(
				title = title,
				maxCards = modalEffect.target.maxCards,
				selectedCards = modalEffect.selectedCards,
				onCardsSelected = onCardsSelected,
				onDismiss = onDismiss,
				onUnknownSelected = if (isShowdown && modalEffect.allowUnknown) {
					{ onSetShowdownUnknown(modalEffect.target.seat) }
				} else {
					null
				},
				initialCards = modalEffect.initialCards,
				minCards = if (isAllBoard) 3 else modalEffect.target.maxCards,
				boardPreview = isAllBoard,
			)
		}

		is RecordHandModalEffect.ShowTableEdit -> {
			TableEditSheet(
				table = modalEffect.table,
				onSaved = { table, _ -> onTableSaved(table) },
				onDismiss = onDismiss,
			)
		}

		is RecordHandModalEffect.ConfirmStepBack -> {
			StepBackConfirmDialog(
				targetStep = modalEffect.targetStep,
				onConfirm = { step, skipWarning ->
					onConfirmStepBack(step, skipWarning, modalEffect.undoLastAction)
				},
				onDismiss = onDismiss,
			)
		}

	}
}

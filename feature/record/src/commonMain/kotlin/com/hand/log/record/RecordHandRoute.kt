package com.hand.log.record

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.hand.log.designsystem.component.HandyCheckBox
import com.hand.log.designsystem.component.VerticalSpacer
import com.hand.log.designsystem.component.modal.ButtonDialog
import com.hand.log.designsystem.theme.HandyTheme
import handylog.core.res.generated.resources.Res
import handylog.core.res.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import androidx.compose.ui.focus.FocusRequester
import com.hand.log.domain.model.ActionType
import com.hand.log.domain.model.Card
import com.hand.log.domain.model.PokerTable
import com.hand.log.record.component.EditActionSheet
import com.hand.log.navigation.interop.LocalNavigateActionInterop
import com.hand.log.navigation.interop.LocalMainActionInterop
import com.hand.log.record.contract.CardSelectorTarget
import com.hand.log.record.contract.RecordHandEffect
import com.hand.log.record.contract.RecordHandModalEffect
import com.hand.log.record.contract.RecordHandState
import com.hand.log.record.contract.RecordStep
import com.hand.log.ui.localizedLabel
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

	val heroStackFocusRequester = remember { FocusRequester() }

	RecordHandContent(
		state = state,
		modalEffect = modalEffect,
		viewModel = viewModel,
		onBack = handleBack,
		heroStackFocusRequester = heroStackFocusRequester,
	)

	RecordHandModalContent(
		modalEffect = modalEffect,
		state = state,
		onCardsSelected = viewModel::onCardsSelected,
		onSetShowdownUnknown = viewModel::setShowdownUnknown,
		onTableSaved = viewModel::onTableSaved,
		onConfirmStepBack = viewModel::confirmStepBack,
		onSelectActionType = viewModel::selectActionType,
		onUpdateActionAmount = viewModel::updateActionAmount,
		onConfirmEditAction = viewModel::restartFromAction,
		onDismiss = viewModel::dismissModal,
	)

	LaunchedEffect(Unit) {
		viewModel.effect.collect { effect ->
			when (effect) {
				is RecordHandEffect.SaveSuccess -> navAction.popBackStack()
				is RecordHandEffect.SaveError -> mainAction.onShowToast(Res.string.record_save_error)
				is RecordHandEffect.FocusHeroStack -> heroStackFocusRequester.requestFocus()
			}
		}
	}
}

@Composable
private fun RecordHandContent(
	state: RecordHandState,
	modalEffect: RecordHandModalEffect,
	viewModel: RecordHandViewModel,
	onBack: () -> Unit,
	heroStackFocusRequester: FocusRequester = FocusRequester(),
) {
	val editingActionIndex = (modalEffect as? RecordHandModalEffect.EditAction)?.actionIndex

	if (state is RecordHandState.Recording) {
		RecordHandScreen(
			state = state,
			editingActionIndex = editingActionIndex,
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
			onEditAction = viewModel::editAction,
			onResumeRecording = viewModel::resumeRecording,
			onNextStep = viewModel::nextStep,
			onPreviousStep = viewModel::previousStep,
			onNavigateToStep = viewModel::navigateToStep,
			onSelectShowdownCard = viewModel::selectShowdownCard,
			onUpdateMemo = viewModel::updateMemo,
			onToggleBbUnit = viewModel::toggleBbUnit,
			onSave = viewModel::saveHand,
			heroStackFocusRequester = heroStackFocusRequester,
		)
	}
}

@Composable
private fun RecordHandModalContent(
	modalEffect: RecordHandModalEffect,
	state: RecordHandState?,
	onCardsSelected: (List<Card>) -> Unit,
	onSetShowdownUnknown: (Int) -> Unit,
	onTableSaved: (PokerTable) -> Unit,
	onConfirmStepBack: (RecordStep, Boolean) -> Unit,
	onSelectActionType: (ActionType) -> Unit,
	onUpdateActionAmount: (String) -> Unit,
	onConfirmEditAction: (Int) -> Unit,
	onDismiss: () -> Unit,
) {
	when (modalEffect) {
		RecordHandModalEffect.Idle -> {}

		is RecordHandModalEffect.ShowCardSelector -> {
			val isShowdown = modalEffect.target is CardSelectorTarget.ShowdownCard
			val title = when (val target = modalEffect.target) {
				is CardSelectorTarget.HeroCard -> stringResource(Res.string.card_selector_hero)
				is CardSelectorTarget.BoardCard -> stringResource(
					Res.string.card_selector_board,
					target.street.localizedLabel(),
				)
				is CardSelectorTarget.SingleBoardCard -> stringResource(
					Res.string.card_selector_board_change,
					target.street.localizedLabel(),
				)
				is CardSelectorTarget.ShowdownCard -> stringResource(
					Res.string.card_selector_showdown,
					target.positionName,
				)
			}
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
				onConfirm = onConfirmStepBack,
				onDismiss = onDismiss,
			)
		}

		is RecordHandModalEffect.EditAction -> {
			val recording = state as? RecordHandState.Recording
			if (recording != null) {
				EditActionSheet(
					state = recording,
					positionName = modalEffect.positionName,
					actionIndex = modalEffect.actionIndex,
					onSelectActionType = onSelectActionType,
					onUpdateActionAmount = onUpdateActionAmount,
					onConfirmEditAction = onConfirmEditAction,
					onDismiss = onDismiss,
				)
			}
		}
	}
}

@Composable
private fun StepBackConfirmDialog(
	targetStep: RecordStep,
	onConfirm: (RecordStep, Boolean) -> Unit,
	onDismiss: () -> Unit,
) {
	var skipWarning by remember { mutableStateOf(false) }

	ButtonDialog(
		title = stringResource(Res.string.step_back_title),
		onDismissRequest = onDismiss,
		onConfirmClick = { onConfirm(targetStep, skipWarning) },
		onDismissClick = onDismiss,
	) {
		Text(
			text = stringResource(Res.string.step_back_description),
			style = HandyTheme.typography.regular14,
			textAlign = TextAlign.Center,
			color = HandyTheme.colorScheme.textSecondary,
			modifier = Modifier.fillMaxWidth(),
		)
		VerticalSpacer(16.dp)
		HandyCheckBox(
			text = stringResource(Res.string.step_back_skip_warning),
			checked = skipWarning,
			onCheckedChange = { skipWarning = it },
		)
	}
}

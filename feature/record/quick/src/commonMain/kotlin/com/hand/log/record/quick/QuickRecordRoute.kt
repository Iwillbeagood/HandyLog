package com.hand.log.record.quick

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import com.hand.log.designsystem.component.FadeAnimatedContent
import com.hand.log.designsystem.component.modal.DefaultDialog
import com.hand.log.navigation.interop.LocalMainActionInterop
import com.hand.log.navigation.interop.LocalNavigateActionInterop
import com.hand.log.record.quick.component.QuickCardPicker
import com.hand.log.record.quick.component.QuickHeroSheet
import com.hand.log.record.quick.component.QuickSetupSheet
import com.hand.log.record.quick.component.SyntaxGuideSheet
import com.hand.log.record.quick.contract.QuickRecordEffect
import com.hand.log.record.quick.contract.QuickRecordModalEffect
import handylog.core.res.generated.resources.Res
import handylog.core.res.generated.resources.quick_record_exit_desc
import handylog.core.res.generated.resources.quick_record_exit_title
import handylog.core.res.generated.resources.quick_record_card_conflict
import handylog.core.res.generated.resources.quick_record_line_invalid
import handylog.core.res.generated.resources.quick_record_setup_conflict
import handylog.core.res.generated.resources.record_save_error
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun QuickRecordRoute(
	viewModel: QuickRecordViewModel,
) {
	val state by viewModel.state.collectAsStateWithLifecycle()
	val modalEffect by viewModel.modalEffect.collectAsStateWithLifecycle()
	val navAction = LocalNavigateActionInterop.current
	val mainAction = LocalMainActionInterop.current

	NavigationBackHandler(
		state = rememberNavigationEventState(NavigationEventInfo.None),
		isBackEnabled = true,
		onBackCompleted = viewModel::requestExit,
	)

	FadeAnimatedContent(targetState = state.showsFinishScreen) { showsFinishScreen ->
		val finishForm = state.finishScreen
		if (showsFinishScreen && finishForm != null) {
			QuickFinishScreen(
				form = finishForm,
				onBack = viewModel::requestExit,
				onHeroPositionSelect = viewModel::selectHeroPosition,
				onCardsClick = viewModel::showFinishCardPicker,
				onEditActions = viewModel::editActions,
				onSave = viewModel::save,
			)
		} else if (!showsFinishScreen) {
			QuickRecordScreen(
				state = state,
				onBack = viewModel::requestExit,
				onGuideClick = viewModel::showGuide,
				onSetupClick = viewModel::showSetup,
				onHeroClick = viewModel::showHeroSelect,
				onKeyClick = viewModel::pressKey,
				onFinishClick = viewModel::showFinish,
			)
		}
	}

	when (val modal = modalEffect) {
		QuickRecordModalEffect.Idle -> Unit
		QuickRecordModalEffect.ShowGuide -> SyntaxGuideSheet(onDismiss = viewModel::dismissModal)
		is QuickRecordModalEffect.ShowSetup -> QuickSetupSheet(
			setup = modal.setup,
			onSetupChange = viewModel::updateSetup,
			onConfirm = viewModel::confirmSetup,
			onDismiss = viewModel::dismissModal,
		)
		is QuickRecordModalEffect.ShowCardPicker -> QuickCardPicker(
			picker = modal,
			onCardsSelected = { viewModel.selectCards(modal.owner, it) },
			onUnknownSelected = { viewModel.clearCards(modal.owner) },
			onDismiss = viewModel::dismissModal,
		)
		is QuickRecordModalEffect.ShowHeroSelect -> QuickHeroSheet(
			hero = modal,
			onHeroSelect = viewModel::selectHero,
			onDismiss = viewModel::dismissModal,
		)
		QuickRecordModalEffect.ConfirmExit -> DefaultDialog(
			title = stringResource(Res.string.quick_record_exit_title),
			content = stringResource(Res.string.quick_record_exit_desc),
			onDismissRequest = viewModel::dismissModal,
			onConfirmClick = viewModel::confirmExit,
		)
	}

	LaunchedEffect(Unit) {
		viewModel.effect.collect { effect ->
			when (effect) {
				QuickRecordEffect.SaveSuccess, QuickRecordEffect.NavigateBack -> navAction.popBackStack()
				QuickRecordEffect.SaveError -> mainAction.onShowToast(Res.string.record_save_error)
				QuickRecordEffect.InvalidLine -> mainAction.onShowToast(Res.string.quick_record_line_invalid)
				QuickRecordEffect.CardConflict -> mainAction.onShowToast(Res.string.quick_record_card_conflict)
				QuickRecordEffect.SetupConflict -> mainAction.onShowToast(
					Res.string.quick_record_setup_conflict,
				)
			}
		}
	}
}

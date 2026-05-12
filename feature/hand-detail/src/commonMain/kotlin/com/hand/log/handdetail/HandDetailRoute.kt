package com.hand.log.handdetail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hand.log.designsystem.component.modal.DefaultDialog
import com.hand.log.domain.model.Card
import com.hand.log.handdetail.component.MemoEditSheet
import com.hand.log.ui.poker.CardSelectorSheet
import com.hand.log.handdetail.contract.HandDetailEffect
import com.hand.log.handdetail.contract.HandDetailModalEffect
import com.hand.log.domain.model.SavedPlayer
import com.hand.log.domain.model.etc.ToastDurationType
import com.hand.log.playersedit.PlayerEditSheet
import com.hand.log.navigation.interop.LocalMainActionInterop
import com.hand.log.navigation.interop.LocalNavigateActionInterop
import com.hand.log.utils.share.rememberShareManager
import com.hand.log.utils.share.toPngBytes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import handylog.core.res.generated.resources.Res
import handylog.core.res.generated.resources.*
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun HandDetailRoute(
	viewModel: HandDetailViewModel,
) {
	val state by viewModel.state.collectAsStateWithLifecycle()
	val modalEffect by viewModel.modalEffect.collectAsStateWithLifecycle()
	val navAction = LocalNavigateActionInterop.current
	val mainAction = LocalMainActionInterop.current
	val shareManager = rememberShareManager()
	val graphicsLayer = rememberGraphicsLayer()

	HandDetailScreen(
		state = state,
		onToggleBbUnit = viewModel::toggleBbUnit,
		onBack = navAction::popBackStack,
		onShowDeleteConfirm = viewModel::showDeleteConfirm,
		onShareText = viewModel::shareText,
		onShareImage = viewModel::shareImage,
		onDownloadImage = viewModel::downloadImage,
		onMarkPlayer = viewModel::showPlayerMark,
		onEditHeroHand = viewModel::editHeroHand,
		onEditShowdownHand = viewModel::editShowdownHand,
		onMemoClick = viewModel::showMemoEdit,
		graphicsLayer = graphicsLayer,
	)

	HandDetailModalContent(
		modalEffect = modalEffect,
		onMemoSave = { text ->
			viewModel.saveMemo(text)
			viewModel.dismissModal()
		},
		onConfirmDelete = viewModel::confirmDelete,
		onSaveAndMarkPlayer = viewModel::saveAndMarkPlayer,
		onCardsSelected = viewModel::onCardsSelected,
		onDismiss = viewModel::dismissModal,
	)

	LaunchedEffect(Unit) {
		viewModel.effect.collect { effect ->
			when (effect) {
				is HandDetailEffect.HandDeleted -> navAction.popBackStack()
				is HandDetailEffect.ShareText -> {
					val copied = shareManager.shareText(effect.text)
					if (copied) {
						mainAction.onShowToast(
							getString(Res.string.clipboard_copied),
							ToastDurationType.LONG,
						)
					}
				}
				is HandDetailEffect.ShareImage -> {
					try {
						val bitmap = graphicsLayer.toImageBitmap()
						val bytes = withContext(Dispatchers.Default) {
							bitmap.toPngBytes()
						}
						shareManager.shareImage(bytes, effect.fileName)
					} catch (e: Throwable) {
						e.printStackTrace()
						mainAction.onShowToast(
							getString(Res.string.hand_detail_image_share_failed),
							ToastDurationType.SHORT,
						)
					}
				}
				is HandDetailEffect.DownloadImage -> {
					try {
						val bitmap = graphicsLayer.toImageBitmap()
						val bytes = withContext(Dispatchers.Default) {
							bitmap.toPngBytes()
						}
						shareManager.saveImage(bytes, effect.fileName)
						mainAction.onShowToast(
							getString(Res.string.hand_detail_image_downloaded),
							ToastDurationType.SHORT,
						)
					} catch (e: Throwable) {
						e.printStackTrace()
						mainAction.onShowToast(
							getString(Res.string.hand_detail_image_save_failed),
							ToastDurationType.SHORT,
						)
					}
				}
				is HandDetailEffect.NavigateToPlayers -> {
					navAction.navigateToTableDetail(effect.tableId)
				}
			}
		}
	}
}

@Composable
private fun HandDetailModalContent(
	modalEffect: HandDetailModalEffect,
	onMemoSave: (String) -> Unit,
	onConfirmDelete: () -> Unit,
	onSaveAndMarkPlayer: (SavedPlayer, Int) -> Unit,
	onCardsSelected: (List<Card>) -> Unit,
	onDismiss: () -> Unit,
) {
	when (modalEffect) {
		HandDetailModalEffect.Idle -> {}
		is HandDetailModalEffect.EditMemo -> {
			MemoEditSheet(
				initialMemo = modalEffect.currentMemo,
				onSave = onMemoSave,
				onDismiss = onDismiss,
			)
		}
		HandDetailModalEffect.ConfirmDelete -> {
			DefaultDialog(
				title = stringResource(Res.string.hand_detail_delete_title),
				content = stringResource(Res.string.hand_detail_delete_description),
				onDismissRequest = onDismiss,
				onConfirmClick = onConfirmDelete,
				onDismissClick = onDismiss,
			)
		}
		is HandDetailModalEffect.ShowPlayerMark -> {
			PlayerEditSheet(
				player = null,
				onSave = { player -> onSaveAndMarkPlayer(player, modalEffect.seat) },
				onDismiss = onDismiss,
			)
		}
		is HandDetailModalEffect.EditHeroHand -> {
			CardSelectorSheet(
				title = stringResource(Res.string.card_selector_hero),
				maxCards = 2,
				selectedCards = modalEffect.selectedCards,
				onCardsSelected = onCardsSelected,
				onDismiss = onDismiss,
			)
		}
		is HandDetailModalEffect.EditShowdownHand -> {
			CardSelectorSheet(
				title = stringResource(
					Res.string.card_selector_showdown,
					modalEffect.positionName,
				),
				maxCards = 2,
				selectedCards = modalEffect.selectedCards,
				onCardsSelected = onCardsSelected,
				onDismiss = onDismiss,
			)
		}
	}
}

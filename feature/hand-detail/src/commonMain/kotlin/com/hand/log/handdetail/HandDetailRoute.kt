package com.hand.log.handdetail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hand.log.designsystem.component.modal.DefaultDialog
import com.hand.log.handdetail.contract.HandDetailEffect
import com.hand.log.handdetail.contract.HandDetailModalEffect
import com.hand.log.domain.model.SavedPlayer
import com.hand.log.domain.model.etc.ToastDurationType
import com.hand.log.playersedit.PlayerEditSheet
import com.hand.log.navigation.interop.LocalMainActionInterop
import com.hand.log.navigation.interop.LocalNavigateActionInterop
import com.hand.log.utils.share.rememberShareManager
import com.hand.log.utils.share.toPngBytes
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
		onEdit = { /* TODO */ },
		onShowDeleteConfirm = viewModel::showDeleteConfirm,
		onShareText = viewModel::shareText,
		onShareImage = viewModel::shareImage,
		onDownloadImage = viewModel::downloadImage,
		onMarkPlayer = viewModel::showPlayerMark,
		graphicsLayer = graphicsLayer,
	)

	HandDetailModalContent(
		modalEffect = modalEffect,
		onConfirmDelete = viewModel::confirmDelete,
		onSaveAndMarkPlayer = viewModel::saveAndMarkPlayer,
		onDismiss = viewModel::dismissModal,
	)

	LaunchedEffect(Unit) {
		viewModel.effect.collect { effect ->
			when (effect) {
				is HandDetailEffect.HandDeleted -> navAction.popBackStack()
				is HandDetailEffect.ShareText -> {
					shareManager.shareText(effect.text)
					mainAction.onShowToast(Res.string.clipboard_copied)
				}
				is HandDetailEffect.ShareImage -> {
					val bitmap = graphicsLayer.toImageBitmap()
					val bytes = bitmap.toPngBytes()
					shareManager.shareImage(bytes, effect.fileName)
				}
				is HandDetailEffect.DownloadImage -> {
					val bitmap = graphicsLayer.toImageBitmap()
					val bytes = bitmap.toPngBytes()
					shareManager.saveImage(bytes, effect.fileName)
					mainAction.onShowToast(
						getString(Res.string.hand_detail_image_downloaded),
						ToastDurationType.SHORT,
					)
				}
				is HandDetailEffect.NavigateToPlayers -> {
					navAction.navigateToPlayers(effect.tableId, effect.seat)
				}
			}
		}
	}
}

@Composable
private fun HandDetailModalContent(
	modalEffect: HandDetailModalEffect,
	onConfirmDelete: () -> Unit,
	onSaveAndMarkPlayer: (SavedPlayer, Int) -> Unit,
	onDismiss: () -> Unit,
) {
	when (modalEffect) {
		HandDetailModalEffect.Idle -> {}
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
	}
}

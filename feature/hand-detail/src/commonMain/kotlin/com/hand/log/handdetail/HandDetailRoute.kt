package com.hand.log.handdetail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hand.log.designsystem.component.modal.DefaultDialog
import com.hand.log.handdetail.component.TableExpandedDialog
import com.hand.log.handdetail.contract.HandDetailEffect
import com.hand.log.handdetail.contract.HandDetailModalEffect
import com.hand.log.domain.model.etc.ToastDurationType
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
		onShowDeleteConfirm = viewModel::showDeleteConfirm,
		onShowTableExpanded = viewModel::showTableExpanded,
		onShareText = viewModel::shareText,
		onShareImage = viewModel::requestShareImage,
		onDownloadImage = viewModel::requestDownloadImage,
		graphicsLayer = graphicsLayer,
	)

	HandDetailModalContent(
		modalEffect = modalEffect,
		onConfirmDelete = viewModel::confirmDelete,
		onDismiss = viewModel::dismissModal,
	)

	LaunchedEffect(Unit) {
		viewModel.effect.collect { effect ->
			when (effect) {
				is HandDetailEffect.HandDeleted -> navAction.popBackStack()
				is HandDetailEffect.ShareText -> shareManager.shareText(effect.text)
				is HandDetailEffect.RequestImageCapture -> {
					val bitmap = graphicsLayer.toImageBitmap()
					val bytes = bitmap.toPngBytes()
					viewModel.shareImage(bytes)
				}
				is HandDetailEffect.ShareImage -> {
					shareManager.shareImage(effect.imageBytes, effect.fileName)
				}
				is HandDetailEffect.RequestImageDownload -> {
					val bitmap = graphicsLayer.toImageBitmap()
					val bytes = bitmap.toPngBytes()
					viewModel.downloadImage(bytes)
				}
				is HandDetailEffect.DownloadImage -> {
					shareManager.saveImage(effect.imageBytes, effect.fileName)
					mainAction.onShowToast(
						getString(Res.string.hand_detail_image_downloaded),
						ToastDurationType.SHORT,
					)
				}
			}
		}
	}
}

@Composable
private fun HandDetailModalContent(
	modalEffect: HandDetailModalEffect,
	onConfirmDelete: () -> Unit,
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
		is HandDetailModalEffect.ShowTableExpanded -> {
			TableExpandedDialog(
				hand = modalEffect.hand,
				useBbUnit = modalEffect.useBbUnit,
				onDismiss = onDismiss,
			)
		}
	}
}

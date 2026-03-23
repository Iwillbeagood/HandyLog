package com.hand.log.handdetail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hand.log.handdetail.contract.HandDetailState
import com.hand.log.handdetail.model.HandHistoryFormatter
import com.hand.log.navigation.interop.LocalNavigateActionInterop
import com.hand.log.navigation.interop.rememberShowSnackBar
import com.hand.log.utils.share.rememberShareManager
import com.hand.log.utils.share.toPngBytes

@Composable
internal fun HandDetailRoute(
	viewModel: HandDetailViewModel,
) {
	val state by viewModel.state.collectAsStateWithLifecycle()
	val navAction = LocalNavigateActionInterop.current
	val showToast = rememberShowSnackBar()
	val shareManager = rememberShareManager()

	when (val current = state) {
		HandDetailState.Loading -> {}
		HandDetailState.Error -> {}
		is HandDetailState.Success -> {
			HandDetailScreen(
				state = current,
				onToggleBbUnit = viewModel::toggleBbUnit,
				onBack = navAction::popBackStack,
				onEdit = {
					navAction.navigateToRecordHand(
						tableId = current.hand.tableId,
						handId = current.hand.id,
					)
				},
				onShareText = {
					val text = HandHistoryFormatter.format(current.hand)
					shareManager.shareText(text)
					showToast("텍스트가 공유되었습니다")
				},
				onShareImage = { imageBitmap ->
					val bytes = imageBitmap.toPngBytes()
					shareManager.shareImage(bytes, "hand_${current.hand.id}.png")
					showToast("이미지가 공유되었습니다")
				},
			)
		}
	}
}

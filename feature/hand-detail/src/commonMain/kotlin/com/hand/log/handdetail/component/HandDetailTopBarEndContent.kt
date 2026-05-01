package com.hand.log.handdetail.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.hand.log.designsystem.component.HandyMenuItem
import com.hand.log.designsystem.component.HandyPopupMenu
import com.hand.log.designsystem.component.TopAppbarIcon
import com.hand.log.designsystem.etc.ThemePreview
import com.hand.log.designsystem.etc.ThemePreviews
import com.hand.log.designsystem.theme.HandyTheme
import handylog.core.res.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun HandDetailTopBarEndContent(
	onShowDeleteConfirm: () -> Unit,
	onShareText: () -> Unit,
	onShareImage: () -> Unit,
	onDownloadImage: () -> Unit,
) {
	val colors = HandyTheme.colorScheme

	Row {
		TopAppbarIcon(
			icon = Res.drawable.delete,
			tint = colors.error,
			onClick = onShowDeleteConfirm,
		)
		Box {
			var showShareMenu by remember { mutableStateOf(false) }

			TopAppbarIcon(
				tint = colors.textPrimary,
				icon = Res.drawable.share_2,
				onClick = { showShareMenu = true },
			)

			HandyPopupMenu(
				expanded = showShareMenu,
				onDismissRequest = { showShareMenu = false },
				items = listOf(
					HandyMenuItem(
						icon = Res.drawable.file_text,
						text = stringResource(Res.string.hand_detail_share_text),
						onClick = onShareText,
					),
					HandyMenuItem(
						icon = Res.drawable.image,
						text = stringResource(Res.string.hand_detail_share_image),
						onClick = onShareImage,
					),
					HandyMenuItem(
						icon = Res.drawable.images,
						text = stringResource(Res.string.hand_detail_download_image),
						onClick = onDownloadImage,
					),
				),
			)
		}
	}
}

@ThemePreviews
@Composable
private fun HandDetailTopBarEndContentPreview() {
	ThemePreview {
		HandDetailTopBarEndContent(
			onShowDeleteConfirm = {},
			onShareText = {},
			onShareImage = {},
			onDownloadImage = {},
		)
	}
}

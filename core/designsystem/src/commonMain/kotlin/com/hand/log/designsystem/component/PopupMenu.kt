package com.hand.log.designsystem.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hand.log.designsystem.etc.ThemePreview
import com.hand.log.designsystem.etc.ThemePreviews
import com.hand.log.designsystem.theme.HandyTheme
import handylog.core.res.generated.resources.Res
import handylog.core.res.generated.resources.file_text
import handylog.core.res.generated.resources.image
import handylog.core.res.generated.resources.share_2
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

data class HandyMenuItem(
	val icon: DrawableResource,
	val text: String,
	val onClick: () -> Unit,
)

@Composable
fun HandyPopupMenu(
	expanded: Boolean,
	onDismissRequest: () -> Unit,
	items: List<HandyMenuItem>,
	modifier: Modifier = Modifier,
) {
	val colors = HandyTheme.colorScheme

	DropdownMenu(
		expanded = expanded,
		onDismissRequest = onDismissRequest,
		containerColor = colors.card,
		shadowElevation = 8.dp,
		shape = RoundedCornerShape(8.dp),
		modifier = modifier,
	) {
		items.forEach { item ->
			Row(
				modifier = Modifier
					.fillMaxWidth()
					.clickable {
						onDismissRequest()
						item.onClick()
					}
					.padding(horizontal = 16.dp, vertical = 12.dp),
				verticalAlignment = Alignment.CenterVertically,
			) {
				Icon(
					painter = painterResource(item.icon),
					contentDescription = null,
					modifier = Modifier.size(18.dp),
					tint = colors.textSecondary,
				)
				Spacer(modifier = Modifier.width(12.dp))
				Text(
					text = item.text,
					style = HandyTheme.typography.medium14,
					color = colors.textPrimary,
				)
			}
		}
	}
}

@ThemePreviews
@Composable
private fun HandyPopupMenuPreview() {
	ThemePreview {
		Box(modifier = Modifier.padding(16.dp)) {
			HandyPopupMenu(
				expanded = true,
				onDismissRequest = {},
				items = listOf(
					HandyMenuItem(
						icon = Res.drawable.file_text,
						text = "텍스트 복사",
						onClick = {},
					),
					HandyMenuItem(
						icon = Res.drawable.image,
						text = "이미지 공유",
						onClick = {},
					),
					HandyMenuItem(
						icon = Res.drawable.share_2,
						text = "공유하기",
						onClick = {},
					),
				),
			)
		}
	}
}

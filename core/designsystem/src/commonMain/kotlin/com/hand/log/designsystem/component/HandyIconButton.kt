package com.hand.log.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.hand.log.designsystem.etc.ThemePreview
import com.hand.log.designsystem.etc.ThemePreviews
import com.hand.log.designsystem.etc.clickableSingle
import com.hand.log.designsystem.theme.HandyTheme
import handylog.core.res.generated.resources.Res
import handylog.core.res.generated.resources.delete
import handylog.core.res.generated.resources.pencil
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

/**
 * 아이콘 버튼 - 원형 클릭 영역 + 싱글 클릭
 */
@Composable
fun HandyIconButton(
	icon: DrawableResource,
	onClick: () -> Unit,
	modifier: Modifier = Modifier,
	contentDescription: String? = null,
	tint: Color = HandyTheme.colorScheme.textSecondary,
	size: Dp = 36.dp,
	iconSize: Dp = 18.dp,
) {
	Icon(
		painter = painterResource(icon),
		contentDescription = contentDescription,
		modifier = modifier
			.size(size)
			.clip(CircleShape)
			.clickableSingle(onClick = onClick)
			.padding((size - iconSize) / 2),
		tint = tint,
	)
}

@ThemePreviews
@Composable
private fun HandyIconButtonPreview() {
	ThemePreview {
		Row(
			horizontalArrangement = Arrangement.spacedBy(8.dp),
			modifier = Modifier.padding(16.dp),
		) {
			HandyIconButton(
				icon = Res.drawable.pencil,
				onClick = {},
				contentDescription = "수정",
			)
			HandyIconButton(
				icon = Res.drawable.delete,
				onClick = {},
				contentDescription = "삭제",
				tint = HandyTheme.colorScheme.error,
			)
		}
	}
}

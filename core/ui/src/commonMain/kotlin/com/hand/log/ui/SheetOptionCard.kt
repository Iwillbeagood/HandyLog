package com.hand.log.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.hand.log.designsystem.etc.ThemePreview
import com.hand.log.designsystem.etc.ThemePreviews
import com.hand.log.designsystem.etc.clickableSingle
import com.hand.log.designsystem.theme.HandyTheme
import handylog.core.res.generated.resources.Res
import handylog.core.res.generated.resources.chevron_right
import handylog.core.res.generated.resources.file_text
import handylog.core.res.generated.resources.grid_3x3
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

@Composable
fun SheetOptionCard(
	icon: DrawableResource,
	title: String,
	description: String,
	onClick: () -> Unit,
	modifier: Modifier = Modifier,
	badge: String? = null,
) {
	val colors = HandyTheme.colorScheme
	Row(
		modifier = modifier
			.fillMaxWidth()
			.clip(RoundedCornerShape(12.dp))
			.background(colors.secondary)
			.border(1.dp, colors.border, RoundedCornerShape(12.dp))
			.clickableSingle(onClick = onClick)
			.padding(16.dp),
		verticalAlignment = Alignment.CenterVertically,
		horizontalArrangement = Arrangement.spacedBy(12.dp),
	) {
		Box(
			modifier = Modifier
				.clip(RoundedCornerShape(10.dp))
				.background(colors.muted)
				.padding(10.dp),
		) {
			Icon(
				painter = painterResource(icon),
				contentDescription = null,
				tint = colors.primary,
				modifier = Modifier.size(20.dp),
			)
		}
		Column(
			modifier = Modifier.weight(1f),
			verticalArrangement = Arrangement.spacedBy(2.dp),
		) {
			Row(
				verticalAlignment = Alignment.CenterVertically,
				horizontalArrangement = Arrangement.spacedBy(6.dp),
			) {
				Text(
					text = title,
					style = HandyTheme.typography.bold16,
					color = colors.textPrimary,
				)
				if (badge != null) {
					Text(
						text = badge,
						style = HandyTheme.typography.bold10,
						color = colors.onPrimary,
						modifier = Modifier
							.background(colors.primary, RoundedCornerShape(4.dp))
							.padding(horizontal = 6.dp, vertical = 2.dp),
					)
				}
			}
			Text(
				text = description,
				style = HandyTheme.typography.regular12,
				color = colors.textSecondary,
			)
		}
		Icon(
			painter = painterResource(Res.drawable.chevron_right),
			contentDescription = null,
			tint = colors.textSecondary,
			modifier = Modifier.size(18.dp),
		)
	}
}

@ThemePreviews
@Composable
private fun SheetOptionCardPreview() {
	ThemePreview {
		Column(
			modifier = Modifier.padding(16.dp),
			verticalArrangement = Arrangement.spacedBy(10.dp),
		) {
			SheetOptionCard(
				icon = Res.drawable.grid_3x3,
				title = "단계별 기록",
				description = "테이블을 보며 좌석별로 액션을 기록해요",
				onClick = {},
			)
			SheetOptionCard(
				icon = Res.drawable.file_text,
				title = "텍스트 간편 기록",
				description = "전용 키보드로 한 줄씩 빠르게 기록해요",
				onClick = {},
				badge = "NEW",
			)
		}
	}
}

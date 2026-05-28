package com.hand.log.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.hand.log.designsystem.etc.ThemePreview
import com.hand.log.designsystem.etc.ThemePreviews
import com.hand.log.designsystem.theme.HandyTheme
import handylog.core.res.generated.resources.Res
import handylog.core.res.generated.resources.chevron_right
import org.jetbrains.compose.resources.painterResource

/**
 * 설정 화면에서 사용하는 카드형 컨테이너.
 *
 * - RoundedCornerShape(12.dp) + card 배경
 * - 내부 content를 자유롭게 구성
 */
@Composable
fun SettingsCard(
	modifier: Modifier = Modifier,
	content: @Composable ColumnScope.() -> Unit,
) {
	Column(
		modifier = modifier
			.fillMaxWidth()
			.clip(RoundedCornerShape(12.dp))
			.background(HandyTheme.colorScheme.card)
			.padding(16.dp),
		content = content,
	)
}

/**
 * 클릭 가능한 네비게이션 아이템 (제목 + '>' 아이콘).
 *
 * @param title 주 텍스트
 * @param subtitle 보조 텍스트 (null이면 미노출)
 * @param titleColor 제목 텍스트 색상
 * @param onClick 클릭 콜백
 */
@Composable
fun SettingsNavigationItem(
	title: String,
	onClick: () -> Unit,
	modifier: Modifier = Modifier,
	subtitle: String? = null,
	titleColor: Color = HandyTheme.colorScheme.textPrimary,
) {
	val colors = HandyTheme.colorScheme

	Row(
		modifier = modifier
			.fillMaxWidth()
			.clip(RoundedCornerShape(12.dp))
			.background(colors.card)
			.clickable(onClick = onClick)
			.padding(16.dp),
		verticalAlignment = Alignment.CenterVertically,
		horizontalArrangement = Arrangement.SpaceBetween,
	) {
		Column(modifier = Modifier.weight(1f)) {
			Text(
				text = title,
				style = HandyTheme.typography.medium14,
				color = titleColor,
			)
			if (subtitle != null) {
				Text(
					text = subtitle,
					style = HandyTheme.typography.regular12,
					color = colors.textSecondary,
				)
			}
		}
		Icon(
			painter = painterResource(Res.drawable.chevron_right),
			contentDescription = null,
			tint = colors.textSecondary,
			modifier = Modifier.size(16.dp),
		)
	}
}

@ThemePreviews
@Composable
private fun SettingsNavigationItemPreview() {
	ThemePreview {
		Column(
			modifier = Modifier.padding(16.dp),
			verticalArrangement = Arrangement.spacedBy(8.dp),
		) {
			SettingsNavigationItem(
				title = "베팅 프리셋",
				onClick = {},
			)
			SettingsNavigationItem(
				title = "Free",
				subtitle = "Pro 버전으로 업그레이드",
				onClick = {},
			)
		}
	}
}

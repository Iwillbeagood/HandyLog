package com.hand.log.handdetail.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
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
import com.hand.log.designsystem.component.VerticalSpacer
import com.hand.log.designsystem.etc.ThemePreview
import com.hand.log.designsystem.etc.ThemePreviews
import com.hand.log.designsystem.theme.HandyTheme
import handylog.core.res.generated.resources.Res
import handylog.core.res.generated.resources.chevron_right
import handylog.core.res.generated.resources.showdown_memo
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun MemoSection(
	memo: String,
	onClick: () -> Unit,
	modifier: Modifier = Modifier,
) {
	val colors = HandyTheme.colorScheme
	val typography = HandyTheme.typography

	Column(
		modifier = modifier
			.fillMaxWidth()
			.clip(RoundedCornerShape(12.dp))
			.background(colors.card)
			.clickable(onClick = onClick)
			.padding(vertical = 16.dp, horizontal = 12.dp),
	) {
		Row(
			verticalAlignment = Alignment.CenterVertically,
		) {
			Text(
				text = stringResource(Res.string.showdown_memo),
				style = typography.bold16,
				color = colors.textPrimary,
			)
			Spacer(modifier = Modifier.weight(1f))
			Icon(
				painter = painterResource(Res.drawable.chevron_right),
				contentDescription = null,
				tint = colors.textSecondary,
				modifier = Modifier.size(20.dp),
			)
		}
		VerticalSpacer(8.dp)
		Text(
			text = memo.ifEmpty { "-" },
			style = typography.regular14,
			color = if (memo.isEmpty()) colors.textSecondary else colors.textPrimary,
			modifier = Modifier.defaultMinSize(minHeight = 60.dp),
		)
	}
}

@ThemePreviews
@Composable
private fun MemoSectionPreview() {
	ThemePreview {
		MemoSection(
			memo = "UTG의 오픈 레이즈에 대해 3벳을 했으나 4벳을 받고 폴드. 상대 레인지가 너무 타이트해서 블러프 3벳은 손해라고 판단.",
			onClick = {},
		)
	}
}

@ThemePreviews
@Composable
private fun MemoSectionEmptyPreview() {
	ThemePreview {
		MemoSection(
			memo = "",
			onClick = {},
		)
	}
}

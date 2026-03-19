package com.hand.log.players.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hand.log.designsystem.etc.ThemePreview
import com.hand.log.designsystem.etc.ThemePreviews
import com.hand.log.designsystem.theme.HandyTheme
import handylog.core.res.generated.resources.Res
import handylog.core.res.generated.resources.users
import org.jetbrains.compose.resources.painterResource

@Composable
internal fun PlayersEmptyState(modifier: Modifier = Modifier) {
	val colors = HandyTheme.colorScheme

	Column(
		modifier = modifier,
		horizontalAlignment = Alignment.CenterHorizontally,
		verticalArrangement = Arrangement.Center,
	) {
		Icon(
			painter = painterResource(Res.drawable.users),
			contentDescription = null,
			tint = colors.primary,
			modifier = Modifier.size(32.dp),
		)

		Spacer(modifier = Modifier.height(16.dp))

		Text(
			text = "마킹된 플레이어가 없습니다",
			style = HandyTheme.typography.bold16,
			color = colors.textPrimary,
		)

		Spacer(modifier = Modifier.height(4.dp))

		Text(
			text = "자주 만나는 플레이어를 마킹해보세요",
			style = HandyTheme.typography.regular14,
			color = colors.textSecondary,
		)
	}
}

@ThemePreviews
@Composable
private fun PlayersEmptyStatePreview() {
	ThemePreview {
		PlayersEmptyState(
			modifier = Modifier.size(300.dp, 400.dp),
		)
	}
}

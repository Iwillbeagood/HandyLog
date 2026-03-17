package com.hand.log.players

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hand.log.designsystem.component.BaseScaffold
import com.hand.log.designsystem.component.HandyTopAppbar
import com.hand.log.designsystem.theme.HandyTheme

@Composable
internal fun PlayersScreen() {
	val colors = HandyTheme.colorScheme
	val typography = HandyTheme.typography

	BaseScaffold(
		topBar = {
			HandyTopAppbar(title = "플레이어")
		},
	) {
		Box(
			modifier = Modifier
				.fillMaxSize()
				.padding(16.dp),
			contentAlignment = Alignment.Center,
		) {
			Text(
				text = "자주 만나는 플레이어를 저장하세요",
				style = typography.medium14,
				color = colors.textSecondary,
			)
		}
	}
}

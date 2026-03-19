package com.hand.log.main.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideIn
import androidx.compose.animation.slideOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.hand.log.designsystem.component.HandyHorizontalDivider
import com.hand.log.designsystem.theme.HandyTheme
import com.hand.log.navigation.navigation.MainTabRoute
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.toPersistentList
import org.jetbrains.compose.resources.painterResource
import com.hand.log.designsystem.etc.ThemePreview
import com.hand.log.designsystem.etc.ThemePreviews

@Composable
internal fun MainBottomBar(
	visible: Boolean,
	bottomItems: PersistentList<MainBottomNavItem>,
	currentItem: MainBottomNavItem?,
	onItemClick: (MainTabRoute) -> Unit = {},
) {
	val colors = HandyTheme.colorScheme

	AnimatedVisibility(
		visible = visible,
		enter = fadeIn() + slideIn { IntOffset(0, it.height) },
		exit = fadeOut() + slideOut { IntOffset(0, it.height) },
	) {
		Box(
			modifier = Modifier
				.fillMaxWidth()
				.background(colors.card)
				.navigationBarsPadding(),
		) {
			Column(
				modifier = Modifier
					.fillMaxWidth(),
			) {
				HandyHorizontalDivider()
				Row(
					modifier = Modifier
						.fillMaxWidth(),
					horizontalArrangement = Arrangement.SpaceEvenly,
					verticalAlignment = Alignment.CenterVertically,
				) {
					bottomItems.forEach { item ->
						val isSelected = item == currentItem
						Box(
							modifier = Modifier
								.weight(1f)
								.clickable { onItemClick(item.route) }
								.padding(vertical = 10.dp),
							contentAlignment = Alignment.Center,
						) {
							Column(
								horizontalAlignment = Alignment.CenterHorizontally,
								verticalArrangement = Arrangement.spacedBy(2.dp),
							) {
								Icon(
									painter = painterResource(item.icon),
									contentDescription = null,
									modifier = Modifier.size(22.dp),
									tint = if (isSelected) colors.primary else colors.textSecondary,
								)
								Text(
									text = when (item) {
										MainBottomNavItem.Home -> "홈"
										MainBottomNavItem.Players -> "마킹"
										MainBottomNavItem.Settings -> "설정"
									},
									style = HandyTheme.typography.medium12,
									color = if (isSelected) colors.primary else colors.textSecondary,
								)
							}
						}
					}
				}
			}
		}
	}
}

@ThemePreviews
@Composable
private fun MainBottomBarPreview() {
	ThemePreview {
		MainBottomBar(
			visible = true,
			bottomItems = MainBottomNavItem.entries.toPersistentList(),
			currentItem = MainBottomNavItem.Home,
			onItemClick = {},
		)
	}
}

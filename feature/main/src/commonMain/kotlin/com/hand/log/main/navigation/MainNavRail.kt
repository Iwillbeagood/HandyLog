package com.hand.log.main.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.hand.log.designsystem.component.FadeAnimatedVisibility
import com.hand.log.designsystem.component.HandyVerticalDivider
import com.hand.log.designsystem.theme.HandyTheme
import com.hand.log.navigation.navigation.MainTabRoute
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.toPersistentList
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import com.hand.log.designsystem.etc.ThemePreview
import com.hand.log.designsystem.etc.ThemePreviews

private val RailWidth = 84.dp

@Composable
internal fun MainNavRail(
	visible: Boolean,
	railItems: PersistentList<MainBottomNavItem>,
	currentItem: MainBottomNavItem?,
	onItemClick: (MainTabRoute) -> Unit = {},
) {
	val colors = HandyTheme.colorScheme

	FadeAnimatedVisibility(visible = visible) {
		Row(modifier = Modifier.fillMaxHeight()) {
			Column(
				modifier = Modifier
					.fillMaxHeight()
					.background(colors.card)
					.statusBarsPadding()
					.navigationBarsPadding()
					.width(RailWidth)
					.padding(vertical = 16.dp),
				verticalArrangement = Arrangement.spacedBy(8.dp),
				horizontalAlignment = Alignment.CenterHorizontally,
			) {
				railItems.forEach { item ->
					val isSelected = item == currentItem
					Box(
						modifier = Modifier
							.clickable { onItemClick(item.route) }
							.padding(vertical = 8.dp, horizontal = 4.dp),
						contentAlignment = Alignment.Center,
					) {
						Column(
							horizontalAlignment = Alignment.CenterHorizontally,
							verticalArrangement = Arrangement.spacedBy(4.dp),
						) {
							Icon(
								painter = painterResource(item.icon),
								contentDescription = null,
								modifier = Modifier.size(24.dp),
								tint = if (isSelected) colors.primary else colors.textSecondary,
							)
							Text(
								text = stringResource(item.labelRes),
								style = HandyTheme.typography.medium12,
								color = if (isSelected) colors.primary else colors.textSecondary,
								textAlign = TextAlign.Center,
							)
						}
					}
				}
			}
			HandyVerticalDivider()
		}
	}
}

@ThemePreviews
@Composable
private fun MainNavRailPreview() {
	ThemePreview {
		MainNavRail(
			visible = true,
			railItems = MainBottomNavItem.entries.toPersistentList(),
			currentItem = MainBottomNavItem.Home,
			onItemClick = {},
		)
	}
}

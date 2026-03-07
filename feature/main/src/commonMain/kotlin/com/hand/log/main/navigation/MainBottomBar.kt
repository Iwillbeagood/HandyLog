package com.hand.log.main.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideIn
import androidx.compose.animation.slideOut
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.hand.log.designsystem.theme.HandLogTheme
import com.hand.log.main.navigation.MainBottomNavItem
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.toPersistentList
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
internal fun MainBottomBar(
	visible: Boolean,
	bottomItems: PersistentList<MainBottomNavItem>,
	currentItem: MainBottomNavItem?,
	onItemClick: (MainBottomNavItem) -> Unit = {},
) {
	AnimatedVisibility(
		visible = visible,
		enter = fadeIn() + slideIn { IntOffset(0, it.height) },
		exit = fadeOut() + slideOut { IntOffset(0, it.height) },
	) {
		NavigationBar(
			containerColor = MaterialTheme.colorScheme.surface,
			modifier = Modifier
				.clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
				.border(
					1.dp,
					MaterialTheme.colorScheme.outline,
					RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
				)
				.shadow(1.dp, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
				.height(60.dp),
		) {
			bottomItems.forEach { item ->

				NavigationBarItem(
					icon = {
						Icon(
							imageVector = item.icon,
							contentDescription = null,
							modifier = Modifier.size(22.dp),
						)
					},
					colors = NavigationBarItemDefaults.colors(
						selectedIconColor = MaterialTheme.colorScheme.onSurface,
						selectedTextColor = MaterialTheme.colorScheme.onSurface,
						indicatorColor = Color.Transparent,
						unselectedIconColor = MaterialTheme.colorScheme.onSecondary,
						unselectedTextColor = MaterialTheme.colorScheme.onSecondary,
					),
					onClick = { onItemClick(item) },
					selected = item == currentItem,
				)
			}
		}
	}
}

@Preview
@Composable
private fun MainBottomBarPreview() {
	HandLogTheme {
		MainBottomBar(
			visible = true,
			bottomItems = MainBottomNavItem.entries.toPersistentList(),
			currentItem = MainBottomNavItem.Home,
			onItemClick = {},
		)
	}
}

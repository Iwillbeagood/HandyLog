package com.hand.log.main.navigation

import androidx.compose.runtime.Composable
import com.hand.log.navigation.navigation.MainTabRoute
import kotlinx.collections.immutable.PersistentList

@Composable
internal expect fun PlatformBottomBar(
	visible: Boolean,
	bottomItems: PersistentList<MainBottomNavItem>,
	currentItem: MainBottomNavItem?,
	onItemClick: (MainTabRoute) -> Unit,
)

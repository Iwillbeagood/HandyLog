package com.hand.log.main.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import com.hand.log.navigation.navigation.MainTabRoute
import com.hand.log.navigation.navigation.Route

enum class MainBottomNavItem(
	val titleRes: Int,
	val icon: ImageVector,
	val route: MainTabRoute,
) {
	Home(
		titleRes = 0,
		icon = Icons.Default.Home,
		route = MainTabRoute.Home,
	),
	;

	companion object {
		fun find(predicate: (MainTabRoute) -> Boolean): MainBottomNavItem? {
			return entries.find { predicate(it.route) }
		}

		@Composable
		fun contains(predicate: @Composable (Route) -> Boolean): Boolean {
			return entries.map { it.route }.any { predicate(it) }
		}
	}
}

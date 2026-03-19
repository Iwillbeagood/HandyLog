package com.hand.log.main.navigation

import androidx.compose.runtime.Composable
import com.hand.log.navigation.navigation.MainTabRoute
import com.hand.log.navigation.navigation.Route
import handylog.core.res.generated.resources.Res
import handylog.core.res.generated.resources.house
import handylog.core.res.generated.resources.settings
import handylog.core.res.generated.resources.users
import org.jetbrains.compose.resources.DrawableResource

enum class MainBottomNavItem(
	val icon: DrawableResource,
	val route: MainTabRoute,
) {
	Home(
		icon = Res.drawable.house,
		route = MainTabRoute.Home,
	),
	Players(
		icon = Res.drawable.users,
		route = MainTabRoute.Players,
	),
	Settings(
		icon = Res.drawable.settings,
		route = MainTabRoute.Settings,
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

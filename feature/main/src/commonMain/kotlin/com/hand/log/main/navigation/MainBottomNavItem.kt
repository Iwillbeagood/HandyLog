package com.hand.log.main.navigation

import androidx.compose.runtime.Composable
import com.hand.log.navigation.navigation.MainTabRoute
import com.hand.log.navigation.navigation.Route
import handylog.core.res.generated.resources.Res
import handylog.core.res.generated.resources.dollar_sign
import org.jetbrains.compose.resources.DrawableResource

enum class MainBottomNavItem(
	val icon: DrawableResource,
	val route: MainTabRoute,
) {
	Home(
		icon = Res.drawable.dollar_sign,
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

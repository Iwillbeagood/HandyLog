package com.hand.log.main.navigation

import androidx.compose.runtime.Composable
import com.hand.log.navigation.navigation.MainTabRoute
import com.hand.log.navigation.navigation.Route
import handylog.core.res.generated.resources.Res
import handylog.core.res.generated.resources.grid_3x3
import handylog.core.res.generated.resources.house
import handylog.core.res.generated.resources.nav_chart
import handylog.core.res.generated.resources.nav_home
import handylog.core.res.generated.resources.nav_marking
import handylog.core.res.generated.resources.nav_settings
import handylog.core.res.generated.resources.settings
import handylog.core.res.generated.resources.users
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource

enum class MainBottomNavItem(
	val icon: DrawableResource,
	val labelRes: StringResource,
	val route: MainTabRoute,
) {
	Home(
		icon = Res.drawable.house,
		labelRes = Res.string.nav_home,
		route = MainTabRoute.Home,
	),
	Players(
		icon = Res.drawable.users,
		labelRes = Res.string.nav_marking,
		route = MainTabRoute.Players(),
	),
	Chart(
		icon = Res.drawable.grid_3x3,
		labelRes = Res.string.nav_chart,
		route = MainTabRoute.Preflop,
	),
	Settings(
		icon = Res.drawable.settings,
		labelRes = Res.string.nav_settings,
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

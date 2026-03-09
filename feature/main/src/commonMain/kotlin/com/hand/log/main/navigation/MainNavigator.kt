package com.hand.log.main.navigation

import com.hand.log.navigation.navigation.LaunchMode
import com.hand.log.navigation.navigation.MainTabRoute
import com.hand.log.navigation.navigation.Route
import com.hand.log.navigation.navigation.RouteStack
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

internal class MainNavigator {

	private val _routeStack = MutableStateFlow(RouteStack(MainTabRoute.Home))
	val routeStack: StateFlow<RouteStack> = _routeStack.asStateFlow()

	fun navigate(
		route: Route,
		launchMode: LaunchMode = LaunchMode.STANDARD,
	) {
		_routeStack.update { current ->
			when (launchMode) {
				LaunchMode.CLEAR_ALL -> RouteStack(backStack = listOf(route))
				LaunchMode.SINGLE_TOP -> {
					if (route == current.current) {
						current
					} else {
						current.copy(backStack = current.backStack + route)
					}
				}
				LaunchMode.CLEAR_TOP -> {
					if (route in current.backStack) {
						current.copy(
							backStack = current.backStack.takeWhile { it != route } + route,
						)
					} else {
						current.copy(backStack = current.backStack + route)
					}
				}
				LaunchMode.STANDARD -> current.copy(backStack = current.backStack + route)
			}
		}
	}

	fun goBack() {
		_routeStack.update { current ->
			if (current.backStack.size <= 1) {
				current
			} else {
				current.copy(
					backStack = current.backStack.dropLast(1),
				)
			}
		}
	}

	fun navigateTab(tab: MainBottomNavItem) {
		when (tab) {
			MainBottomNavItem.Home -> {
				navigate(MainTabRoute.Home, LaunchMode.CLEAR_ALL)
			}
		}
	}
}

package com.hand.log.main.navigation

import com.hand.log.navigation.interop.NavigateActionInterop
import com.hand.log.navigation.navigation.LaunchMode
import com.hand.log.navigation.navigation.MainTabRoute
import com.hand.log.navigation.navigation.BetSizeSettings
import com.hand.log.navigation.navigation.HandDetail
import com.hand.log.navigation.navigation.PlayerHands
import com.hand.log.navigation.navigation.RecordHand
import com.hand.log.navigation.navigation.Route
import com.hand.log.navigation.navigation.RouteStack
import com.hand.log.navigation.navigation.Table
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

internal class MainNavigator {

	private var openAddNonce = 0L
	private val _routeStack = MutableStateFlow(RouteStack(MainTabRoute.Home))
	val routeStack: StateFlow<RouteStack> = _routeStack.asStateFlow()

	val navigateActionInterop = object : NavigateActionInterop {
		override fun popBackStack() {
			goBack()
		}

		override fun navigateBottomNav(item: MainTabRoute) {
			navigateTab(item)
		}

		override fun navigateToTableDetail(tableId: String) {
			navigate(Table(tableId))
		}

		override fun navigateToRecordHand(tableId: String) {
			navigate(RecordHand(tableId))
		}

		override fun navigateToHandDetail(handId: String) {
			navigate(HandDetail(handId))
		}

		override fun navigateToPlayersWithAdd() {
			navigateTab(MainTabRoute.Players(openAdd = true, nonce = ++openAddNonce))
		}

		override fun navigateToPlayerHands(savedPlayerId: String, playerName: String) {
			navigate(PlayerHands(savedPlayerId, playerName))
		}

		override fun navigateToBetSizeSettings() {
			navigate(BetSizeSettings)
		}
	}

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

	fun navigateTab(tab: MainTabRoute) {
		navigate(tab, LaunchMode.CLEAR_ALL)
	}
}

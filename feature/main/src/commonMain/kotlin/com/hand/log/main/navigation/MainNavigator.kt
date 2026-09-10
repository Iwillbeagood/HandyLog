package com.hand.log.main.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hand.log.navigation.interop.NavigateActionInterop
import com.hand.log.navigation.navigation.LaunchMode
import com.hand.log.navigation.navigation.MainTabRoute
import com.hand.log.navigation.navigation.BetSizeSettings
import com.hand.log.navigation.navigation.Contact
import com.hand.log.navigation.navigation.Legal
import com.hand.log.navigation.navigation.HandDetail
import com.hand.log.navigation.navigation.ProUpgrade
import com.hand.log.navigation.navigation.PlayerHands
import com.hand.log.navigation.navigation.PreflopChart
import com.hand.log.navigation.navigation.PreflopChartTable
import com.hand.log.navigation.navigation.PreflopQuiz
import com.hand.log.navigation.navigation.PreflopQuizSession
import com.hand.log.navigation.navigation.RecordHand
import com.hand.log.navigation.navigation.Route
import com.hand.log.navigation.navigation.RouteStack
import com.hand.log.navigation.navigation.Table
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

internal class MainNavigator : ViewModel() {

	private var openAddNonce = 0L
	private val _routeStack = MutableStateFlow(RouteStack(MainTabRoute.Home))

	val uiState: StateFlow<MainUiState> = _routeStack
		.map { it.toUiState() }
		.stateIn(
			scope = viewModelScope,
			started = SharingStarted.WhileSubscribed(5_000),
			initialValue = _routeStack.value.toUiState(),
		)

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

		override fun navigateToPreflopChart(
			stack: String?,
			scenario: String?,
			hero: String?,
			villain: String?,
		) {
			navigate(PreflopChart(stack, scenario, hero, villain))
		}

		override fun navigateToPreflopChartTable(stack: String?, hero: String?) {
			navigate(PreflopChartTable(stack, hero))
		}

		override fun navigateToPreflopQuiz() {
			navigate(PreflopQuiz)
		}

		override fun navigateToPreflopQuizSession(type: String, stack: String, recordId: String) {
			navigate(PreflopQuizSession(type, stack, recordId))
		}

		override fun navigateToBetSizeSettings() {
			navigate(BetSizeSettings)
		}

		override fun navigateToProUpgrade() {
			navigate(ProUpgrade)
		}

		override fun navigateToContact() {
			navigate(Contact)
		}

		override fun navigateToLegal() {
			navigate(Legal)
		}
	}

	private fun RouteStack.toUiState(): MainUiState {
		val current = current
		val currentBottomItem = MainBottomNavItem.entries.find { it.route == current }
			?: when (current) {
				is MainTabRoute.Players -> MainBottomNavItem.Players
				is PlayerHands -> MainBottomNavItem.Players
				else -> null
			}
		return MainUiState(
			backStack = backStack,
			showBottomBar = current is MainTabRoute || current is PlayerHands,
			currentBottomItem = currentBottomItem,
		)
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

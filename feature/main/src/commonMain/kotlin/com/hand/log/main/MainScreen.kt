package com.hand.log.main

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hand.log.main.navigation.MainBottomBar
import com.hand.log.main.navigation.MainBottomNavItem
import com.hand.log.main.navigation.MainNavDisplay
import com.hand.log.main.navigation.MainNavigator
import com.hand.log.navigation.interop.LocalNavigateActionInterop
import com.hand.log.navigation.navigation.MainTabRoute
import com.hand.log.navigation.navigation.PlayerHands
import com.hand.log.navigation.navigation.RouteStack
import kotlinx.collections.immutable.toPersistentList

@Composable
fun MainScreen() {
	val navigator = remember { MainNavigator() }
	val routeStack by navigator.routeStack.collectAsStateWithLifecycle()

	CompositionLocalProvider(
		LocalNavigateActionInterop provides navigator.navigateActionInterop,
	) {
		MainScreenContent(
			routeStack = routeStack,
			onTabSelected = navigator::navigateTab,
			onBack = navigator::goBack,
		)
	}
}

@Composable
private fun MainScreenContent(
	routeStack: RouteStack,
	onTabSelected: (MainTabRoute) -> Unit,
	onBack: () -> Unit,
) {
	Scaffold(
		contentWindowInsets = WindowInsets(),
		bottomBar = {
			val showBottomBar = routeStack.current is MainTabRoute ||
				routeStack.current is PlayerHands
			val currentItem = MainBottomNavItem.entries.find { it.route == routeStack.current }
				?: when (routeStack.current) {
					is MainTabRoute.Players -> MainBottomNavItem.Players
					is PlayerHands -> MainBottomNavItem.Players
					else -> null
				}
			MainBottomBar(
				visible = showBottomBar,
				bottomItems = MainBottomNavItem.entries.toPersistentList(),
				currentItem = currentItem,
				onItemClick = onTabSelected,
			)
		},
		content = { paddingValues ->
			MainNavDisplay(
				paddingValues = paddingValues,
				backStack = routeStack.backStack,
				onBack = onBack,
			)
		},
	)
}

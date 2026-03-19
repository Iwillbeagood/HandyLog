package com.hand.log.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
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
			MainBottomBar(
				visible = routeStack.current in MainBottomNavItem.entries.map { it.route },
				bottomItems = MainBottomNavItem.entries.toPersistentList(),
				currentItem = MainBottomNavItem.entries.find { it.route == routeStack.current },
				onItemClick = onTabSelected,
			)
		},
		content = { paddingValues ->
			Box(modifier = Modifier.padding(paddingValues)) {
				MainNavDisplay(
					backStack = routeStack.backStack,
					onBack = onBack,
				)
			}
		},
	)
}

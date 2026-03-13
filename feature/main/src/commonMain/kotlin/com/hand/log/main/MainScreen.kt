package com.hand.log.main

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hand.log.main.navigation.MainBottomBar
import com.hand.log.main.navigation.MainBottomNavItem
import com.hand.log.main.navigation.MainNavHost
import com.hand.log.main.navigation.MainNavigator
import com.hand.log.navigation.interop.LocalNavigateActionInterop
import com.hand.log.navigation.interop.NavigateActionInterop
import com.hand.log.navigation.navigation.MainTabRoute
import com.hand.log.navigation.navigation.RecordHand
import com.hand.log.navigation.navigation.RouteStack
import com.hand.log.navigation.navigation.TableDetail
import kotlinx.collections.immutable.toPersistentList

@Composable
fun MainScreen() {
	val navigator = remember { MainNavigator() }
	val routeStack by navigator.routeStack.collectAsStateWithLifecycle()

	val navigateActionInterop = object : NavigateActionInterop {
		override fun popBackStack() {
			navigator.goBack()
		}

		override fun navigateBottomNav(item: MainTabRoute) {
			navigator.navigateTab(item)
		}

		override fun navigateToTableDetail(tableId: String) {
			navigator.navigate(TableDetail(tableId))
		}

		override fun navigateToRecordHand(tableId: String) {
			navigator.navigate(RecordHand(tableId))
		}
	}

	CompositionLocalProvider(
		LocalNavigateActionInterop provides navigateActionInterop,
	) {
		MainScreenContent(
			routeStack = routeStack,
			onTabSelected = navigator::navigateTab,
		)
	}
}

@Composable
private fun MainScreenContent(
	routeStack: RouteStack,
	onTabSelected: (MainTabRoute) -> Unit,
) {
	Scaffold(
		modifier = Modifier
			.fillMaxSize()
			.statusBarsPadding()
			.navigationBarsPadding(),
		bottomBar = {
			MainBottomBar(
				visible = routeStack.current in MainBottomNavItem.entries.map { it.route },
				bottomItems = MainBottomNavItem.entries.toPersistentList(),
				currentItem = MainBottomNavItem.entries.find { it.route == routeStack.current },
				onItemClick = onTabSelected,
			)
		},
		content = {
			MainNavHost(
				backStack = routeStack.backStack,
				paddingValues = it,
			)
		},
	)
}

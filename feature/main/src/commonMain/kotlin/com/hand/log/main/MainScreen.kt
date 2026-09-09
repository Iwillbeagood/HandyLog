package com.hand.log.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hand.log.designsystem.window.LocalWindowSize
import com.hand.log.designsystem.window.ProvideWindowSize
import com.hand.log.main.navigation.MainBottomBar
import com.hand.log.main.navigation.MainBottomNavItem
import com.hand.log.main.navigation.MainNavDisplay
import com.hand.log.main.navigation.MainNavRail
import com.hand.log.main.navigation.MainNavigator
import com.hand.log.main.navigation.MainUiState
import com.hand.log.navigation.interop.LocalNavigateActionInterop
import com.hand.log.navigation.navigation.MainTabRoute
import kotlinx.collections.immutable.toPersistentList
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun MainScreen() {
	val navigator: MainNavigator = koinViewModel()
	val uiState by navigator.uiState.collectAsStateWithLifecycle()

	CompositionLocalProvider(
		LocalNavigateActionInterop provides navigator.navigateActionInterop,
	) {
		ProvideWindowSize {
			MainScreenContent(
				uiState = uiState,
				onTabSelected = navigator::navigateTab,
				onBack = navigator::goBack,
			)
		}
	}
}

@Composable
private fun MainScreenContent(
	uiState: MainUiState,
	onTabSelected: (MainTabRoute) -> Unit,
	onBack: () -> Unit,
) {
	if (LocalWindowSize.current.isLarge) {
		Row(modifier = Modifier.fillMaxSize()) {
			MainNavRail(
				visible = uiState.showBottomBar,
				railItems = MainBottomNavItem.entries.toPersistentList(),
				currentItem = uiState.currentBottomItem,
				onItemClick = onTabSelected,
			)
			Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
				MainNavDisplay(
					paddingValues = WindowInsets.navigationBars.asPaddingValues(),
					backStack = uiState.backStack,
					onBack = onBack,
				)
			}
		}
	} else {
		Scaffold(
			contentWindowInsets = WindowInsets(),
			bottomBar = {
				MainBottomBar(
					visible = uiState.showBottomBar,
					bottomItems = MainBottomNavItem.entries.toPersistentList(),
					currentItem = uiState.currentBottomItem,
					onItemClick = onTabSelected,
				)
			},
			content = { paddingValues ->
				MainNavDisplay(
					paddingValues = paddingValues,
					backStack = uiState.backStack,
					onBack = onBack,
				)
			},
		)
	}
}

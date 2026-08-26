package com.hand.log.main

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hand.log.main.navigation.MainBottomBar
import com.hand.log.main.navigation.MainBottomNavItem
import com.hand.log.main.navigation.MainNavDisplay
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
		MainScreenContent(
			uiState = uiState,
			onTabSelected = navigator::navigateTab,
			onBack = navigator::goBack,
		)
	}
}

@Composable
private fun MainScreenContent(
	uiState: MainUiState,
	onTabSelected: (MainTabRoute) -> Unit,
	onBack: () -> Unit,
) {
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

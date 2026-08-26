package com.hand.log.main.navigation

import androidx.compose.runtime.Immutable
import com.hand.log.navigation.navigation.Route

@Immutable
internal data class MainUiState(
	val backStack: List<Route>,
	val showBottomBar: Boolean,
	val currentBottomItem: MainBottomNavItem?,
)

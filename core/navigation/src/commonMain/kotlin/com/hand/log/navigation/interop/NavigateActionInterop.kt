package com.hand.log.navigation.interop

import androidx.compose.runtime.staticCompositionLocalOf
import com.hand.log.navigation.navigation.MainTabRoute

val LocalNavigateActionInterop = staticCompositionLocalOf<NavigateActionInterop> {
	error("No NavigateActionInterop provided")
}

interface NavigateActionInterop {
	fun popBackStack()
	fun navigateBottomNav(item: MainTabRoute)
	fun navigateToTableDetail(tableId: String)
	fun navigateToRecordHand(tableId: String)
	fun navigateToHandDetail(handId: String)
	fun navigateToPlayerHands(savedPlayerId: String, playerName: String)
	fun navigateToPlayersWithAdd()
	fun navigateToBetSizeSettings()
	fun navigateToProUpgrade()
}

package com.hand.log.navigation.interop

import androidx.compose.runtime.staticCompositionLocalOf
import com.hand.log.navigation.navigation.MainBottomNavItem

val LocalNavigateActionInterop = staticCompositionLocalOf<NavigateActionInterop> {
	error("No NavigateActionInterop provided")
}

interface NavigateActionInterop {
	fun popBackStack()
	fun navigateBottomNav(item: MainBottomNavItem)
//    fun navigateToIncomeList()
}

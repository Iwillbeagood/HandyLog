package com.hand.log.settings.upgrade.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.hand.log.navigation.navigation.ProUpgrade
import com.hand.log.settings.upgrade.ProUpgradeRoute

fun EntryProviderScope<NavKey>.proUpgradeNavGraph() {
	entry<ProUpgrade> {
		ProUpgradeRoute()
	}
}

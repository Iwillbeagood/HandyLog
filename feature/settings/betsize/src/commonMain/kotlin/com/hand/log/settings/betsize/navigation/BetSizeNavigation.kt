package com.hand.log.settings.betsize.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.hand.log.navigation.navigation.BetSizeSettings
import com.hand.log.settings.betsize.BetSizeSettingsRoute

fun EntryProviderScope<NavKey>.betSizeNavGraph() {
	entry<BetSizeSettings> { _ ->
		BetSizeSettingsRoute()
	}
}

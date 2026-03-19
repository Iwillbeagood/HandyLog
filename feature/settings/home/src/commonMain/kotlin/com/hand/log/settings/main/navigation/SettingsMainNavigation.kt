package com.hand.log.settings.main.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.hand.log.navigation.navigation.MainTabRoute
import com.hand.log.navigation.navigation.tabTransitionMetadata
import com.hand.log.settings.main.SettingsRoute

fun EntryProviderScope<NavKey>.settingsMainNavGraph() {
	entry<MainTabRoute.Settings>(
		metadata = tabTransitionMetadata,
	) { _ ->
		SettingsRoute()
	}
}

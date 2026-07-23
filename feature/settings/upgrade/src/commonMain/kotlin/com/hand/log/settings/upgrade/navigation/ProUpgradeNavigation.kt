package com.hand.log.settings.upgrade.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.hand.log.navigation.navigation.ProUpgrade
import com.hand.log.settings.upgrade.ProUpgradeRoute
import com.hand.log.settings.upgrade.ProUpgradeViewModel
import org.koin.compose.viewmodel.koinViewModel

fun EntryProviderScope<NavKey>.proUpgradeNavGraph() {
	entry<ProUpgrade> {
		val viewModel: ProUpgradeViewModel = koinViewModel()
		ProUpgradeRoute(viewModel = viewModel)
	}
}

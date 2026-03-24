package com.hand.log.settings.betsize.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.hand.log.navigation.navigation.BetSizeSettings
import com.hand.log.settings.betsize.BetSizeSettingsRoute
import com.hand.log.settings.betsize.BetSizeViewModel
import org.koin.compose.viewmodel.koinViewModel

fun EntryProviderScope<NavKey>.betSizeNavGraph() {
	entry<BetSizeSettings> { _ ->
		val viewModel: BetSizeViewModel = koinViewModel()

		BetSizeSettingsRoute(
			viewModel = viewModel,
		)
	}
}

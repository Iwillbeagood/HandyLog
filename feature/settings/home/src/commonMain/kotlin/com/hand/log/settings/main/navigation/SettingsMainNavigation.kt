package com.hand.log.settings.main.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.hand.log.navigation.navigation.MainTabRoute
import com.hand.log.navigation.navigation.tabTransitionMetadata
import com.hand.log.settings.main.SettingsRoute
import com.hand.log.settings.main.SettingsViewModel
import org.koin.compose.viewmodel.koinViewModel

fun EntryProviderScope<NavKey>.settingsMainNavGraph(
	paddingValues: PaddingValues,
) {

	entry<MainTabRoute.Settings>(
		metadata = tabTransitionMetadata,
	) {
		val viewModel: SettingsViewModel = koinViewModel()

		Box(
			modifier = Modifier.padding(paddingValues),
		) {
			SettingsRoute(
				viewModel = viewModel,
			)
		}
	}
}

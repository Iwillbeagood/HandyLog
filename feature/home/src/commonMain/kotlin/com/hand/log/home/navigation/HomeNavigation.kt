package com.hand.log.home.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.hand.log.home.HomeRoute
import com.hand.log.home.HomeViewModel
import com.hand.log.navigation.navigation.MainTabRoute
import com.hand.log.navigation.navigation.tabTransitionMetadata
import org.koin.compose.viewmodel.koinViewModel

fun EntryProviderScope<NavKey>.homeNavGraph() {
	entry<MainTabRoute.Home>(
		metadata = tabTransitionMetadata,
	) { _ ->
		val homeViewModel: HomeViewModel = koinViewModel()

		HomeRoute(
			viewModel = homeViewModel,
		)
	}
}

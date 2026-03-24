package com.hand.log.home.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.hand.log.home.HomeRoute
import com.hand.log.home.HomeViewModel
import com.hand.log.navigation.navigation.MainTabRoute
import com.hand.log.navigation.navigation.tabTransitionMetadata
import org.koin.compose.viewmodel.koinViewModel

fun EntryProviderScope<NavKey>.homeNavGraph(
	paddingValues: PaddingValues,
) {
	entry<MainTabRoute.Home>(
		metadata = tabTransitionMetadata,
	) {
		val homeViewModel: HomeViewModel = koinViewModel()

		Box(
			modifier = Modifier.padding(paddingValues),
		) {
			HomeRoute(
				viewModel = homeViewModel,
			)
		}

	}
}

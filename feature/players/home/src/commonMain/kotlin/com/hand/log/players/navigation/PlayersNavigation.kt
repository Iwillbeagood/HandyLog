package com.hand.log.players.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.hand.log.navigation.navigation.MainTabRoute
import com.hand.log.navigation.navigation.tabTransitionMetadata
import com.hand.log.players.PlayersRoute
import com.hand.log.players.PlayersViewModel
import org.koin.compose.viewmodel.koinViewModel

fun EntryProviderScope<NavKey>.playersNavGraph(
	paddingValues: PaddingValues,
) {
	entry<MainTabRoute.Players>(
		metadata = tabTransitionMetadata,
	) { key ->
		val viewModel: PlayersViewModel = koinViewModel()

		Box(
			modifier = Modifier.padding(paddingValues),
		) {
			PlayersRoute(
				viewModel = viewModel,
				openAdd = key.openAdd,
			)
		}
	}
}

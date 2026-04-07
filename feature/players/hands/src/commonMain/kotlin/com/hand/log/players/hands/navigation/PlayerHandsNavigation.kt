package com.hand.log.players.hands.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.hand.log.navigation.navigation.PlayerHands
import com.hand.log.players.hands.PlayerHandsRoute
import com.hand.log.players.hands.PlayerHandsViewModel
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

fun EntryProviderScope<NavKey>.playerHandsNavGraph(
	paddingValues: PaddingValues = PaddingValues(),
) {
	entry<PlayerHands> { key ->
		val viewModel: PlayerHandsViewModel = koinViewModel {
			parametersOf(key.savedPlayerId)
		}
		PlayerHandsRoute(
			playerName = key.playerName,
			viewModel = viewModel,
			paddingValues = paddingValues,
		)
	}
}

package com.hand.log.players.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.hand.log.navigation.navigation.MainTabRoute
import com.hand.log.players.PlayersScreen

fun EntryProviderScope<NavKey>.playersNavGraph() {
	entry<MainTabRoute.Players> { _ ->
		PlayersScreen()
	}
}

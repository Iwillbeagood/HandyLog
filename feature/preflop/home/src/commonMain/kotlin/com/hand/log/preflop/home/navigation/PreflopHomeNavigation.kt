package com.hand.log.preflop.home.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.hand.log.navigation.navigation.MainTabRoute
import com.hand.log.preflop.home.PreflopHomeRoute

fun EntryProviderScope<NavKey>.preflopHomeNavGraph(
	paddingValues: PaddingValues,
) {
	entry<MainTabRoute.Preflop> {
		Box(
			modifier = Modifier.padding(paddingValues),
		) {
			PreflopHomeRoute()
		}
	}
}

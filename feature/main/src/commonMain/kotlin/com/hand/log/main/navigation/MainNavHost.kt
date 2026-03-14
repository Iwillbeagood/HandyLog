package com.hand.log.main.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.hand.log.home.navigation.homeNavGraph
import com.hand.log.record.navigation.recordHandNavGraph
import com.hand.log.table.navigation.tableNavGraph

@Composable
internal fun MainNavHost(
	backStack: List<NavKey>,
) {
	val entryProvider = entryProvider {
		homeNavGraph()
		tableNavGraph()
		recordHandNavGraph()
	}

	NavDisplay(
		entryDecorators = listOf(
			rememberSaveableStateHolderNavEntryDecorator(),
			rememberViewModelStoreNavEntryDecorator(),
		),
		backStack = backStack,
		entryProvider = entryProvider,
	)
}

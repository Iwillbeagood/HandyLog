package com.hand.log.main.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.hand.log.home.navigation.homeNavGraph

@Composable
internal fun MainNavHost(
	backStack: List<NavKey>,
	paddingValues: PaddingValues,
	modifier: Modifier = Modifier,
) {
	val entryProvider = entryProvider<NavKey> {
		homeNavGraph()
	}

	Box(
		modifier = modifier
			.fillMaxSize()
			.background(MaterialTheme.colorScheme.surfaceDim)
			.padding(paddingValues),
	) {
		NavDisplay(
			entryDecorators = listOf(
				rememberSaveableStateHolderNavEntryDecorator(),
				rememberViewModelStoreNavEntryDecorator(),
			),
			backStack = backStack,
			entryProvider = entryProvider,
		)
	}
}

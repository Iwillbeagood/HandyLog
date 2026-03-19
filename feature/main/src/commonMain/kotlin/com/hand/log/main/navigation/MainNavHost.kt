package com.hand.log.main.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.hand.log.handdetail.navigation.handDetailNavGraph
import com.hand.log.home.navigation.homeNavGraph
import com.hand.log.players.navigation.playersNavGraph
import com.hand.log.record.navigation.recordHandNavGraph
import com.hand.log.table.navigation.tableNavGraph

@Composable
internal fun MainNavHost(
	backStack: List<NavKey>,
	onBack: () -> Unit,
) {
	val entryProvider = entryProvider {
		homeNavGraph()
		playersNavGraph()
		tableNavGraph()
		recordHandNavGraph()
		handDetailNavGraph()
	}

	NavDisplay(
		entryDecorators = listOf(
			rememberSaveableStateHolderNavEntryDecorator(),
			rememberViewModelStoreNavEntryDecorator(),
		),
		backStack = backStack,
		onBack = onBack,
		entryProvider = entryProvider,
		transitionSpec = {
			slideInHorizontally(
				initialOffsetX = { it },
				animationSpec = tween(350, easing = FastOutSlowInEasing),
			) togetherWith
				slideOutHorizontally(
					targetOffsetX = { (-it * 0.15f).toInt() },
					animationSpec = tween(350, easing = FastOutSlowInEasing),
				) + fadeOut(animationSpec = tween(350, easing = FastOutSlowInEasing))
		},
		popTransitionSpec = {
			EnterTransition.None togetherWith
				slideOutHorizontally(
					targetOffsetX = { it },
					animationSpec = tween(350, easing = FastOutSlowInEasing),
				)
		},
	)
}

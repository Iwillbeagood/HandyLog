package com.hand.log.main.navigation

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import com.hand.log.navigation.navigation.MainTabRoute
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.hand.log.handdetail.navigation.handDetailNavGraph
import com.hand.log.home.navigation.homeNavGraph
import com.hand.log.players.hands.navigation.playerHandsNavGraph
import com.hand.log.players.navigation.playersNavGraph
import com.hand.log.preflop.chart.navigation.preflopChartNavGraph
import com.hand.log.preflop.home.navigation.preflopHomeNavGraph
import com.hand.log.preflop.quiz.home.navigation.preflopQuizHomeNavGraph
import com.hand.log.preflop.quiz.session.navigation.preflopQuizSessionNavGraph
import com.hand.log.record.navigation.recordHandNavGraph
import com.hand.log.settings.betsize.navigation.betSizeNavGraph
import com.hand.log.settings.contact.navigation.contactNavGraph
import com.hand.log.settings.legal.navigation.legalNavGraph
import com.hand.log.settings.main.navigation.settingsMainNavGraph
import com.hand.log.settings.upgrade.navigation.proUpgradeNavGraph
import com.hand.log.table.navigation.tableNavGraph

private const val SLIDE_DURATION = 450
private const val TAB_FADE_DURATION = 250

private fun tabCrossfade(): ContentTransform =
	fadeIn(tween(TAB_FADE_DURATION, easing = FastOutSlowInEasing)) togetherWith
		fadeOut(tween(TAB_FADE_DURATION, easing = FastOutSlowInEasing))

@Composable
internal fun MainNavDisplay(
	paddingValues: PaddingValues,
	backStack: List<NavKey>,
	onBack: () -> Unit,
) {
	val entryProvider = entryProvider {
		homeNavGraph(paddingValues)
		preflopHomeNavGraph(paddingValues)
		playersNavGraph(paddingValues)
		settingsMainNavGraph(paddingValues)

		preflopChartNavGraph()
		preflopQuizHomeNavGraph()
		preflopQuizSessionNavGraph()
		tableNavGraph()
		recordHandNavGraph()
		handDetailNavGraph()
		playerHandsNavGraph(paddingValues)
		betSizeNavGraph()
		proUpgradeNavGraph()
		contactNavGraph()
		legalNavGraph()
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
			if (initialState.key is MainTabRoute && targetState.key is MainTabRoute) {
				tabCrossfade()
			} else {
				slideInHorizontally(
					initialOffsetX = { it },
					animationSpec = tween(SLIDE_DURATION, easing = FastOutSlowInEasing),
				) togetherWith
					slideOutHorizontally(
						targetOffsetX = { (-it * 0.15f).toInt() },
						animationSpec = tween(SLIDE_DURATION, easing = FastOutSlowInEasing),
					)
			}
		},
		popTransitionSpec = {
			if (initialState.key is MainTabRoute && targetState.key is MainTabRoute) {
				tabCrossfade()
			} else {
				(
					slideInHorizontally(
						initialOffsetX = { (-it * 0.15f).toInt() },
						animationSpec = tween(SLIDE_DURATION, easing = FastOutSlowInEasing),
					) togetherWith
						slideOutHorizontally(
							targetOffsetX = { it },
							animationSpec = tween(SLIDE_DURATION, easing = FastOutSlowInEasing),
						)
					).apply { targetContentZIndex = -1f }
			}
		},
	)
}

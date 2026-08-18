package com.hand.log.main.navigation

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.IntOffset
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
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

private fun slideTween() = tween<IntOffset>(SLIDE_DURATION, easing = FastOutSlowInEasing)

// 앞으로 진입: 새 화면이 오른쪽에서 들어오고 이전 화면은 살짝 왼쪽으로 밀린다.
private fun slideForward(): ContentTransform =
	slideInHorizontally(initialOffsetX = { it }, animationSpec = slideTween()) togetherWith
		slideOutHorizontally(targetOffsetX = { (-it * 0.15f).toInt() }, animationSpec = slideTween())

// 뒤로가기(버튼·제스처 공통): 현재 화면이 오른쪽으로 빠지고 이전 화면이 왼쪽에서 되돌아온다.
private fun slideBack(): ContentTransform =
	(
		slideInHorizontally(initialOffsetX = {
			(-it * 0.15f).toInt()
		}, animationSpec = slideTween()) togetherWith
			slideOutHorizontally(targetOffsetX = { it }, animationSpec = slideTween())
		).apply { targetContentZIndex = -1f }

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
		transitionSpec = { slideForward() },
		popTransitionSpec = { slideBack() },
		// 시스템 뒤로가기(예측 back) 제스처도 기본 scaleOut(중앙 축소) 대신 pop 과 동일한 slide 로 맞춘다.
		predictivePopTransitionSpec = { slideBack() },
	)
}

package com.hand.log.navigation.navigation

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.navigation3.ui.NavDisplay

private const val TAB_FADE_DURATION = 250

/**
 * 바텀 네비게이션 탭 엔트리에 부여하는 전환 metadata — 탭 간 이동은 단순 fade 로 처리한다.
 * NavDisplay 는 들어오는 엔트리(push 시 target, pop 시 사라지는 화면)의 metadata 를 우선 참조하므로,
 * 이 metadata 는 '탭으로 push' 될 때만 fade 를 적용하고 deep 화면 pop/push 는 NavDisplay 기본 slide 로 남는다.
 */
val tabTransitionMetadata: Map<String, Any> =
	NavDisplay.transitionSpec {
		fadeIn(
			animationSpec = tween(TAB_FADE_DURATION, easing = FastOutSlowInEasing),
		) togetherWith fadeOut(
			animationSpec = tween(TAB_FADE_DURATION, easing = FastOutSlowInEasing),
		)
	} + NavDisplay.popTransitionSpec {
		fadeIn(
			animationSpec = tween(TAB_FADE_DURATION, easing = FastOutSlowInEasing),
		) togetherWith fadeOut(
			animationSpec = tween(TAB_FADE_DURATION, easing = FastOutSlowInEasing),
		)
	}

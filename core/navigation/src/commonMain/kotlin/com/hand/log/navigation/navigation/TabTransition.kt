package com.hand.log.navigation.navigation

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.navigation3.ui.NavDisplay

private const val TAB_FADE_DURATION = 250

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

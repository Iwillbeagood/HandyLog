package com.hand.log.designsystem.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandIn
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun HmFadeAnimatedVisibility(
	visible: Boolean,
	modifier: Modifier = Modifier,
	content:
	@Composable()
	AnimatedVisibilityScope.() -> Unit,
) {
	AnimatedVisibility(
		modifier = modifier,
		visible = visible,
		enter = fadeIn(animationSpec = tween(300)),
		exit = fadeOut(animationSpec = tween(300)),
		content = content,
	)
}

@Composable
fun ExpandAnimatedVisibility(
	visible: Boolean,
	content:
	@Composable()
	AnimatedVisibilityScope.() -> Unit,
) {
	AnimatedVisibility(
		visible = visible,
		enter = expandIn(),
		exit = shrinkOut(),
		content = content,
	)
}

@Composable
fun BottomToTopAnimatedVisibility(
	visible: Boolean,
	content:
	@Composable()
	AnimatedVisibilityScope.() -> Unit,
) {
	AnimatedVisibility(
		visible = visible,
		enter = slideInVertically(
			initialOffsetY = { fullHeight -> fullHeight },
			animationSpec = tween(300),
		),
		exit = slideOutVertically(
			targetOffsetY = { fullHeight -> fullHeight },
			animationSpec = tween(300),
		),
		content = content,
	)
}

@Composable
fun TopToBottomToTopAnimatedVisibility(
	visible: Boolean,
	content: @Composable AnimatedVisibilityScope.() -> Unit,
) {
	AnimatedVisibility(
		visible = visible,
		enter = slideInVertically(
			initialOffsetY = { fullHeight -> -fullHeight },
			animationSpec = tween(300),
		),
		exit = slideOutVertically(
			targetOffsetY = { fullHeight -> -fullHeight },
			animationSpec = tween(300),
		),
		content = content,
	)
}

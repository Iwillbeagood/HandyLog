package com.hand.log.navigation.utils

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.SizeTransform
import androidx.compose.runtime.Composable
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDeepLink
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable

/**
 * Type-safe navigation composable helper for Navigation 3.0.
 *
 * This is a simplified wrapper around the standard composable function
 * that maintains API compatibility while removing the deprecated typeMap parameter.
 *
 * In Navigation 3.0, Kotlin Serialization handles type serialization automatically,
 * so custom NavTypes are no longer needed.
 */
inline fun <reified T : Any> NavGraphBuilder.composableType(
	deepLinks: List<NavDeepLink> = emptyList(),
	noinline enterTransition: (
		AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition?
	)? = null,
	noinline exitTransition: (
		AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition?
	)? = null,
	noinline popEnterTransition: (
		AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition?
	)? = enterTransition,
	noinline popExitTransition: (
		AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition?
	)? = exitTransition,
	noinline sizeTransform: (
		AnimatedContentTransitionScope<NavBackStackEntry>.() -> SizeTransform?
	)? = null,
	noinline content: @Composable AnimatedContentScope.(NavBackStackEntry) -> Unit,
) {
	composable<T>(
		deepLinks = deepLinks,
		enterTransition = enterTransition,
		exitTransition = exitTransition,
		popEnterTransition = popEnterTransition,
		popExitTransition = popExitTransition,
		sizeTransform = sizeTransform,
		content = content,
	)
}



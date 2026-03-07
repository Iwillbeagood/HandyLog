package com.hand.log.navigation.navigation

import androidx.compose.runtime.Immutable

@Immutable
data class RouteStack(
	val backStack: List<Route>,
) {
	val current: Route? = backStack.lastOrNull()

	val previous: Route? = backStack.dropLast(1).lastOrNull()

	constructor(startDestination: Route) : this(backStack = listOf(startDestination))
}

package com.hand.log.navigation.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

interface Route : NavKey

@Serializable
data object Splash : NavKey

sealed interface MainTabRoute : Route {

	@Serializable
	data object Home : MainTabRoute

	@Serializable
	data object Players : MainTabRoute
}

@Serializable
data class TableDetail(val tableId: String) : Route

@Serializable
data class RecordHand(val tableId: String) : Route

@Serializable
data class HandDetail(val handId: String) : Route

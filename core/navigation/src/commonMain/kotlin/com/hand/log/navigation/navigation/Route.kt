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
	data class Players(val openAdd: Boolean = false, val nonce: Long = 0) : MainTabRoute

	@Serializable
	data object Settings : MainTabRoute
}

@Serializable
data class Table(val tableId: String) : Route

@Serializable
data class RecordHand(val tableId: String) : Route

@Serializable
data class HandDetail(val handId: String) : Route

@Serializable
data class PlayerHands(val savedPlayerId: String, val playerName: String) : Route

@Serializable
data object BetSizeSettings : Route

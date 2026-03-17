package com.hand.log.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class PreflopStreet(
	val actions: List<Action> = emptyList(),
)

@Serializable
data class FlopStreet(
	val card1: Card? = null,
	val card2: Card? = null,
	val card3: Card? = null,
	val actions: List<Action> = emptyList(),
) {
	val cards: List<Card> get() = listOfNotNull(card1, card2, card3)
}

@Serializable
data class TurnStreet(
	val card: Card? = null,
	val actions: List<Action> = emptyList(),
) {
	val cards: List<Card> get() = listOfNotNull(card)
}

@Serializable
data class RiverStreet(
	val card: Card? = null,
	val actions: List<Action> = emptyList(),
) {
	val cards: List<Card> get() = listOfNotNull(card)
}

@Serializable
data class HandStreets(
	val preflop: PreflopStreet = PreflopStreet(),
	val flop: FlopStreet? = null,
	val turn: TurnStreet? = null,
	val river: RiverStreet? = null,
) {
	fun getActions(street: Street): List<Action> = when (street) {
		Street.PREFLOP -> preflop.actions
		Street.FLOP -> flop?.actions ?: emptyList()
		Street.TURN -> turn?.actions ?: emptyList()
		Street.RIVER -> river?.actions ?: emptyList()
	}

	fun getCards(street: Street): List<Card> = when (street) {
		Street.PREFLOP -> emptyList()
		Street.FLOP -> flop?.cards ?: emptyList()
		Street.TURN -> turn?.cards ?: emptyList()
		Street.RIVER -> river?.cards ?: emptyList()
	}
}

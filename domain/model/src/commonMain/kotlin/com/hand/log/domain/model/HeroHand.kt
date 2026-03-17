package com.hand.log.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class HeroHand(
	val card1: Card,
	val card2: Card,
) {
	val cards: List<Card> get() = listOf(card1, card2)
}

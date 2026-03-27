package com.hand.log.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class ShowdownEntry(
	val seat: Int,
	val cards: PocketCards,
) {
	val card1: Card get() = cards.card1
	val card2: Card get() = cards.card2
}

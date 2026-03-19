package com.hand.log.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class ShowdownEntry(
	val seat: Int,
	val card1: Card,
	val card2: Card,
)

data class HandRecord(
	val id: String,
	val tableId: String,
	val createdAt: Long,
	val blinds: Blinds? = null,
	val heroHand: HeroHand? = null,
	val heroSeat: Int = 0,
	val heroStack: Double = 0.0,
	val buttonSeat: Int = 1,
	val streets: HandStreets = HandStreets(),
	val showdown: List<ShowdownEntry> = emptyList(),
	val result: Double? = null,
	val memo: String? = null,
) {
	val heroCards: List<Card> get() = heroHand?.cards ?: emptyList()
}

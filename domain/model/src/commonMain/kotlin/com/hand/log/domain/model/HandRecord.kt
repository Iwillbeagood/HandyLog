package com.hand.log.domain.model

data class HandRecord(
	val id: String,
	val tableId: String,
	val createdAt: Long,
	val blinds: Blinds? = null,
	val heroHand: HeroHand? = null,
	val heroStack: Double = 0.0,
	val buttonSeat: Int = 1,
	val streets: HandStreets = HandStreets(),
	val result: Double? = null,
	val memo: String? = null,
) {
	val heroCards: List<Card> get() = heroHand?.cards ?: emptyList()
}

package com.hand.log.domain.model

data class HandRecord(
	val id: String,
	val tableId: String,
	val createdAt: Long,
	val blinds: Blinds? = null,
	val heroCards: List<Card> = emptyList(),
	val heroStack: Double = 0.0,
	val buttonSeat: Int = 1,
	val streets: Map<Street, StreetData> = emptyMap(),
	val result: Double? = null,
	val memo: String? = null,
)

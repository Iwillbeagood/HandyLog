package com.hand.log.domain.model

data class HandHistory(
	val id: String,
	val date: Long,
	val tableSize: Int,
	val heroPosition: Position? = null,
	val players: List<PlayerHand> = emptyList(),
	val communityCards: List<Card> = emptyList(),
	val actions: List<Action> = emptyList(),
	val potSize: Double? = null,
	val notes: String? = null,
	val createdAt: Long = 0L,
)

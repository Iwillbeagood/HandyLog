package com.hand.log.domain.model

data class SavedPlayer(
	val id: String,
	val name: String,
	val tendency: PlayerTendency? = null,
	val memo: String? = null,
	val createdAt: Long = 0L,
)

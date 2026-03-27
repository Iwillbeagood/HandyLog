package com.hand.log.domain.model

data class Player(
	val id: String = "",
	val seat: Int,
	val tendency: PlayerTendency? = null,
	val memo: String? = null,
	val name: String? = null,
	val pocketCards: PocketCards? = null,
)

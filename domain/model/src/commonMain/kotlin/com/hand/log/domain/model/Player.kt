package com.hand.log.domain.model

data class Player(
	val seat: Int,
	val stack: Double,
	val tendency: PlayerTendency? = null,
	val memo: String? = null,
	val name: String? = null,
)

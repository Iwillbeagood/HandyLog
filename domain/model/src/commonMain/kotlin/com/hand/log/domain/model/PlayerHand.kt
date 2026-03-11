package com.hand.log.domain.model

data class PlayerHand(
	val position: Position,
	val card1: Card? = null,
	val card2: Card? = null,
	val result: String? = null,
)

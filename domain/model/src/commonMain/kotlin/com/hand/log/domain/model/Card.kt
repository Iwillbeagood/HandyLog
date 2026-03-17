package com.hand.log.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Card(
	val rank: Rank,
	val suit: Suit,
)

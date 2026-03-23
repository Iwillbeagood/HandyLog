package com.hand.log.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class PocketCards(
	val card1: Card,
	val card2: Card,
)

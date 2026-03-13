package com.hand.log.domain.model

data class StreetData(
	val cards: List<Card> = emptyList(),
	val actions: List<Action> = emptyList(),
)

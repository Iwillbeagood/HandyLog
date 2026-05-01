package com.hand.log.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class HandPlayer(
	val seat: Int,
	val cards: PocketCards? = null,
	val initialStack: Double? = null,
	val ranking: HandRanking? = null,
	val bestCards: List<Card> = emptyList(),
	val outcome: ShowdownOutcome? = null,
	val playerName: String? = null,
	val savedPlayerId: String? = null,
	val isHero: Boolean = false,
) {
	val isWinner: Boolean get() = outcome == ShowdownOutcome.WIN
	val isSplit: Boolean get() = outcome == ShowdownOutcome.SPLIT

	fun toShowdownEntry(): ShowdownEntry? = cards?.let { ShowdownEntry(seat = seat, cards = it) }
}

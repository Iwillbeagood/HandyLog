package com.hand.log.domain.model

import kotlinx.datetime.LocalDate

data class PokerTable(
	val id: String,
	val date: LocalDate,
	val location: String? = null,
	val gameType: GameType,
	val maxPlayers: Int = 0,
	val heroSeat: Int,
	val players: List<Player> = emptyList(),
	val createdAt: Long,
) {
	val playerCount: Int get() = players.size
}

package com.hand.log.tableedit

import com.hand.log.domain.model.PokerTable

internal data class TableSeatResize(
	val heroSeat: Int,
	val removedSeats: Set<Int>,
)

internal fun PokerTable.resizeSeats(maxPlayers: Int): TableSeatResize {
	val otherSeats = players.map { it.seat }.filter { it != heroSeat }.toSet()
	val newHeroSeat = if (heroSeat in 1..maxPlayers) {
		heroSeat
	} else {
		(maxPlayers downTo 1).firstOrNull { it !in otherSeats } ?: maxPlayers
	}
	val removedSeats = otherSeats.filter { it > maxPlayers || it == newHeroSeat }.toSet()
	return TableSeatResize(heroSeat = newHeroSeat, removedSeats = removedSeats)
}

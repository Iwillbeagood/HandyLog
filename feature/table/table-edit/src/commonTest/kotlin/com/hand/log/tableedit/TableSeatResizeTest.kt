package com.hand.log.tableedit

import com.hand.log.domain.model.GameType
import com.hand.log.domain.model.Player
import com.hand.log.domain.model.PokerTable
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

class TableSeatResizeTest {

	private fun table(heroSeat: Int, seats: List<Int>) = PokerTable(
		id = "t",
		date = LocalDate(2026, 1, 1),
		gameType = GameType.Tournament(isBigBlindAnte = true),
		maxPlayers = 9,
		heroSeat = heroSeat,
		players = seats.map { Player(id = "p$it", seat = it) },
		createdAt = 0L,
	)

	@Test
	fun keepsHeroSeatAndRemovesSeatsBeyondMax() {
		val resize = table(heroSeat = 3, seats = (1..9).toList()).resizeSeats(6)
		assertEquals(TableSeatResize(heroSeat = 3, removedSeats = setOf(7, 8, 9)), resize)
	}

	@Test
	fun movesHeroToHighestFreeSeatWhenOutOfRange() {
		val resize = table(heroSeat = 9, seats = listOf(1, 2, 3, 6, 9)).resizeSeats(6)
		assertEquals(TableSeatResize(heroSeat = 5, removedSeats = emptySet()), resize)
	}

	@Test
	fun displacesLastSeatWhenNoSeatIsFree() {
		val resize = table(heroSeat = 9, seats = (1..9).toList()).resizeSeats(6)
		assertEquals(TableSeatResize(heroSeat = 6, removedSeats = setOf(6, 7, 8)), resize)
	}

	@Test
	fun noChangeWhenMaxPlayersGrows() {
		val resize = table(heroSeat = 4, seats = (1..6).toList()).resizeSeats(9)
		assertEquals(TableSeatResize(heroSeat = 4, removedSeats = emptySet()), resize)
	}
}

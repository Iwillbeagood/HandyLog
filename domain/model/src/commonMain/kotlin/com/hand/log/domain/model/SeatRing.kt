package com.hand.log.domain.model

class SeatRing(
	seats: Collection<Int>,
	val buttonSeat: Int,
	private val compactNames: Boolean = false,
) {
	val seats: List<Int> = seats.distinct().sorted()

	val size: Int get() = seats.size

	private val buttonIndex: Int
		get() {
			val exact = seats.indexOf(buttonSeat)
			if (exact >= 0) return exact
			val next = seats.indexOfFirst { it >= buttonSeat }
			return if (next >= 0) next else 0
		}

	fun after(seat: Int, steps: Int): Int {
		if (seats.isEmpty()) return 0
		val base = seats.indexOf(seat).takeIf { it >= 0 } ?: return 0
		return seats[(base + steps) % seats.size]
	}

	private fun afterButton(steps: Int): Int {
		if (seats.isEmpty()) return 0
		return seats[(buttonIndex + steps) % seats.size]
	}

	private val isHeadsUp: Boolean get() = size == 2

	private val sbOffset: Int get() = if (isHeadsUp) 0 else 1

	val sbSeat: Int get() = afterButton(sbOffset)

	val bbSeat: Int get() = afterButton(sbOffset + 1)

	val preflopOrder: List<Int>
		get() = (0 until size).map { afterButton(sbOffset + 2 + it) }

	val postflopOrder: List<Int>
		get() = (0 until size).map { afterButton(1 + it) }

	fun positionOf(seat: Int): Position {
		if (seats.isEmpty()) return Position.BTN
		val btn = afterButton(0)
		if (seat == btn) return Position.BTN
		if (seat == sbSeat) return Position.SB
		if (seat == bbSeat) return Position.BB

		val utgOrder = preflopOrder.filter { it != btn && it != sbSeat && it != bbSeat }
		val idx = utgOrder.indexOf(seat)
		return when {
			idx == 0 -> Position.UTG
			idx == utgOrder.lastIndex -> Position.CO
			size <= 6 -> Position.MP
			idx == utgOrder.lastIndex - 1 -> Position.HJ
			compactNames -> Position.MP
			idx == utgOrder.lastIndex - 2 -> Position.LJ
			idx == 1 -> Position.UTG1
			else -> Position.MP
		}
	}

	fun buttonForPosition(seat: Int, position: Position): Int? =
		seats.firstOrNull { SeatRing(seats, it, compactNames).positionOf(seat) == position }
}

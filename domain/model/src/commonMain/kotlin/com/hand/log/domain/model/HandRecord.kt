package com.hand.log.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class ShowdownEntry(
	val seat: Int,
	val cards: PocketCards,
) {
	val card1: Card get() = cards.card1
	val card2: Card get() = cards.card2
}

data class HandRecord(
	val id: String,
	val tableId: String,
	val createdAt: Long,
	val blinds: Blinds? = null,
	val heroHand: PocketCards? = null,
	val heroSeat: Int = 0,
	val heroStack: Double = 0.0,
	val buttonSeat: Int = 1,
	val streets: HandStreets = HandStreets(),
	val showdown: List<ShowdownEntry> = emptyList(),
	val showdownResults: List<ShowdownResult> = emptyList(),
	val result: Double? = null,
	val memo: String? = null,
) {
	/** 프리플랍에 참여한 모든 좌석 (정렬) */
	val allSeats: List<Int>
		get() = streets.preflop.actions.map { it.playerSeat }.distinct().sorted()

	/** 프리플랍 참여 인원 수 */
	val playerCount: Int
		get() = allSeats.size.coerceAtLeast(2)

	/** 프리플랍에서 폴드한 좌석 */
	val preflopFoldedSeats: Set<Int>
		get() = streets.preflop.actions
			.filter { it.type == ActionType.FOLD }
			.map { it.playerSeat }
			.toSet()

	/** 최종까지 남은 좌석 (모든 스트릿의 폴드 제외) */
	val remainingSeats: Set<Int>
		get() {
			val seats = allSeats.toMutableSet()
			listOf(Street.PREFLOP, Street.FLOP, Street.TURN, Street.RIVER).forEach { street ->
				streets.getActions(street).forEach { action ->
					if (action.type == ActionType.FOLD) seats.remove(action.playerSeat)
				}
			}
			return seats
		}

	/** 쇼다운 승자 좌석 */
	val winnerSeats: Set<Int>
		get() {
			val fromResults = showdownResults.filter { it.isWinner }.map { it.seat }.toSet()
			return if (fromResults.isNotEmpty()) {
				fromResults
			} else if (result != null && result >= 0) {
				setOf(heroSeat)
			} else {
				emptySet()
			}
		}
}

package com.hand.log.record.model

import androidx.compose.runtime.Immutable
import com.hand.log.domain.model.Card
import com.hand.log.domain.model.PocketCards
import com.hand.log.domain.model.ShowdownEntry

@Immutable
data class RecordShowdown(
	val seat1: PocketCards? = null,
	val seat2: PocketCards? = null,
	val seat3: PocketCards? = null,
	val seat4: PocketCards? = null,
	val seat5: PocketCards? = null,
	val seat6: PocketCards? = null,
	val seat7: PocketCards? = null,
	val seat8: PocketCards? = null,
	val seat9: PocketCards? = null,
	val seat10: PocketCards? = null,
	val unknownSeats: Set<Int> = emptySet(),
) {
	/** 해당 좌석이 미공개(?) 상태인지 */
	fun isUnknown(seat: Int): Boolean = seat in unknownSeats

	/** 해당 좌석의 카드가 선택되었거나 미공개 상태인지 (쇼다운 완료 판단용) */
	fun isResolved(seat: Int): Boolean = get(seat) != null || isUnknown(seat)

	fun setUnknown(seat: Int): RecordShowdown =
		copy(unknownSeats = unknownSeats + seat).set(seat, null)

	fun clearUnknown(seat: Int): RecordShowdown =
		copy(unknownSeats = unknownSeats - seat)
	operator fun get(seat: Int): PocketCards? = when (seat) {
		1 -> seat1
		2 -> seat2
		3 -> seat3
		4 -> seat4
		5 -> seat5
		6 -> seat6
		7 -> seat7
		8 -> seat8
		9 -> seat9
		10 -> seat10
		else -> null
	}

	fun set(seat: Int, hand: PocketCards?): RecordShowdown = when (seat) {
		1 -> copy(seat1 = hand)
		2 -> copy(seat2 = hand)
		3 -> copy(seat3 = hand)
		4 -> copy(seat4 = hand)
		5 -> copy(seat5 = hand)
		6 -> copy(seat6 = hand)
		7 -> copy(seat7 = hand)
		8 -> copy(seat8 = hand)
		9 -> copy(seat9 = hand)
		10 -> copy(seat10 = hand)
		else -> this
	}

	val allCards: Set<Card>
		get() = listOfNotNull(seat1, seat2, seat3, seat4, seat5, seat6, seat7, seat8, seat9, seat10)
			.flatMap { listOf(it.card1, it.card2) }
			.toSet()

	fun toShowdownEntries(): List<ShowdownEntry> = buildList {
		listOf(
			1 to seat1, 2 to seat2, 3 to seat3, 4 to seat4, 5 to seat5,
			6 to seat6, 7 to seat7, 8 to seat8, 9 to seat9, 10 to seat10,
		).forEach { (seat, hand) ->
			if (hand != null) {
				add(ShowdownEntry(seat = seat, cards = hand))
			}
		}
	}
}

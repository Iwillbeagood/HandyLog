package com.hand.log.domain.model

import com.hand.log.domain.model.quick.QuickHandConfig
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

data class HandWithTable(
	val hand: HandRecord,
	val table: PokerTable?,
) {
	val date: LocalDate
		get() = table?.date ?: Instant.fromEpochMilliseconds(hand.createdAt)
			.toLocalDateTime(TimeZone.currentSystemDefault())
			.date

	val displayBlinds: Blinds?
		get() = hand.blinds?.takeUnless { table == null && it == QuickHandConfig.BB_UNIT_BLINDS }
}

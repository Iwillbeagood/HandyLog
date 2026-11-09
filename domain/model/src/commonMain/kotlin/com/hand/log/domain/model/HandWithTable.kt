package com.hand.log.domain.model

import com.hand.log.domain.model.quick.QuickHandConfig
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

data class HandWithTable(
	val hand: HandRecord,
	val table: PokerTable?,
) {
	@OptIn(ExperimentalTime::class)
	val date: LocalDate
		get() = table?.date ?: Instant.fromEpochMilliseconds(hand.createdAt)
			.toLocalDateTime(TimeZone.currentSystemDefault())
			.date

	val displayBlinds: Blinds?
		get() = hand.blinds?.takeUnless { table == null && it == QuickHandConfig.BB_UNIT_BLINDS }
}

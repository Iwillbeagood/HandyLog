package com.hand.log.preflop.quiz.common

import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/** epoch millis → "2026.07.30 14:32" 형태의 로컬 시각 문자열. 퀴즈를 언제 풀었는지 표시용. */
fun formatQuizPlayedAt(epochMillis: Long): String {
	val dt = Instant.fromEpochMilliseconds(
		epochMillis,
	).toLocalDateTime(TimeZone.currentSystemDefault())
	fun pad(value: Int): String = value.toString().padStart(2, '0')
	return "${dt.year}.${pad(dt.monthNumber)}.${pad(dt.dayOfMonth)} ${pad(dt.hour)}:${pad(dt.minute)}"
}

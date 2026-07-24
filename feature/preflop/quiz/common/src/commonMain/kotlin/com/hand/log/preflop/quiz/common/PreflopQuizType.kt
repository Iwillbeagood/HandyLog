package com.hand.log.preflop.quiz.common

/** 퀴즈 유형. MIXED 는 아직 잠금(향후 오픈). */
enum class PreflopQuizType(val locked: Boolean) {
	RFI(locked = false),
	VS_OPEN(locked = false),
	MIXED(locked = true),
}

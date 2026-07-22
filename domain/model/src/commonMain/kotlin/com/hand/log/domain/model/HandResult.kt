package com.hand.log.domain.model

enum class HeroResultType {
	FOLD_WIN,
	FOLD_LOSE,
	SHOWDOWN_WIN,
	SHOWDOWN_LOSE,
	SHOWDOWN_SPLIT,
}

/** 히어로(나) 기준 승/무/패 — 전적 집계용 */
enum class HeroOutcome {
	WIN,
	LOSE,
	DRAW,
}

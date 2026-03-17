package com.hand.log.domain.model

enum class Position(val label: String) {
	BTN("BTN"),
	SB("SB"),
	BB("BB"),
	UTG("UTG"),
	UTG1("UTG+1"),
	UTG2("UTG+2"),
	LJ("LJ"),
	HJ("HJ"),
	CO("CO"),
	MP("MP"),
	;

	companion object {
		/** 플레이어 수에 따른 포지션 목록 (고정 순서) */
		fun forPlayerCount(count: Int): List<Position> = when (count) {
			2 -> listOf(BTN, BB)
			3 -> listOf(BTN, SB, BB)
			4 -> listOf(BTN, SB, BB, UTG)
			5 -> listOf(BTN, SB, BB, UTG, CO)
			6 -> listOf(BTN, SB, BB, UTG, MP, CO)
			7 -> listOf(BTN, SB, BB, UTG, LJ, HJ, CO)
			8 -> listOf(BTN, SB, BB, UTG, UTG1, LJ, HJ, CO)
			9 -> listOf(BTN, SB, BB, UTG, UTG1, LJ, HJ, CO, MP)
			10 -> listOf(BTN, SB, BB, UTG, UTG1, UTG2, LJ, HJ, CO, MP)
			else -> listOf(BTN, SB, BB)
		}
	}
}

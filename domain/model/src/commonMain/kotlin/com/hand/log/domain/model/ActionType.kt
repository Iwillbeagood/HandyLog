package com.hand.log.domain.model

enum class ActionType {
	FOLD,
	CHECK,
	CALL,
	BET,
	RAISE,
	ALL_IN,
	;

	companion object {
		val PREFLOP_BB_OPTION = listOf(CHECK, RAISE, ALL_IN)
		val PREFLOP_DEFAULT = listOf(FOLD, CALL, RAISE, ALL_IN)
		val POSTFLOP_NO_BET = listOf(CHECK, BET, ALL_IN)
		val POSTFLOP_FACING_BET = listOf(FOLD, CALL, RAISE, ALL_IN)
	}
}

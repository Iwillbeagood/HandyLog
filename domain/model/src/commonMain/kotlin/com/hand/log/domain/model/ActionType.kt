package com.hand.log.domain.model

enum class ActionType(val label: String) {
	FOLD("폴드"),
	CHECK("체크"),
	CALL("콜"),
	BET("벳"),
	RAISE("레이즈"),
	ALL_IN("올인"),
	;

	companion object {
		/** 프리플랍 BB 옵션 (아무도 레이즈 안 했을 때) */
		val PREFLOP_BB_OPTION = listOf(CHECK, RAISE, ALL_IN)

		/** 프리플랍 기본 (폴드/콜/레이즈/올인) */
		val PREFLOP_DEFAULT = listOf(FOLD, CALL, RAISE, ALL_IN)

		/** 포스트플랍 벳이 없을 때 (체크/벳/올인) */
		val POSTFLOP_NO_BET = listOf(CHECK, BET, ALL_IN)

		/** 포스트플랍 벳/레이즈 있을 때 (폴드/콜/레이즈/올인) */
		val POSTFLOP_FACING_BET = listOf(FOLD, CALL, RAISE, ALL_IN)
	}
}

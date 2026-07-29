package com.hand.log.domain.model.preflop

/**
 * 차트 칸의 권장 액션. 원본 차트가 레이즈를 오픈/3벳/4벳으로 구분하고 각각 밸류·블러프로 나누므로 그대로 반영한다.
 */
enum class PreflopAction {
	RAISE,
	RAISE_BLUFF,
	THREE_BET,
	THREE_BET_BLUFF,
	FOUR_BET,
	FOUR_BET_BLUFF,
	ALL_IN,
	CALL,
	LIMP,
	FOLD,
}

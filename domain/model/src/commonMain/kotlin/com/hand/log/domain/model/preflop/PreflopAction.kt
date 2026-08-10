package com.hand.log.domain.model.preflop

/**
 * 차트 칸의 권장 액션. 원본 차트가 오픈/3벳/4벳을 밸류·블러프로 나누고,
 * 나아가 오픈 후 대응(3벳 맞으면 폴드/콜/4벳/잼)과 3벳 후 대응(4벳 맞으면 폴드/콜/스택오프/잼)까지
 * 세분해 표기하므로 각 라인을 개별 액션으로 그대로 반영한다.
 */
enum class PreflopAction {
	// 오픈(RFI) 계열 — 오픈 후 3벳에 대한 대응 계획까지 구분.
	RAISE,
	RAISE_BLUFF,
	RAISE_FOLD,
	RAISE_CALL,
	RAISE_4BET,
	RAISE_JAM,

	// 3벳 계열 — 3벳 후 4벳에 대한 대응 계획까지 구분.
	THREE_BET,
	THREE_BET_BLUFF,
	THREE_BET_STACKOFF,
	THREE_BET_FOLD,
	THREE_BET_CALL,
	THREE_BET_JAM,

	// 4벳 계열.
	FOUR_BET,
	FOUR_BET_BLUFF,

	// 패시브/기타.
	ALL_IN,
	CALL,
	LIMP,
	CHECK,
	FOLD,
}

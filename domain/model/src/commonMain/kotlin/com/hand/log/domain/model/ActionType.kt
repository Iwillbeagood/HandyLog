package com.hand.log.domain.model

enum class ActionType(val label: String) {
	FOLD("폴드"),
	CHECK("체크"),
	CALL("콜"),
	BET("벳"),
	RAISE("레이즈"),
	ALL_IN("올인"),
}

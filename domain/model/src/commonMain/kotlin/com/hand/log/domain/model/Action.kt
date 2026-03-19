package com.hand.log.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Action(
	val playerSeat: Int,
	val type: ActionType,
	val amount: Double? = null,
	val stackBefore: Double? = null,
	val stackAfter: Double? = null,
	val betLevel: Int = 0,
) {
	val label: String
		get() = when {
			type == ActionType.ALL_IN -> "올인"
			type == ActionType.FOLD -> "폴드"
			type == ActionType.CHECK -> "체크"
			type == ActionType.CALL -> "콜"
			betLevel <= 0 -> type.label
			betLevel == 1 -> "벳"
			betLevel == 2 -> "레이즈"
			else -> "${betLevel}벳"
		}
}

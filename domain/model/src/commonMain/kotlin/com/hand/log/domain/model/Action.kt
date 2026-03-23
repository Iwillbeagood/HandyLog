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
)

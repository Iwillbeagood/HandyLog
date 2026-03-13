package com.hand.log.domain.model

data class Action(
	val playerSeat: Int,
	val type: ActionType,
	val amount: Double? = null,
)

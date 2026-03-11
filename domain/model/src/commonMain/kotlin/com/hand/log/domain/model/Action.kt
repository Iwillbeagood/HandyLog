package com.hand.log.domain.model

data class Action(
	val street: Street,
	val position: Position,
	val actionType: ActionType,
	val amount: Double? = null,
	val actionOrder: Int,
)

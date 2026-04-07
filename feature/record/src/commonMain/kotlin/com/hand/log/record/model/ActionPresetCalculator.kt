package com.hand.log.record.model

import com.hand.log.domain.model.Action
import com.hand.log.domain.model.ActionType
import com.hand.log.domain.model.Street

/**
 * x2, x3 등 배수 프리셋의 기준이 되는 마지막 유효 레이즈 금액을 계산한다.
 * 숏스택 올인(이전 최대 베팅보다 작은 올인)은 유효 레이즈가 아니므로 제외한다.
 */
fun calculateLastRaiseTo(
	streetActions: List<Action>,
	currentStreet: Street,
	bb: Double,
): Double {
	var currentMax = if (currentStreet == Street.PREFLOP) bb else 0.0
	var lastRaiseTo = 0.0
	streetActions.forEach { action ->
		val amount = action.amount ?: 0.0
		if (amount > currentMax &&
			(action.type == ActionType.BET || action.type == ActionType.RAISE || action.type == ActionType.ALL_IN)
		) {
			lastRaiseTo = amount
			currentMax = amount
		}
	}
	return lastRaiseTo
}

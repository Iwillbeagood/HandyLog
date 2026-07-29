package com.hand.log.domain.model.preflop

import com.hand.log.domain.model.Position

/** 프리플랍 차트 화면의 조건 선택 상태 — 로컬에 저장되어 재진입 시 유지된다. */
data class PreflopSelection(
	val stack: PreflopStack,
	val scenario: PreflopScenario,
	val hero: Position,
	val villain: Position?,
) {
	companion object {
		val DEFAULT = PreflopSelection(
			stack = PreflopStack.BB100,
			scenario = PreflopScenario.RFI,
			hero = Position.BTN,
			villain = null,
		)
	}
}

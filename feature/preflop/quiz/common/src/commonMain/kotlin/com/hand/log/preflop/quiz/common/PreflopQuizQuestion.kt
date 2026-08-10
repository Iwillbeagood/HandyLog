package com.hand.log.preflop.quiz.common

import com.hand.log.domain.model.Position
import com.hand.log.domain.model.preflop.PreflopAction
import com.hand.log.domain.model.preflop.PreflopHand
import com.hand.log.domain.model.preflop.PreflopScenario
import com.hand.log.domain.model.preflop.PreflopStack

data class PreflopQuizQuestion(
	val stack: PreflopStack,
	val scenario: PreflopScenario,
	val hero: Position,
	val villain: Position?,
	val hand: PreflopHand,
	val correct: PreflopAction,
	val options: List<PreflopAction>,
	// 리레이즈 대응(2차) 선택지 — 정답에 대응 계획이 있을 때만 채워진다.
	val planOptions: List<PreflopAction> = emptyList(),
)

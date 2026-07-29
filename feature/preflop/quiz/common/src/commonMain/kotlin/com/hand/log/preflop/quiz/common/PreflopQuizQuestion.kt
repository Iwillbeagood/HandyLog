package com.hand.log.preflop.quiz.common

import com.hand.log.domain.model.Position
import com.hand.log.domain.model.preflop.PreflopHand
import com.hand.log.domain.model.preflop.PreflopScenario
import com.hand.log.domain.model.preflop.PreflopStack

data class PreflopQuizQuestion(
	val stack: PreflopStack,
	val scenario: PreflopScenario,
	val hero: Position,
	val villain: Position?,
	val hand: PreflopHand,
	val correct: QuizAnswer,
	val options: List<QuizAnswer>,
)

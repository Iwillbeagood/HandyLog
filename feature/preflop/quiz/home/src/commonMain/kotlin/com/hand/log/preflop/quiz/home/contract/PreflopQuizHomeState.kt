package com.hand.log.preflop.quiz.home.contract

import com.hand.log.domain.model.preflop.PreflopStack
import com.hand.log.domain.model.preflop.QuizRecord
import com.hand.log.preflop.quiz.common.PreflopQuizType

internal data class PreflopQuizHomeState(
	val selectedStack: PreflopStack? = null,
	val availableTypes: Set<PreflopQuizType> = PreflopQuizType.entries.toSet(),
	val records: List<QuizRecord> = emptyList(),
) {
	val stackOptions: List<PreflopStack?> get() = listOf(null) + PreflopStack.entries

	fun isTypeEnabled(type: PreflopQuizType): Boolean = type in availableTypes
}

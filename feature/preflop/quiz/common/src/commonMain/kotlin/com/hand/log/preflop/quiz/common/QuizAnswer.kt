package com.hand.log.preflop.quiz.common

import com.hand.log.domain.model.preflop.PreflopAction

/** 퀴즈 정답 선택지(4개 버튼). 차트의 세분화된 액션을 이 4개로 묶어 채점한다. */
enum class QuizAnswer { RAISE_VALUE, RAISE_BLUFF, CALL, FOLD }

fun PreflopAction.toQuizAnswer(): QuizAnswer = when (this) {
	PreflopAction.RAISE, PreflopAction.THREE_BET, PreflopAction.FOUR_BET -> QuizAnswer.RAISE_VALUE
	PreflopAction.RAISE_BLUFF, PreflopAction.THREE_BET_BLUFF, PreflopAction.FOUR_BET_BLUFF -> QuizAnswer.RAISE_BLUFF
	PreflopAction.CALL, PreflopAction.LIMP -> QuizAnswer.CALL
	PreflopAction.FOLD -> QuizAnswer.FOLD
}

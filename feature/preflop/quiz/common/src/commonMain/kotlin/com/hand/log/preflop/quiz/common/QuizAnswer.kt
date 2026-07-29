package com.hand.log.preflop.quiz.common

import com.hand.log.domain.model.preflop.PreflopAction
import com.hand.log.domain.model.preflop.PreflopScenario

/**
 * 퀴즈 정답 선택지. 차트의 세분화된 액션을 액션 카테고리(공격/콜/폴드)로 묶어 채점한다.
 * 공격([RAISE])의 실제 표기(레이즈·3벳·4벳)는 시나리오에 따라 달라지므로 라벨은 화면에서 결정한다.
 */
enum class QuizAnswer { RAISE, CALL, FOLD }

fun PreflopAction.toQuizAnswer(): QuizAnswer = when (this) {
	PreflopAction.RAISE, PreflopAction.RAISE_BLUFF,
	PreflopAction.THREE_BET, PreflopAction.THREE_BET_BLUFF,
	PreflopAction.FOUR_BET, PreflopAction.FOUR_BET_BLUFF,
	PreflopAction.ALL_IN,
	-> QuizAnswer.RAISE
	PreflopAction.CALL, PreflopAction.LIMP -> QuizAnswer.CALL
	PreflopAction.FOLD -> QuizAnswer.FOLD
}

/**
 * 시나리오별 정답 선택지. RFI 는 콜 라인이 없어 공격/폴드만, 나머지는 공격/콜/폴드.
 * 특정 차트가 아니라 시나리오 기준으로 고정하여 선택지 개수로 정답이 유추되지 않게 한다.
 */
fun PreflopScenario.answerOptions(): List<QuizAnswer> = when (this) {
	PreflopScenario.RFI -> listOf(QuizAnswer.RAISE, QuizAnswer.FOLD)
	PreflopScenario.FACING_RFI, PreflopScenario.VS_3BET ->
		listOf(QuizAnswer.RAISE, QuizAnswer.CALL, QuizAnswer.FOLD)
}

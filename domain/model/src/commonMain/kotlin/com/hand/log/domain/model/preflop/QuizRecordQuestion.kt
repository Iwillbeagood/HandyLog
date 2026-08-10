package com.hand.log.domain.model.preflop

import com.hand.log.domain.model.Position
import kotlinx.serialization.Serializable

/**
 * 퀴즈 기록에 함께 저장하는 문제 1개 스냅샷. 결과 화면에서 과거 기록의 리뷰를 복원하는 데 쓰인다.
 * [userAnswer] 가 null 이면 건너뛴 문제.
 */
@Serializable
data class QuizRecordQuestion(
	val stack: PreflopStack,
	val scenario: PreflopScenario,
	val hero: Position,
	val villain: Position?,
	val hand: PreflopHand,
	val correct: PreflopAction,
	val options: List<PreflopAction>,
	val userAnswer: PreflopAction?,
)

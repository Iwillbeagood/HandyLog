package com.hand.log.domain.model.preflop

/**
 * 프리플랍 퀴즈 한 문제에 대한 LLM 해설 요청용 데이터.
 *
 * 정답 액션([correctActionLabel])은 차트에서 이미 확정된 값이므로 LLM 은 이를 바꾸거나
 * 반박하지 않고 "왜 그 액션이 정답인지"만 설명한다 — 환각 여지를 최소화한다.
 * 모든 필드는 이미 사용자 로케일로 포맷된 표시용 문자열이다(프롬프트에 그대로 삽입).
 */
data class QuizReviewSpot(
	val stackLabel: String,
	val heroLabel: String,
	val scenarioLabel: String,
	val handNotation: String,
	val correctActionLabel: String,
	val userAnswerLabel: String,
	val isCorrect: Boolean,
	val languageName: String,
)

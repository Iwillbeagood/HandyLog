package com.hand.log.domain.model.preflop

/** 완료한 프리플랍 퀴즈 세션 1회의 기록. [questions] 는 결과 화면에서 리뷰를 복원하기 위한 문제 스냅샷. */
data class QuizRecord(
	val id: String,
	val quizType: String,
	val score: Int,
	val total: Int,
	val avgResponseMs: Long,
	val bestStreak: Int,
	val playedAt: Long,
	val questions: List<QuizRecordQuestion> = emptyList(),
)

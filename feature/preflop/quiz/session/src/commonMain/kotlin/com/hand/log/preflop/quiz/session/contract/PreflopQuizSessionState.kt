package com.hand.log.preflop.quiz.session.contract

import com.hand.log.domain.model.preflop.PreflopAction
import com.hand.log.preflop.quiz.common.PreflopQuizQuestion
import com.hand.log.preflop.quiz.common.PreflopQuizType

internal enum class QuizPhase { LOADING, PLAYING, RESULT, REVIEW }

internal enum class ReviewStatus { IDLE, LOADING, LOADED, ERROR }

internal data class PreflopQuizSessionState(
	val phase: QuizPhase = QuizPhase.LOADING,
	val questions: List<PreflopQuizQuestion> = emptyList(),
	val index: Int = 0,
	val answers: List<PreflopAction?> = emptyList(),
	// 현재 문제에서 1차(첫 액션)를 고른 뒤 2차(리레이즈 대응)를 기다리는 중이면 그 1차 액션. null 이면 1차 선택 단계.
	val pendingPrimary: PreflopAction? = null,
	val result: QuizResult? = null,
	val reviewIndex: Int = 0,
	val reviewStatus: ReviewStatus = ReviewStatus.IDLE,
	val reviewText: String = "",
) {
	val current: PreflopQuizQuestion? get() = questions.getOrNull(index)
	val total: Int get() = questions.size

	/** 오답(또는 건너뛴) 문제의 원본 인덱스 목록 — 결과 후 리뷰 대상. */
	val reviewQuestionIndices: List<Int>
		get() = questions.indices.filter { answers.getOrNull(it) != questions[it].correct }

	val reviewTotal: Int get() = reviewQuestionIndices.size

	val reviewQuestion: PreflopQuizQuestion?
		get() = reviewQuestionIndices.getOrNull(reviewIndex)?.let { questions[it] }

	val reviewUserAnswer: PreflopAction?
		get() = reviewQuestionIndices.getOrNull(reviewIndex)?.let { answers.getOrNull(it) }
}

internal data class QuizResult(
	val score: Int,
	val total: Int,
	val accuracyPct: Int,
	val avgResponseMs: Long,
	val bestStreak: Int,
	val quizType: PreflopQuizType,
	val playedAt: Long,
)

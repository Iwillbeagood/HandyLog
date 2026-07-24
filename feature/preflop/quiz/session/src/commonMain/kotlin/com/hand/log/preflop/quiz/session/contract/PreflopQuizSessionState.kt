package com.hand.log.preflop.quiz.session.contract

import com.hand.log.preflop.quiz.common.PreflopQuizQuestion
import com.hand.log.preflop.quiz.common.QuizAnswer

internal enum class QuizPhase { LOADING, PLAYING, RESULT }

internal enum class ReviewStatus { IDLE, LOADING, LOADED, ERROR }

internal data class PreflopQuizSessionState(
	val phase: QuizPhase = QuizPhase.LOADING,
	val questions: List<PreflopQuizQuestion> = emptyList(),
	val index: Int = 0,
	val selected: QuizAnswer? = null,
	val score: Int = 0,
	val streak: Int = 0,
	val bestStreak: Int = 0,
	val result: QuizResult? = null,
	val reviewStatus: ReviewStatus = ReviewStatus.IDLE,
	val reviewText: String = "",
) {
	val current: PreflopQuizQuestion? get() = questions.getOrNull(index)
	val total: Int get() = questions.size
	val answered: Boolean get() = selected != null
}

internal data class QuizResult(
	val score: Int,
	val total: Int,
	val accuracyPct: Int,
	val avgResponseMs: Long,
	val bestStreak: Int,
)

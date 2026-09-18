package com.hand.log.preflop.quiz.session.contract

import com.hand.log.domain.model.preflop.PreflopAction
import com.hand.log.domain.model.preflop.PreflopChart
import com.hand.log.domain.model.preflop.PreflopChartQuery
import com.hand.log.preflop.quiz.common.PreflopQuizQuestion
import com.hand.log.preflop.quiz.common.PreflopQuizType

internal enum class QuizPhase { LOADING, PLAYING, RESULT, REVIEW }

internal enum class ReviewStatus { IDLE, LOADING, LOADED, ERROR }

internal enum class ReviewOutcome { CORRECT, WRONG, SKIPPED }

internal data class PreflopQuizSessionState(
	val phase: QuizPhase = QuizPhase.LOADING,
	val questions: List<PreflopQuizQuestion> = emptyList(),
	val index: Int = 0,
	val answers: List<PreflopAction?> = emptyList(),
	val charts: Map<PreflopChartQuery, PreflopChart> = emptyMap(),
	val result: QuizResult? = null,
	val reviewIndex: Int = 0,
	val reviewStatus: ReviewStatus = ReviewStatus.IDLE,
	val reviewText: String = "",
) {
	val current: PreflopQuizQuestion? get() = questions.getOrNull(index)
	val total: Int get() = questions.size

	val progress: Float get() = if (total == 0) 0f else (index + 1).toFloat() / total

	val canGoPrevious: Boolean get() = index > 0

	val canReviewPrev: Boolean get() = reviewIndex > 0

	val hasNextReview: Boolean get() = reviewIndex < reviewTotal - 1

	val showChartAction: Boolean get() = phase == QuizPhase.RESULT || phase == QuizPhase.REVIEW

	val reviewQuestionIndices: List<Int>
		get() = questions.indices.sortedBy { outcomeAt(it) == ReviewOutcome.CORRECT }

	val reviewTotal: Int get() = reviewQuestionIndices.size

	/** 리뷰 대상 문제들 — 상단 선택 카드에서 순서대로 노출. */
	val reviewQuestions: List<PreflopQuizQuestion>
		get() = reviewQuestionIndices.map { questions[it] }

	val reviewOutcomes: List<ReviewOutcome>
		get() = reviewQuestionIndices.map { outcomeAt(it) }

	val reviewQuestion: PreflopQuizQuestion?
		get() = reviewQuestionIndices.getOrNull(reviewIndex)?.let { questions[it] }

	val reviewChart: PreflopChart?
		get() = reviewQuestion?.let {
			charts[PreflopChartQuery(it.stack, it.scenario, it.hero, it.villain)]
		}

	val reviewUserAnswer: PreflopAction?
		get() = reviewQuestionIndices.getOrNull(reviewIndex)?.let { answers.getOrNull(it) }

	val reviewOutcome: ReviewOutcome?
		get() = reviewQuestionIndices.getOrNull(reviewIndex)?.let { outcomeAt(it) }

	private fun outcomeAt(index: Int): ReviewOutcome {
		val answer = answers.getOrNull(index)
		return when {
			answer == null -> ReviewOutcome.SKIPPED
			answer == questions[index].correct -> ReviewOutcome.CORRECT
			else -> ReviewOutcome.WRONG
		}
	}
}

internal data class QuizResult(
	val score: Int,
	val total: Int,
	val accuracyPct: Int,
	val avgResponseMs: Long,
	val bestStreak: Int,
	val quizType: PreflopQuizType,
	val playedAt: Long,
) {
	val scoreFraction: Float get() = if (total == 0) 0f else score.toFloat() / total
}

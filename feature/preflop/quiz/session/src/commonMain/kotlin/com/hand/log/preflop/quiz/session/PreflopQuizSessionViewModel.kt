package com.hand.log.preflop.quiz.session

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hand.log.domain.model.Position
import com.hand.log.domain.model.preflop.PreflopScenario
import com.hand.log.domain.model.preflop.QuizRecord
import com.hand.log.domain.model.preflop.QuizReviewSpot
import com.hand.log.domain.repository.QuizRecordRepository
import com.hand.log.domain.repository.AiReviewRepository
import com.hand.log.preflop.chart.data.PreflopChartRepository
import com.hand.log.preflop.quiz.common.PreflopQuizType
import com.hand.log.preflop.quiz.common.QuizAnswer
import com.hand.log.preflop.quiz.common.QuizQuestionGenerator
import com.hand.log.preflop.quiz.session.contract.PreflopQuizSessionState
import com.hand.log.preflop.quiz.session.contract.QuizPhase
import com.hand.log.preflop.quiz.session.contract.QuizResult
import com.hand.log.preflop.quiz.session.contract.ReviewStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.TimeSource
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalTime::class, ExperimentalUuidApi::class)
internal class PreflopQuizSessionViewModel(
	quizTypeName: String,
	private val chartRepository: PreflopChartRepository,
	private val generator: QuizQuestionGenerator,
	private val quizRecordRepository: QuizRecordRepository,
	private val aiReviewRepository: AiReviewRepository,
) : ViewModel() {

	private val quizType = runCatching { PreflopQuizType.valueOf(quizTypeName) }
		.getOrDefault(PreflopQuizType.RFI)

	private val responseTimes = mutableListOf<Long>()
	private var questionMark = TimeSource.Monotonic.markNow()

	private val _state = MutableStateFlow(PreflopQuizSessionState())
	val state: StateFlow<PreflopQuizSessionState> = _state

	init {
		start()
	}

	private fun start() {
		responseTimes.clear()
		_state.value = PreflopQuizSessionState(phase = QuizPhase.LOADING)
		viewModelScope.launch {
			val charts = chartRepository.load().charts
			val questions = generator.generate(charts, quizType, QUESTION_COUNT)
			questionMark = TimeSource.Monotonic.markNow()
			_state.value = PreflopQuizSessionState(
				phase = if (questions.isEmpty()) QuizPhase.RESULT else QuizPhase.PLAYING,
				questions = questions,
			)
		}
	}

	fun onAnswer(answer: QuizAnswer) = record(answer)

	fun onSkip() = record(null)

	fun onRetry() = start()

	/** 정답 공개 없이 답변만 기록하고 곧바로 다음 문제로 넘어간다. 결과는 전 문제를 푼 뒤 공개한다. */
	private fun record(answer: QuizAnswer?) {
		val s = _state.value
		if (s.phase != QuizPhase.PLAYING || s.current == null) return
		responseTimes.add(questionMark.elapsedNow().inWholeMilliseconds)
		val answers = s.answers + answer
		if (s.index + 1 >= s.total) {
			finish(answers)
		} else {
			questionMark = TimeSource.Monotonic.markNow()
			_state.update { it.copy(index = it.index + 1, answers = answers) }
		}
	}

	private fun finish(answers: List<QuizAnswer?>) {
		val questions = _state.value.questions
		val total = questions.size
		val score = answers.indices.count { answers[it] == questions[it].correct }
		val bestStreak = bestStreak(answers, questions.map { it.correct })
		val avg = if (responseTimes.isEmpty()) 0L else responseTimes.average().toLong()
		val accuracy = if (total == 0) 0 else score * 100 / total
		val result = QuizResult(score, total, accuracy, avg, bestStreak)

		_state.update {
			it.copy(phase = QuizPhase.RESULT, answers = answers, result = result)
		}

		if (total > 0) {
			viewModelScope.launch {
				quizRecordRepository.save(
					QuizRecord(
						id = Uuid.random().toString(),
						quizType = quizType.name,
						score = score,
						total = total,
						avgResponseMs = avg,
						bestStreak = bestStreak,
						playedAt = Clock.System.now().toEpochMilliseconds(),
					),
				)
			}
		}
	}

	private fun bestStreak(answers: List<QuizAnswer?>, correct: List<QuizAnswer>): Int {
		var streak = 0
		var best = 0
		answers.forEachIndexed { i, a ->
			if (a == correct[i]) {
				streak++
				best = maxOf(best, streak)
			} else {
				streak = 0
			}
		}
		return best
	}

	fun onStartReview() {
		if (_state.value.reviewTotal == 0) return
		_state.update {
			it.copy(
				phase = QuizPhase.REVIEW,
				reviewIndex = 0,
				reviewStatus = ReviewStatus.IDLE,
				reviewText = "",
			)
		}
	}

	fun onReviewPrev() = moveReview(-1)

	fun onReviewNext() = moveReview(1)

	fun onExitReview() {
		_state.update { it.copy(phase = QuizPhase.RESULT) }
	}

	private fun moveReview(delta: Int) {
		val s = _state.value
		val next = s.reviewIndex + delta
		if (next < 0 || next >= s.reviewTotal) return
		_state.update {
			it.copy(reviewIndex = next, reviewStatus = ReviewStatus.IDLE, reviewText = "")
		}
	}

	/**
	 * 리뷰 중인 문제의 정답 액션에 대한 LLM 해설을 요청한다. 정답은 차트에서 확정된 값이므로
	 * LLM 은 "왜 정답인지"만 설명한다. 응답이 늦게 도착해도 다른 문제로 넘어갔다면 무시한다.
	 */
	fun onRequestReview(languageName: String) {
		val s = _state.value
		val question = s.reviewQuestion ?: return
		val userAnswer = s.reviewUserAnswer
		if (s.reviewStatus == ReviewStatus.LOADING || s.reviewStatus == ReviewStatus.LOADED) return

		val requestedIndex = s.reviewIndex
		_state.update { it.copy(reviewStatus = ReviewStatus.LOADING) }

		viewModelScope.launch {
			val spot = QuizReviewSpot(
				stackLabel = question.stack.label,
				heroLabel = question.hero.label,
				scenarioLabel = scenarioLabel(question.scenario, question.villain),
				handNotation = question.hand.notation,
				correctActionLabel = actionLabel(question.correct, question.scenario),
				userAnswerLabel = userAnswer?.let { actionLabel(it, question.scenario) } ?: "건너뜀",
				isCorrect = userAnswer == question.correct,
				languageName = languageName,
			)
			val result = runCatching { aiReviewRepository.reviewQuizSpot(spot) }
				.mapCatching { it.ifBlank { error("empty review") } }
			_state.update { st ->
				if (st.reviewIndex != requestedIndex) return@update st
				result.fold(
					onSuccess = { st.copy(reviewStatus = ReviewStatus.LOADED, reviewText = it) },
					onFailure = { st.copy(reviewStatus = ReviewStatus.ERROR) },
				)
			}
		}
	}

	// LLM 프롬프트에 삽입할 스팟 설명 문자열(UI 표시가 아닌 프롬프트 데이터).
	private fun scenarioLabel(scenario: PreflopScenario, villain: Position?): String = when (scenario) {
		PreflopScenario.RFI -> "RFI — 앞 포지션이 모두 폴드, 내가 첫 오픈 여부를 결정하는 상황"
		PreflopScenario.FACING_RFI -> "앞 포지션 ${villain?.label ?: "상대"}의 오픈 레이즈에 대응하는 상황"
		PreflopScenario.VS_3BET -> "내 오픈에 ${villain?.label ?: "상대"}가 3벳한 상황"
	}

	private fun actionLabel(answer: QuizAnswer, scenario: PreflopScenario): String = when (answer) {
		QuizAnswer.RAISE -> when (scenario) {
			PreflopScenario.RFI -> "레이즈"
			PreflopScenario.FACING_RFI -> "3벳"
			PreflopScenario.VS_3BET -> "4벳"
		}
		QuizAnswer.CALL -> "콜"
		QuizAnswer.FOLD -> "폴드"
	}

	companion object {
		const val QUESTION_COUNT = 10
	}
}

package com.hand.log.preflop.quiz.session

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hand.log.domain.model.Position
import com.hand.log.domain.model.preflop.PreflopScenario
import com.hand.log.domain.model.preflop.QuizRecord
import com.hand.log.domain.model.preflop.QuizReviewSpot
import com.hand.log.domain.repository.QuizRecordRepository
import com.hand.log.domain.usecase.ReviewSpotUseCase
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
	private val reviewSpot: ReviewSpotUseCase,
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
			val charts = chartRepository.load()
			val questions = generator.generate(charts, quizType, QUESTION_COUNT)
			questionMark = TimeSource.Monotonic.markNow()
			_state.value = PreflopQuizSessionState(
				phase = if (questions.isEmpty()) QuizPhase.RESULT else QuizPhase.PLAYING,
				questions = questions,
			)
		}
	}

	fun onAnswer(answer: QuizAnswer) {
		_state.update { s ->
			val question = s.current ?: return@update s
			if (s.answered) return@update s
			responseTimes.add(questionMark.elapsedNow().inWholeMilliseconds)
			val correct = answer == question.correct
			val streak = if (correct) s.streak + 1 else 0
			s.copy(
				selected = answer,
				score = if (correct) s.score + 1 else s.score,
				streak = streak,
				bestStreak = maxOf(s.bestStreak, streak),
			)
		}
	}

	fun onNext() {
		val s = _state.value
		if (!s.answered) return
		advance(resetStreak = false)
	}

	fun onSkip() {
		if (_state.value.current == null) return
		advance(resetStreak = true)
	}

	fun onRetry() = start()

	/**
	 * 현재 문제의 정답 액션에 대한 LLM 해설을 요청한다. 정답은 차트에서 확정된 값이므로
	 * LLM 은 "왜 정답인지"만 설명한다. 응답이 늦게 도착해도 다른 문제로 넘어갔다면 무시한다.
	 */
	fun onRequestReview(languageName: String) {
		val s = _state.value
		val question = s.current ?: return
		val selected = s.selected ?: return
		if (s.reviewStatus == ReviewStatus.LOADING || s.reviewStatus == ReviewStatus.LOADED) return

		val requestedIndex = s.index
		_state.update { it.copy(reviewStatus = ReviewStatus.LOADING) }

		viewModelScope.launch {
			val spot = QuizReviewSpot(
				stackLabel = question.stack.label,
				heroLabel = question.hero.label,
				scenarioLabel = scenarioLabel(question.scenario, question.villain),
				handNotation = question.hand.notation,
				correctActionLabel = actionLabel(question.correct),
				userAnswerLabel = actionLabel(selected),
				isCorrect = selected == question.correct,
				languageName = languageName,
			)
			val result = reviewSpot(spot)
			_state.update { st ->
				if (st.index != requestedIndex) return@update st
				result.fold(
					onSuccess = { st.copy(reviewStatus = ReviewStatus.LOADED, reviewText = it) },
					onFailure = { st.copy(reviewStatus = ReviewStatus.ERROR) },
				)
			}
		}
	}

	private fun advance(resetStreak: Boolean) {
		val s = _state.value
		if (s.index + 1 >= s.total) {
			finish()
		} else {
			questionMark = TimeSource.Monotonic.markNow()
			_state.update {
				it.copy(
					index = it.index + 1,
					selected = null,
					streak = if (resetStreak) 0 else it.streak,
					reviewStatus = ReviewStatus.IDLE,
					reviewText = "",
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

	private fun actionLabel(answer: QuizAnswer): String = when (answer) {
		QuizAnswer.RAISE_VALUE -> "레이즈(밸류)"
		QuizAnswer.RAISE_BLUFF -> "레이즈(블러프)"
		QuizAnswer.CALL -> "콜"
		QuizAnswer.FOLD -> "폴드"
	}

	private fun finish() {
		val s = _state.value
		val avg = if (responseTimes.isEmpty()) 0L else responseTimes.average().toLong()
		val accuracy = if (s.total == 0) 0 else s.score * 100 / s.total
		val result = QuizResult(s.score, s.total, accuracy, avg, s.bestStreak)
		_state.update { it.copy(phase = QuizPhase.RESULT, result = result) }

		if (s.total > 0) {
			viewModelScope.launch {
				quizRecordRepository.save(
					QuizRecord(
						id = Uuid.random().toString(),
						quizType = quizType.name,
						score = s.score,
						total = s.total,
						avgResponseMs = avg,
						bestStreak = s.bestStreak,
						playedAt = Clock.System.now().toEpochMilliseconds(),
					),
				)
			}
		}
	}

	companion object {
		const val QUESTION_COUNT = 10
	}
}

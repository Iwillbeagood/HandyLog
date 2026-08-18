package com.hand.log.preflop.quiz.session

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hand.log.domain.model.Position
import com.hand.log.domain.model.Rank
import com.hand.log.domain.model.preflop.HandShape
import com.hand.log.domain.model.preflop.PreflopAction
import com.hand.log.domain.model.preflop.PreflopChart
import com.hand.log.domain.model.preflop.PreflopChartQuery
import com.hand.log.domain.model.preflop.PreflopHand
import com.hand.log.domain.model.preflop.PreflopScenario
import com.hand.log.domain.model.preflop.QuizRecord
import com.hand.log.domain.model.preflop.QuizRecordQuestion
import com.hand.log.domain.model.preflop.QuizReviewFocus
import com.hand.log.domain.model.preflop.QuizReviewSpot
import com.hand.log.platform.etc.Logger
import com.hand.log.preflop.quiz.common.PreflopQuizQuestion
import com.hand.log.domain.repository.QuizRecordRepository
import com.hand.log.domain.repository.AiReviewRepository
import com.hand.log.preflop.chart.data.PreflopChartRepository
import com.hand.log.preflop.quiz.common.PreflopQuizType
import com.hand.log.preflop.quiz.common.QuizQuestionGenerator
import com.hand.log.preflop.quiz.common.hasResponsePlan
import com.hand.log.preflop.quiz.common.primaryAction
import com.hand.log.preflop.quiz.session.contract.PreflopQuizSessionModalEffect
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
	recordId: String,
	private val chartRepository: PreflopChartRepository,
	private val generator: QuizQuestionGenerator,
	private val quizRecordRepository: QuizRecordRepository,
	private val aiReviewRepository: AiReviewRepository,
) : ViewModel() {

	private val quizType = runCatching { PreflopQuizType.valueOf(quizTypeName) }
		.getOrDefault(PreflopQuizType.RFI)

	private val responseTimes = mutableListOf<Long>()
	private var questionMark = TimeSource.Monotonic.markNow()

	private var loadedCharts: Map<PreflopChartQuery, PreflopChart> = emptyMap()

	private val _state = MutableStateFlow(PreflopQuizSessionState())
	val state: StateFlow<PreflopQuizSessionState> = _state

	private val _modalEffect =
		MutableStateFlow<PreflopQuizSessionModalEffect>(PreflopQuizSessionModalEffect.Idle)
	val modalEffect: StateFlow<PreflopQuizSessionModalEffect> get() = _modalEffect

	init {
		if (recordId.isEmpty()) start() else openRecord(recordId)
	}

	/** 저장된 퀴즈 기록을 결과 화면으로 바로 연다. 문제는 저장하지 않으므로 리뷰는 제공하지 않는다. */
	private fun openRecord(id: String) {
		_state.value = PreflopQuizSessionState(phase = QuizPhase.LOADING)
		viewModelScope.launch {
			val record = quizRecordRepository.findById(id)
			if (record == null) {
				start()
				return@launch
			}
			val accuracy = if (record.total == 0) 0 else record.score * 100 / record.total
			val recordType = runCatching { PreflopQuizType.valueOf(record.quizType) }
				.getOrDefault(quizType)
			// 저장된 문제 스냅샷을 복원해 결과 화면에서 리뷰를 다시 시작할 수 있게 한다.
			_state.value = PreflopQuizSessionState(
				phase = QuizPhase.RESULT,
				questions = record.questions.map { it.toQuizQuestion() },
				answers = record.questions.map { it.userAnswer },
				result = QuizResult(
					score = record.score,
					total = record.total,
					accuracyPct = accuracy,
					avgResponseMs = record.avgResponseMs,
					bestStreak = record.bestStreak,
					quizType = recordType,
					playedAt = record.playedAt,
				),
			)
		}
	}

	private fun start() {
		responseTimes.clear()
		_state.value = PreflopQuizSessionState(phase = QuizPhase.LOADING)
		viewModelScope.launch {
			val charts = chartRepository.load().charts
			loadedCharts = charts
			val questions = generator.generate(charts, quizType, QUESTION_COUNT)
			questionMark = TimeSource.Monotonic.markNow()
			_state.value = PreflopQuizSessionState(
				phase = if (questions.isEmpty()) QuizPhase.RESULT else QuizPhase.PLAYING,
				questions = questions,
			)
		}
	}

	/**
	 * 1차(첫 액션) 선택. 정답이 리레이즈 대응이 있는 복합 라인이고 그 첫 액션을 맞게 골랐을 때만
	 * 2차 선택을 기다린다. 그 외에는 곧바로 답으로 확정한다.
	 */
	fun onPrimarySelect(primary: PreflopAction) {
		val q = _state.value.current ?: return
		if (_state.value.phase != QuizPhase.PLAYING) return
		val needPlan = q.correct.hasResponsePlan() &&
			primary == q.correct.primaryAction() &&
			q.planOptions.isNotEmpty()
		if (needPlan) {
			_state.update { it.copy(pendingPrimary = primary) }
		} else {
			record(primary)
		}
	}

	fun onPlanSelect(fullAction: PreflopAction) = record(fullAction)

	fun onSkip() = record(null)

	/** 2차 선택 대기 중이면 1차 선택으로, 그 외에는 직전 문제로 돌아가 그 답을 지운다. */
	fun onPrevious() {
		val s = _state.value
		if (s.phase != QuizPhase.PLAYING) return
		if (s.pendingPrimary != null) {
			_state.update { it.copy(pendingPrimary = null) }
			return
		}
		if (s.index == 0) return
		responseTimes.removeAt(responseTimes.lastIndex)
		questionMark = TimeSource.Monotonic.markNow()
		_state.update {
			it.copy(index = it.index - 1, answers = it.answers.dropLast(1), pendingPrimary = null)
		}
	}

	fun onCloseRequest() {
		if (_state.value.phase == QuizPhase.PLAYING) {
			_modalEffect.update { PreflopQuizSessionModalEffect.ConfirmExit }
		}
	}

	fun dismissModal() {
		_modalEffect.update { PreflopQuizSessionModalEffect.Idle }
	}

	fun onRetry() = start()

	/** 정답 공개 없이 답변만 기록하고 곧바로 다음 문제로 넘어간다. 결과는 전 문제를 푼 뒤 공개한다. */
	private fun record(answer: PreflopAction?) {
		val s = _state.value
		if (s.phase != QuizPhase.PLAYING || s.current == null) return
		responseTimes.add(questionMark.elapsedNow().inWholeMilliseconds)
		val answers = s.answers + answer
		if (s.index + 1 >= s.total) {
			finish(answers)
		} else {
			questionMark = TimeSource.Monotonic.markNow()
			_state.update { it.copy(index = it.index + 1, answers = answers, pendingPrimary = null) }
		}
	}

	private fun finish(answers: List<PreflopAction?>) {
		val questions = _state.value.questions
		val total = questions.size
		val score = answers.indices.count { answers[it] == questions[it].correct }
		val bestStreak = bestStreak(answers, questions.map { it.correct })
		val avg = if (responseTimes.isEmpty()) 0L else responseTimes.average().toLong()
		val accuracy = if (total == 0) 0 else score * 100 / total
		val playedAt = Clock.System.now().toEpochMilliseconds()
		val result = QuizResult(score, total, accuracy, avg, bestStreak, quizType, playedAt)

		_state.update {
			it.copy(phase = QuizPhase.RESULT, answers = answers, result = result)
		}

		if (total > 0) {
			val recordQuestions = questions.mapIndexed { i, q -> q.toRecordQuestion(answers.getOrNull(i)) }
			viewModelScope.launch {
				quizRecordRepository.save(
					QuizRecord(
						id = Uuid.random().toString(),
						quizType = quizType.name,
						score = score,
						total = total,
						avgResponseMs = avg,
						bestStreak = bestStreak,
						playedAt = playedAt,
						questions = recordQuestions,
					),
				)
			}
		}
	}

	private fun bestStreak(answers: List<PreflopAction?>, correct: List<PreflopAction>): Int {
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

	fun onSelectReview(index: Int) {
		val s = _state.value
		if (index < 0 || index >= s.reviewTotal || index == s.reviewIndex) return
		_state.update { it.copy(reviewIndex = index, reviewStatus = ReviewStatus.IDLE, reviewText = "") }
	}

	/** 리뷰를 끝내고 결과 화면으로 돌아간다. */
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

		val focus = reviewFocus(question.correct, userAnswer)
		viewModelScope.launch {
			val spot = QuizReviewSpot(
				stackLabel = question.stack.label,
				heroLabel = question.hero.label,
				scenarioLabel = scenarioLabel(question.scenario, question.villain),
				handNotation = question.hand.notation,
				correctActionLabel = actionLabel(question.correct),
				userAnswerLabel = userAnswer?.let { actionLabel(it) } ?: "건너뜀",
				focus = focus,
				isOpen = question.scenario == PreflopScenario.RFI,
				languageName = languageName,
				primaryActionLabel = if (focus == QuizReviewFocus.RESPONSE_PLAN) {
					actionLabel(question.correct.primaryAction())
				} else {
					""
				},
				reraiseLabel = if (focus == QuizReviewFocus.RESPONSE_PLAN) {
					reraiseLabel(question.correct.primaryAction())
				} else {
					""
				},
				neighborHint = neighborHint(question),
			)
			val result = runCatching { aiReviewRepository.reviewQuizSpot(spot) }
				.mapCatching { it.ifBlank { error("empty review") } }
			result.onFailure { Logger.e("AI 퀴즈 리뷰 실패", it) }
			_state.update { st ->
				if (st.reviewIndex != requestedIndex) return@update st
				result.fold(
					onSuccess = { st.copy(reviewStatus = ReviewStatus.LOADED, reviewText = it) },
					onFailure = { st.copy(reviewStatus = ReviewStatus.ERROR) },
				)
			}
		}
	}

	/**
	 * 오답이 정답과 어디서 갈렸는지 판정한다. 첫 액션(오픈/3벳)은 맞고 리레이즈 대응만 다르면
	 * [QuizReviewFocus.RESPONSE_PLAN] — 해설이 대응 차이에만 집중하도록 한다.
	 */
	private fun reviewFocus(correct: PreflopAction, userAnswer: PreflopAction?): QuizReviewFocus = when {
		userAnswer == correct -> QuizReviewFocus.CORRECT
		userAnswer != null &&
			correct.hasResponsePlan() &&
			userAnswer.primaryAction() == correct.primaryAction() -> QuizReviewFocus.RESPONSE_PLAN
		else -> QuizReviewFocus.PRIMARY
	}

	// 첫 액션 기준 상대의 리레이즈 라벨: 오픈이면 3벳, 3벳이면 4벳.
	private fun reraiseLabel(primary: PreflopAction): String = when (primary) {
		PreflopAction.RAISE -> "3벳"
		PreflopAction.THREE_BET -> "4벳"
		else -> "리레이즈"
	}

	private fun neighborHint(question: PreflopQuizQuestion): String {
		val chart = loadedCharts[
			PreflopChartQuery(question.stack, question.scenario, question.hero, question.villain),
		] ?: return ""
		val hand = question.hand
		val lane = when (hand.shape) {
			HandShape.PAIR -> Rank.entries.map { PreflopHand(it, it, HandShape.PAIR) }
			else ->
				Rank.entries
					.filter { it.ordinal > hand.high.ordinal }
					.map { PreflopHand(hand.high, it, hand.shape) }
		}
		if (lane.size <= 1) return ""
		return lane.joinToString(", ") { h ->
			"${h.notation}:${chart.actionFor(h)?.let { actionLabel(it) } ?: "폴드"}"
		}
	}

	// LLM 프롬프트에 삽입할 스팟 설명 문자열(UI 표시가 아닌 프롬프트 데이터).
	private fun scenarioLabel(scenario: PreflopScenario, villain: Position?): String = when (scenario) {
		PreflopScenario.RFI -> "RFI — 앞 포지션이 모두 폴드, 내가 첫 오픈 여부를 결정하는 상황"
		PreflopScenario.FACING_RFI -> "앞 포지션 ${villain?.label ?: "상대"}의 오픈 레이즈에 대응하는 상황"
		PreflopScenario.VS_3BET -> "내 오픈에 ${villain?.label ?: "상대"}가 3벳한 상황"
		PreflopScenario.VS_LIMP -> "SB 가 림프한 상황에서 BB 로 대응하는 상황"
	}

	// LLM 프롬프트용 한국어 액션 라벨(로컬라이즈 불필요). 차트 액션과 1:1.
	private fun actionLabel(action: PreflopAction): String = when (action) {
		PreflopAction.RAISE -> "레이즈"
		PreflopAction.RAISE_BLUFF -> "레이즈 블러프"
		PreflopAction.RAISE_FOLD -> "레이즈/폴드"
		PreflopAction.RAISE_CALL -> "레이즈/콜"
		PreflopAction.RAISE_4BET -> "레이즈/4벳"
		PreflopAction.RAISE_JAM -> "레이즈 올인"
		PreflopAction.THREE_BET -> "3벳"
		PreflopAction.THREE_BET_BLUFF -> "3벳 블러프"
		PreflopAction.THREE_BET_STACKOFF -> "3벳/스택오프"
		PreflopAction.THREE_BET_FOLD -> "3벳/폴드"
		PreflopAction.THREE_BET_CALL -> "3벳/콜"
		PreflopAction.THREE_BET_JAM -> "3벳 올인"
		PreflopAction.FOUR_BET -> "4벳"
		PreflopAction.FOUR_BET_BLUFF -> "4벳 블러프"
		PreflopAction.ALL_IN -> "올인"
		PreflopAction.CALL -> "콜"
		PreflopAction.LIMP -> "림프"
		PreflopAction.CHECK -> "체크"
		PreflopAction.FOLD -> "폴드"
	}

	companion object {
		const val QUESTION_COUNT = 10
	}
}

private fun PreflopQuizQuestion.toRecordQuestion(userAnswer: PreflopAction?) = QuizRecordQuestion(
	stack = stack,
	scenario = scenario,
	hero = hero,
	villain = villain,
	hand = hand,
	correct = correct,
	options = options,
	userAnswer = userAnswer,
)

private fun QuizRecordQuestion.toQuizQuestion() = PreflopQuizQuestion(
	stack = stack,
	scenario = scenario,
	hero = hero,
	villain = villain,
	hand = hand,
	correct = correct,
	options = options,
)

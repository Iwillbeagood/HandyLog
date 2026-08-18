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
	/** 내 답이 정답과 어디서 갈렸는지 — 해설 초점을 결정한다. */
	val focus: QuizReviewFocus,
	/** 오픈(RFI) 스팟인지. 오픈이면 상대 레인지가 없어 코칭 근거가 달라진다. */
	val isOpen: Boolean,
	val languageName: String,
	/** [QuizReviewFocus.RESPONSE_PLAN] 일 때 정답과 공통인 첫 액션 라벨(예: "레이즈", "3벳"). 그 외엔 빈 문자열. */
	val primaryActionLabel: String = "",
	/** [QuizReviewFocus.RESPONSE_PLAN] 일 때 대응 대상 리레이즈 라벨(예: "3벳", "4벳"). 그 외엔 빈 문자열. */
	val reraiseLabel: String = "",
	/** 같은 하이카드 라인 주변 핸드의 실제 차트 액션(예: "98s:레이즈, 97s:폴드"). 경계 핸드 추측을 막는 근거. */
	val neighborHint: String = "",
)

/** 내 답이 정답과 어긋난 지점 — LLM 해설이 무엇에 집중해야 하는지를 정한다. */
enum class QuizReviewFocus {
	/** 완전 정답 — 왜 정답인지 설명. */
	CORRECT,

	/** 첫 액션(오픈/3벳)은 정답과 같고 리레이즈 대응(폴드/콜/잼 등)만 틀림 — 대응 차이만 설명. */
	RESPONSE_PLAN,

	/** 첫 액션부터 다르거나 건너뜀 — 액션 선택 자체를 설명. */
	PRIMARY,
}

package com.hand.log.data.datasoure.remote

import com.hand.log.domain.model.preflop.QuizReviewFocus
import com.hand.log.domain.model.preflop.QuizReviewSpot

/**
 * 프리플랍 퀴즈 해설용 프롬프트. 정답 액션은 차트에서 확정된 값이므로,
 * LLM 은 정답을 바꾸지 않고 "왜 그 액션이 정답인지"만 근거를 들어 설명한다.
 */
internal object HoldemPromptManager {

	val SYSTEM_PROMPT: String = buildString {
		append(
			"당신은 텍사스 홀덤 프리플랍 코치입니다. '정답 액션'은 차트에서 확정된 값이니 바꾸거나 반박하지 말고, 아래 '해설 지시'가 요구하는 초점만 근거를 들어 설명하세요.\n",
		)
		append("약한 핸드나 스몰 수딧 커넥터(예: 65s)를 '강하다'고 표현하지 마세요.\n")
		append(
			"장황하게 늘리지 말고 핵심만 간결하게: 전문 용어는 쉽게 풀되 최대 2문장 평문으로, 서론·반복·군더더기 없이, 요청된 언어로만, 마크다운·불릿·머리말 없이 답하세요.",
		)
	}

	fun buildUserPrompt(spot: QuizReviewSpot): String = buildString {
		appendLine("답변 언어: ${spot.languageName}")
		appendLine("스택: ${spot.stackLabel}")
		appendLine("내 포지션: ${spot.heroLabel}")
		appendLine("상황: ${spot.scenarioLabel}")
		appendLine("내 핸드: ${spot.handNotation}")
		appendLine("정답 액션: ${spot.correctActionLabel}")
		val verdict = if (spot.focus == QuizReviewFocus.CORRECT) "정답" else "오답"
		appendLine("내가 선택한 답: ${spot.userAnswerLabel} ($verdict)")
		if (spot.neighborHint.isNotBlank()) {
			appendLine("주변 핸드 액션(참고): ${spot.neighborHint}")
		}
		append("해설 지시: ")
		append(focusInstruction(spot))
		if (spot.neighborHint.isNotBlank()) {
			append(" ")
			append(boundaryInstruction(spot))
		}
	}

	// '주변 핸드 액션'(같은 레인의 실제 차트 액션)을 근거로 경계 핸드를 짚게 한다.
	// 대응만 틀린 경우엔 '내가 고른 액션'이 실제로 정답이 되는 경계를 알려 준다.
	private fun boundaryInstruction(spot: QuizReviewSpot): String = when (spot.focus) {
		QuizReviewFocus.RESPONSE_PLAN ->
			"'주변 핸드 액션'을 근거로, 내가 고른 '${spot.userAnswerLabel}'이 실제로 정답이 되는 경계 핸드가 어디부터인지" +
				" 구체적으로 짚어 주세요(예: '${spot.handNotation}보다 강한 ○○ 이상부터 ${spot.userAnswerLabel}, ${spot.handNotation}는 ${spot.correctActionLabel}')." +
				" 목록에 없는 핸드·액션은 추측하지 마세요."
		else ->
			"'주변 핸드 액션'의 실제 차트 액션을 근거로 경계 핸드를 '○○부터 (정답 액션)' 식으로 짚고, 목록에 없는 핸드·액션은 추측하지 마세요."
	}

	// 스팟에 실제로 해당하는 코칭 지시만 조립한다 — 무관한 규칙을 매 요청에 싣지 않는다.
	private fun focusInstruction(spot: QuizReviewSpot): String = when (spot.focus) {
		QuizReviewFocus.RESPONSE_PLAN ->
			"첫 액션(${spot.primaryActionLabel})은 정답과 같고, 상대의 ${spot.reraiseLabel} 대응만 틀렸습니다. " +
				"${spot.primaryActionLabel}이 왜 맞는지와 '에쿼티(승률)' 이야기는 하지 마세요(너무 당연한 내용). " +
				"내 스택(${spot.stackLabel}) 깊이를 기준으로, '${spot.userAnswerLabel}'과 정답 '${spot.correctActionLabel}'의 차이를 팟오즈·임플라이드 오즈·SPR 로만 설명하세요. " +
				responsePlanDirection(spot) +
				allInGuidance(spot)
		QuizReviewFocus.PRIMARY ->
			"내 답 '${spot.userAnswerLabel}'과 정답 '${spot.correctActionLabel}'은 첫 액션부터 다릅니다. 내 답이 어떤 핸드에 어울리는지 짚고, 이 핸드는 왜 그 액션이 아니라 정답 액션이 맞는지 대비해 설명하세요. " +
				rangeGuidance(spot.isOpen) + allInGuidance(spot)
		QuizReviewFocus.CORRECT ->
			"정답 액션이 왜 최선인지 설명하세요. " + rangeGuidance(spot.isOpen) + allInGuidance(spot)
	}

	// 정답이 폴드면 '과하게 방어함', 아니면 '너무 타이트하게 폴드함' — 방향에 맞는 근거만 싣는다.
	private fun responsePlanDirection(spot: QuizReviewSpot): String =
		if (spot.correctActionLabel.contains("폴드")) {
			"스택이 얕을수록 마진 핸드로 ${spot.reraiseLabel}에 콜하면 임플라이드 오즈가 부족해 손해라, 이 스택·핸드에선 폴드가 맞다고(예: '${spot.stackLabel}에선 ${spot.handNotation}로 콜하면 스택이 얕아 임플라이드 오즈가 안 맞는다') 짚어 주세요."
		} else {
			"이 핸드는 이 스택에서 ${spot.reraiseLabel}에 '${spot.correctActionLabel}'로 이어갈 만큼 충분해 폴드는 너무 타이트하다고 짚어 주세요."
		}

	// 오픈이면 상대 레인지가 없고, 대응이면 이미 액션한 상대 레인지와 비교한다 — 스팟에 맞는 한쪽만 싣는다.
	private fun rangeGuidance(isOpen: Boolean): String = if (isOpen) {
		"이 스팟은 오픈(RFI)이라 비교할 상대 레인지가 없으니 상대 레인지를 언급하지 말고, 뒤 포지션일수록 넓게 오픈하며 약한 핸드는 강해서가 아니라 스틸·포지션·플레이어빌리티 때문에 오픈함을 근거로 내 포지션 기준으로 설명하세요."
	} else {
		"이미 액션한 상대의 레인지와 비교해 설명하세요. 상대가 앞 포지션(UTG·MP)이면 좁고 강해 3벳은 프리미엄 밸류로 제한되고 나머지는 콜·폴드가 낫고, 뒤 포지션(CO·BTN·SB)이면 넓고 약해 약한 핸드로도 3벳(블러프)·콜이 성립합니다. 3벳은 밸류와 블러프를 구분하고 블러프 3벳은 폴드에쿼티·블로커로 설명하세요."
	}

	// 정답이 올인/잼일 때만 붙인다.
	private fun allInGuidance(spot: QuizReviewSpot): String = if (spot.correctActionLabel.contains(
			"올인",
		)
	) {
		" 정답이 올인/잼이라 포스트플랍이 없으니 플레이어빌리티는 언급하지 말고 폴드에쿼티·콜당했을 때의 올인 에쿼티·스택 깊이로 설명하세요."
	} else {
		""
	}
}

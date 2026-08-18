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
		append("전문 용어는 초보자도 이해하게 풀어 쓰고, 2~3문장 평문으로, 요청된 언어로만, 마크다운·불릿·머리말 없이 답하세요.")
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
			append(" '주변 핸드 액션'의 실제 차트 액션을 근거로 경계 핸드를 '○○부터 (정답 액션)' 식으로 짚고, 목록에 없는 핸드·액션은 추측하지 마세요.")
		}
	}

	// 스팟에 실제로 해당하는 코칭 지시만 조립한다 — 무관한 규칙을 매 요청에 싣지 않는다.
	private fun focusInstruction(spot: QuizReviewSpot): String = when (spot.focus) {
		QuizReviewFocus.RESPONSE_PLAN ->
			"첫 액션(${spot.primaryActionLabel})은 정답과 같고, 상대의 ${spot.reraiseLabel} 대응만 틀렸습니다. " +
				"${spot.primaryActionLabel}이 왜 맞는지는 설명하지 말고, 상대의 ${spot.reraiseLabel}이 들어왔을 때 이 핸드가 왜 '${spot.userAnswerLabel}'이 아니라 정답 '${spot.correctActionLabel}'의 대응이어야 하는지, 핸드 에쿼티·팟오즈·상대 ${spot.reraiseLabel} 레인지로 그 차이에만 집중해 설명하세요." +
				allInGuidance(spot)
		QuizReviewFocus.PRIMARY ->
			"내 답 '${spot.userAnswerLabel}'과 정답 '${spot.correctActionLabel}'은 첫 액션부터 다릅니다. 내 답이 어떤 핸드에 어울리는지 짚고, 이 핸드는 왜 그 액션이 아니라 정답 액션이 맞는지 대비해 설명하세요. " +
				rangeGuidance(spot.isOpen) + allInGuidance(spot)
		QuizReviewFocus.CORRECT ->
			"정답 액션이 왜 최선인지 설명하세요. " + rangeGuidance(spot.isOpen) + allInGuidance(spot)
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

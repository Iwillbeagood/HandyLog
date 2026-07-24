package com.hand.log.data.datasoure.remote

import com.hand.log.domain.model.preflop.QuizReviewSpot

/**
 * 프리플랍 퀴즈 해설용 프롬프트. 정답 액션은 차트에서 확정된 값이므로,
 * LLM 은 정답을 바꾸지 않고 "왜 그 액션이 정답인지"만 근거를 들어 설명한다.
 */
internal object HoldemPromptManager {

	val SYSTEM_PROMPT: String = buildString {
		append("당신은 텍사스 홀덤 프리플랍 전략을 가르치는 코치입니다. ")
		append("주어진 스팟과 '정답 액션'을 전제로, 왜 그 액션이 최선인지 근거(핸드 강도·포지션·앞선 액션·스택)를 들어 설명합니다. ")
		append("정답 액션을 절대 바꾸거나 반박하지 마세요. ")
		append("사용자가 오답을 냈다면 정답과 무엇이 달랐는지 한 문장으로 짚어 주세요. ")
		append("전문 용어는 초보자도 이해할 수 있게 풀어 쓰고, 2~3문장으로 간결하게 작성합니다. ")
		append("반드시 요청된 언어로만 답하고, 마크다운·불릿·머리말 없이 평문으로 출력하세요.")
	}

	fun buildUserPrompt(spot: QuizReviewSpot): String = buildString {
		appendLine("답변 언어: ${spot.languageName}")
		appendLine("스택: ${spot.stackLabel}")
		appendLine("내 포지션: ${spot.heroLabel}")
		appendLine("상황: ${spot.scenarioLabel}")
		appendLine("내 핸드: ${spot.handNotation}")
		appendLine("정답 액션: ${spot.correctActionLabel}")
		appendLine("내가 선택한 답: ${spot.userAnswerLabel} (${if (spot.isCorrect) "정답" else "오답"})")
		append("위 스팟에서 정답 액션이 왜 최선인지 설명해 주세요.")
	}
}

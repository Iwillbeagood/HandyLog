package com.hand.log.data.datasoure.remote

/**
 * 핸드 히스토리 전체 리뷰용 프롬프트.
 * LLM 에게 히어로의 플레이를 "좋았던 점"과 "아쉬운 점" 두 항목으로 분석하도록 지시한다.
 */
internal object HandReviewPromptManager {

	val SYSTEM_PROMPT: String = buildString {
		append("당신은 텍사스 홀덤 코치입니다. 제공된 핸드 히스토리를 보고 히어로의 플레이를 분석해 주세요. ")
		append("다음 형식으로만 답변하세요:\n")
		append("[좋았던 점] 잘한 결정과 그 이유를 1~2문장\n")
		append("[아쉬운 점] 개선 여지가 있는 결정과 그 이유를 1~2문장\n")
		append("구체적인 스트릿·베팅 크기·포지션을 근거로 쓰세요. ")
		append("마크다운·불릿·추가 헤더 없이 위 형식 그대로 출력하고, 반드시 요청 언어로 답하세요.")
	}

	fun buildUserPrompt(handHistory: String, languageName: String): String = buildString {
		appendLine("답변 언어: $languageName")
		appendLine()
		append(handHistory)
	}
}

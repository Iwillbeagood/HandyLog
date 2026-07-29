package com.hand.log.domain.repository

import com.hand.log.domain.model.preflop.QuizReviewSpot

/** OpenAI 를 사용하는 모든 AI 리뷰 요청을 하나의 인터페이스로 관리한다. */
interface AiReviewRepository {
	/** 프리플랍 퀴즈 스팟에 대한 LLM 해설을 반환한다. */
	suspend fun reviewQuizSpot(spot: QuizReviewSpot): String

	/** 핸드 히스토리 전문을 분석하고 플레이 리뷰를 반환한다. */
	suspend fun reviewHand(handHistory: String, languageName: String): String
}

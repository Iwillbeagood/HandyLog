package com.hand.log.domain.repository

import com.hand.log.domain.model.preflop.QuizReviewSpot

interface QuizReviewRepository {
	/** LLM 으로 해당 스팟의 해설 문장을 생성한다. */
	suspend fun review(spot: QuizReviewSpot): String
}

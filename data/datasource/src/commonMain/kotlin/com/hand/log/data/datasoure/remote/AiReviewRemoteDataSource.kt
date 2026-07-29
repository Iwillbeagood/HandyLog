package com.hand.log.data.datasoure.remote

import com.hand.log.domain.model.preflop.QuizReviewSpot

interface AiReviewRemoteDataSource {
	suspend fun reviewQuizSpot(spot: QuizReviewSpot): String
	suspend fun reviewHand(handHistory: String, languageName: String): String
}

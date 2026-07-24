package com.hand.log.data.datasoure.remote

import com.hand.log.domain.model.preflop.QuizReviewSpot

interface QuizReviewRemoteDataSource {
	suspend fun review(spot: QuizReviewSpot): String
}

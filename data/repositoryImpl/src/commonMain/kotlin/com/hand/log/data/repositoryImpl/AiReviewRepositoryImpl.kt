package com.hand.log.data.repositoryImpl

import com.hand.log.data.datasoure.remote.AiReviewRemoteDataSource
import com.hand.log.domain.model.preflop.QuizReviewSpot
import com.hand.log.domain.repository.AiReviewRepository

internal class AiReviewRepositoryImpl(
	private val aiReviewRemoteDataSource: AiReviewRemoteDataSource,
) : AiReviewRepository {

	override suspend fun reviewQuizSpot(spot: QuizReviewSpot): String =
		aiReviewRemoteDataSource.reviewQuizSpot(spot)

	override suspend fun reviewHand(handHistory: String, languageName: String): String =
		aiReviewRemoteDataSource.reviewHand(handHistory, languageName)
}

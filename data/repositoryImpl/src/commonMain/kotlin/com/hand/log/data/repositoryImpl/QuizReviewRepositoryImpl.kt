package com.hand.log.data.repositoryImpl

import com.hand.log.data.datasoure.remote.QuizReviewRemoteDataSource
import com.hand.log.domain.model.preflop.QuizReviewSpot
import com.hand.log.domain.repository.QuizReviewRepository

internal class QuizReviewRepositoryImpl(
	private val quizReviewRemoteDataSource: QuizReviewRemoteDataSource,
) : QuizReviewRepository {

	override suspend fun review(spot: QuizReviewSpot): String =
		quizReviewRemoteDataSource.review(spot)
}

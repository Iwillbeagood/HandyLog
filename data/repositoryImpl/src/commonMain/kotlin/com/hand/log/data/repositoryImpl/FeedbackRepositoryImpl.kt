package com.hand.log.data.repositoryImpl

import com.hand.log.data.datasoure.remote.FeedbackRemoteDataSource
import com.hand.log.domain.model.Feedback
import com.hand.log.domain.repository.FeedbackRepository

internal class FeedbackRepositoryImpl(
	private val feedbackRemoteDataSource: FeedbackRemoteDataSource,
) : FeedbackRepository {

	override suspend fun submit(feedback: Feedback) {
		feedbackRemoteDataSource.submit(feedback)
	}
}

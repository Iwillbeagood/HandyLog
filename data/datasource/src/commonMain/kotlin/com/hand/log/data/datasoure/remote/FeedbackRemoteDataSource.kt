package com.hand.log.data.datasoure.remote

import com.hand.log.domain.model.Feedback

interface FeedbackRemoteDataSource {
	suspend fun submit(feedback: Feedback)
}

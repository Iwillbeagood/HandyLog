package com.hand.log.domain.repository

import com.hand.log.domain.model.Feedback

interface FeedbackRepository {
	suspend fun submit(feedback: Feedback)
}

package com.hand.log.data.repositoryImpl

import com.hand.log.data.datasoure.remote.AiReviewRemoteDataSource
import com.hand.log.data.datasoure.remote.AiReviewUnavailableException
import com.hand.log.domain.model.preflop.QuizReviewSpot
import com.hand.log.domain.repository.AiReviewRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

internal class AiReviewRepositoryImpl(
	private val aiReviewRemoteDataSource: AiReviewRemoteDataSource,
) : AiReviewRepository {

	private val available = MutableStateFlow(true)

	override suspend fun reviewQuizSpot(spot: QuizReviewSpot): String =
		guarded { aiReviewRemoteDataSource.reviewQuizSpot(spot) }

	override suspend fun reviewHand(handHistory: String, languageName: String): String =
		guarded { aiReviewRemoteDataSource.reviewHand(handHistory, languageName) }

	private suspend fun guarded(block: suspend () -> String): String =
		try {
			block()
		} catch (e: AiReviewUnavailableException) {
			available.update { false }
			throw e
		}

	override fun observeAvailable(): Flow<Boolean> = available

	override suspend fun refreshAvailability() {
		val result = aiReviewRemoteDataSource.isAvailable()
		available.update { result }
	}
}

package com.hand.log.domain.repository

import com.hand.log.domain.model.Sample
import com.hand.log.domain.model.etc.error.MessageType
import kotlinx.coroutines.flow.Flow

interface SampleRepository {

	suspend fun upsertSample(
		sample: Sample,
		onError: (MessageType) -> Unit,
		onSuccess: () -> Unit,
	)

	fun observeSamples(): Flow<List<Sample>>

	suspend fun getSamples(): List<Sample>

	suspend fun getSample(id: Long): Sample

	suspend fun deleteSample(id: Long)
}

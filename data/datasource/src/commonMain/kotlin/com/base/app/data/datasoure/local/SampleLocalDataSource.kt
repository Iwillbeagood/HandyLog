package com.hand.log.data.datasoure.local

import com.hand.log.domain.model.Sample
import kotlinx.coroutines.flow.Flow

interface SampleLocalDataSource {
	suspend fun upsertSample(sample: Sample)
	fun observeSamples(): Flow<List<Sample>>
	suspend fun getSamples(): List<Sample>
	suspend fun getSample(id: Long): Sample
	suspend fun deleteSample(id: Long)
}

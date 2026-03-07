package com.hand.log.data.repositoryImpl

import com.hand.log.data.datasoure.local.SampleLocalDataSource
import com.hand.log.data.datasoure.remote.SampleRemoteDataSource
import com.hand.log.domain.model.Sample
import com.hand.log.domain.model.etc.error.MessageType
import com.hand.log.domain.repository.SampleRepository
import com.hand.log.utils.etc.Logger
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

internal class SampleRepositoryImpl @Inject constructor(
	private val sampleRemoteDataSource: SampleRemoteDataSource,
	private val sampleLocalDataSource: SampleLocalDataSource,
) : SampleRepository {

	override suspend fun upsertSample(
		sample: Sample,
		onError: (MessageType) -> Unit,
		onSuccess: () -> Unit,
	) {
		try {
			sampleLocalDataSource.upsertSample(sample)
			onSuccess()
		} catch (e: Exception) {
			Logger.e("upsertSample error: $e")
		}
	}

	override fun observeSamples(): Flow<List<Sample>> {
		return sampleLocalDataSource.observeSamples()
	}

	override suspend fun getSamples(): List<Sample> {
		return sampleLocalDataSource.getSamples()
	}

	override suspend fun getSample(id: Long): Sample {
		return sampleLocalDataSource.getSample(id)
	}

	override suspend fun deleteSample(id: Long) {
		try {
			sampleLocalDataSource.deleteSample(id)
		} catch (e: Exception) {
			Logger.e("deleteById error: $e")
		}
	}
}

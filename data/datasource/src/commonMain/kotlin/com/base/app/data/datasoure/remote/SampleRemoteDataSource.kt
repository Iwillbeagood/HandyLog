package com.hand.log.data.datasoure.remote

import com.hand.log.domain.model.Sample
import com.hand.log.domain.model.etc.error.MessageType
import kotlinx.coroutines.flow.Flow

interface SampleRemoteDataSource {
	fun getSampleData(
		onError: (MessageType) -> Unit,
	): Flow<Sample>
}

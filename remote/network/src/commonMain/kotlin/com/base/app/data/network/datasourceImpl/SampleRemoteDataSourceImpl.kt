package com.hand.log.data.network.datasourceImpl

import com.hand.log.data.network.mapper.toSample
import com.hand.log.data.network.service.SampleApi
import com.hand.log.data.network.utils.apiResult.suspendOnFailureWithErrorHandling
import com.hand.log.data.network.utils.apiResult.suspendOnSuccess
import com.hand.log.data.datasoure.remote.SampleRemoteDataSource
import com.hand.log.domain.model.Sample
import com.hand.log.domain.model.etc.error.MessageType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

internal class SampleRemoteDataSourceImpl @Inject constructor(
	private val api: SampleApi,
) : SampleRemoteDataSource {

	override fun getSampleData(onError: (MessageType) -> Unit): Flow<Sample> = flow {
		api.getSampleData()
			.suspendOnFailureWithErrorHandling(onError)
			.suspendOnSuccess {
				emit(response.toSample())
			}
	}

}

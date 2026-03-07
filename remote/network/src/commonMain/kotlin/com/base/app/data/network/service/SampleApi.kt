package com.hand.log.data.network.service

import com.hand.log.data.network.model.SampleData
import com.hand.log.data.network.utils.apiResult.ApiResult
import retrofit2.http.GET

internal interface SampleApi {

	@GET("/sample")
	suspend fun getSampleData(): ApiResult<SampleData>
}

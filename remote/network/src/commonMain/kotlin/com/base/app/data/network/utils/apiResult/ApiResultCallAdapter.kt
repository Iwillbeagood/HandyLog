package com.hand.log.data.network.utils.apiResult

import kotlinx.coroutines.CoroutineScope
import retrofit2.Call
import retrofit2.CallAdapter
import java.lang.reflect.Type

internal class ApiResultCallAdapter<R>(
	private val successType: Type,
	private val coroutineScope: CoroutineScope,
) : CallAdapter<R, Call<ApiResult<R>>> {
	override fun adapt(call: Call<R>): Call<ApiResult<R>> = ApiResultCall(call, coroutineScope)

	override fun responseType(): Type = successType
}

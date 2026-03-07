package com.hand.log.data.network.utils.apiResult

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import retrofit2.Call
import retrofit2.CallAdapter
import retrofit2.Retrofit
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

class ResultCallAdapterFactory(
	private val coroutineScope: CoroutineScope,
) : CallAdapter.Factory() {

	override fun get(
		returnType: Type,
		annotations: Array<out Annotation>,
		retrofit: Retrofit,
	): CallAdapter<*, *>? {
		when (getRawType(returnType)) {
			Call::class.java -> {
				val callType = getParameterUpperBound(0, returnType as ParameterizedType)
				val rawType = getRawType(callType)
				if (rawType != ApiResult::class.java) {
					return null
				}

				val resultType = getParameterUpperBound(0, callType as ParameterizedType)
				return ApiResultCallAdapter<Any>(resultType, coroutineScope)
			}
			else -> return null
		}
	}

	companion object {
		@JvmStatic
		fun create(
			coroutineScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO),
		): ResultCallAdapterFactory = ResultCallAdapterFactory(coroutineScope)
	}
}

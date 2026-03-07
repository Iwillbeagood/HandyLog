package com.hand.log.data.network.utils.apiResult

import okhttp3.Request
import okio.Timeout
import retrofit2.Call
import retrofit2.Callback

internal abstract class CallDelegate<TIn, TOut>(
	protected val proxy: Call<TIn>,
) : Call<TOut> {
	final override fun enqueue(callback: Callback<TOut>) = enqueueImpl(callback)

	override fun cancel() = proxy.cancel()
	override fun request(): Request = proxy.request()
	override fun isExecuted() = proxy.isExecuted
	override fun isCanceled() = proxy.isCanceled
	override fun timeout(): Timeout = null ?: proxy.timeout()

	abstract fun enqueueImpl(callback: Callback<TOut>)
}

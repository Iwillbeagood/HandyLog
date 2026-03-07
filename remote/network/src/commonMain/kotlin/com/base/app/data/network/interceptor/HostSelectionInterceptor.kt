package com.hand.log.data.network.interceptor

import com.hand.log.common.Constants
import com.hand.log.domain.repository.TokenRepository
import com.hand.log.utils.etc.Logger
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import java.net.URL

internal class HostSelectionInterceptor(
	private val tokenRepository: TokenRepository,
) : Interceptor {
	override fun intercept(chain: Interceptor.Chain): Response {
		val token = runBlocking { tokenRepository.getToken() }
		var lastException: IOException? = null

		for (hostString in Constants.HOST_URLS) {
			try {
				val hostUrl = URL(hostString)
				val request = chain.request()

				val newUrl = request.url.newBuilder()
					.scheme(hostUrl.protocol)
					.host(hostUrl.host)
					.port(if (hostUrl.port != -1) hostUrl.port else request.url.port)
					.build()

				val newRequest = request.newBuilder()
					.url(newUrl)
					.header("Content-Type", "application/json; charset=utf-8")
					.header("HmmAuth", token)
					.build()
				Logger.i("Attempting request with host: ${newUrl.host}")

				return chain.proceed(newRequest)
			} catch (e: IOException) {
				lastException = e
				Logger.e("Request failed for host $hostString. Trying next.")
			}
		}

		throw lastException ?: IOException("All hosts failed to respond.")
	}
}

package com.hand.log.data.network.di

import com.hand.log.common.Constants
import com.hand.log.data.network.interceptor.HostSelectionInterceptor
import com.hand.log.data.network.utils.HttpNetworkLogger
import com.hand.log.data.network.utils.apiResult.ResultCallAdapterFactory
import com.hand.log.domain.repository.TokenRepository
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Converter
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object NetworkModule {

	@Provides
	@Singleton
	fun provideOkhttpClient(
		tokenRepository: TokenRepository,
	): OkHttpClient =
		OkHttpClient
			.Builder()
			.addInterceptor(HostSelectionInterceptor(tokenRepository))
			.addInterceptor(HttpNetworkLogger())
			.build()

	@Provides
	@Singleton
	fun provideJson(): Json = Json {
		ignoreUnknownKeys = true
		coerceInputValues = true
		encodeDefaults = true
	}

	@Provides
	@Singleton
	fun provideConverterFactory(json: Json): Converter.Factory =
		json.asConverterFactory("application/json".toMediaType())

	@Provides
	@Singleton
	fun provideKTCRetrofit(
		okHttpClient: OkHttpClient,
		converterFactory: Converter.Factory,
	): Retrofit =
		Retrofit.Builder()
			.baseUrl(Constants.BASE_URI)
			.addCallAdapterFactory(ResultCallAdapterFactory.create())
			.addConverterFactory(converterFactory)
			.client(okHttpClient)
			.build()
}

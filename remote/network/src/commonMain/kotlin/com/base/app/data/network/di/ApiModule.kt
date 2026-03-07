package com.hand.log.data.network.di

import com.hand.log.data.network.service.SampleApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object ApiModule {

	@Provides
	@Singleton
	fun provideSampleApi(
		retrofit: Retrofit,
	): SampleApi = retrofit.create(SampleApi::class.java)
}

package com.hand.log.data.datasoure.di

import com.hand.log.data.datasoure.remote.FeedbackRemoteDataSource
import com.hand.log.data.datasoure.remote.FeedbackRemoteDataSourceImpl
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val networkModule = module {
	single {
		HttpClient {
			expectSuccess = true
			install(ContentNegotiation) {
				json(
					Json {
						ignoreUnknownKeys = true
						isLenient = true
						encodeDefaults = true
					},
				)
			}
			install(Logging) {
				level = LogLevel.INFO
			}
		}
	}
}

val remoteDataSourceModule = module {
	singleOf(::FeedbackRemoteDataSourceImpl) bind FeedbackRemoteDataSource::class
}

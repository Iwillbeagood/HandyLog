package com.hand.log.data.repositoryImpl.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import com.hand.log.data.repositoryImpl.SampleRepositoryImpl

@InstallIn(SingletonComponent::class)
@Module
internal abstract class RepositoryModule {

	@Binds
	abstract fun bindsSampleRepository(
		repository: SampleRepositoryImpl,
	): com.hand.log.domain.repository.SampleRepository
}

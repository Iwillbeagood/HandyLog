package com.hand.log.data.network.di

import com.hand.log.data.network.datasourceImpl.SampleRemoteDataSourceImpl
import com.hand.log.data.datasoure.remote.SampleRemoteDataSource
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class RemoteDataSourceModule {

    @Binds
    @Singleton
    abstract fun bindSampleDataSource(
        impl: SampleRemoteDataSourceImpl
    ): SampleRemoteDataSource
}


package com.hand.log.data.repositoryImpl.di

import com.hand.log.data.repositoryImpl.HandRecordRepositoryImpl
import com.hand.log.data.repositoryImpl.PokerTableRepositoryImpl
import com.hand.log.data.repositoryImpl.AppSettingsRepositoryImpl
import com.hand.log.data.repositoryImpl.SavedPlayerRepositoryImpl
import com.hand.log.domain.repository.AppSettingsRepository
import com.hand.log.domain.repository.HandRecordRepository
import com.hand.log.domain.repository.PokerTableRepository
import com.hand.log.domain.repository.SavedPlayerRepository
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val repositoryModule = module {
	singleOf(::PokerTableRepositoryImpl) bind PokerTableRepository::class
	singleOf(::HandRecordRepositoryImpl) bind HandRecordRepository::class
	singleOf(::SavedPlayerRepositoryImpl) bind SavedPlayerRepository::class
	singleOf(::AppSettingsRepositoryImpl) bind AppSettingsRepository::class
}

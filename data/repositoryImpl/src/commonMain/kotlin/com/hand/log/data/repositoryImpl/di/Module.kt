package com.hand.log.data.repositoryImpl.di

import com.hand.log.data.repositoryImpl.HandRecordRepositoryImpl
import com.hand.log.data.repositoryImpl.PokerTableRepositoryImpl
import com.hand.log.domain.repository.HandRecordRepository
import com.hand.log.domain.repository.PokerTableRepository
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val repositoryModule = module {
	singleOf(::PokerTableRepositoryImpl) bind PokerTableRepository::class
	singleOf(::HandRecordRepositoryImpl) bind HandRecordRepository::class
}

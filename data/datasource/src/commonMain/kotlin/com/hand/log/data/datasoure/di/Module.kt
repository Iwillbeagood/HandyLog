package com.hand.log.data.datasoure.di

import com.hand.log.data.datasoure.local.HandRecordLocalDataSource
import com.hand.log.data.datasoure.local.HandRecordLocalDataSourceImpl
import com.hand.log.data.datasoure.local.PokerTableLocalDataSource
import com.hand.log.data.datasoure.local.PokerTableLocalDataSourceImpl
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val dataSourceModule = module {
	singleOf(::PokerTableLocalDataSourceImpl) bind PokerTableLocalDataSource::class
	singleOf(::HandRecordLocalDataSourceImpl) bind HandRecordLocalDataSource::class
}

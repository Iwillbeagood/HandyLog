package com.hand.log.database.di

import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.hand.log.data.datasoure.local.HandRecordLocalDataSource
import com.hand.log.data.datasoure.local.PokerTableLocalDataSource
import com.hand.log.data.datasoure.local.SavedPlayerLocalDataSource
import com.hand.log.database.AppDatabase
import com.hand.log.database.impl.HandRecordLocalDataSourceImpl
import com.hand.log.database.impl.PokerTableLocalDataSourceImpl
import com.hand.log.database.impl.SavedPlayerLocalDataSourceImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val databaseModule = module {
	single<AppDatabase> {
		getDatabaseBuilder()
			.setDriver(BundledSQLiteDriver())
			.setQueryCoroutineContext(Dispatchers.IO)
			.fallbackToDestructiveMigration(true)
			.build()
	}
	single { get<AppDatabase>().pokerTableDao() }
	single { get<AppDatabase>().handRecordDao() }
	single { get<AppDatabase>().savedPlayerDao() }
}

val databaseDataSourceModule = module {
	singleOf(::PokerTableLocalDataSourceImpl) bind PokerTableLocalDataSource::class
	singleOf(::HandRecordLocalDataSourceImpl) bind HandRecordLocalDataSource::class
	singleOf(::SavedPlayerLocalDataSourceImpl) bind SavedPlayerLocalDataSource::class
}

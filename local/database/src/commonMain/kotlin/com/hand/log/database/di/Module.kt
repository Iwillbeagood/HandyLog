package com.hand.log.database.di

import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.hand.log.database.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
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
}

package com.hand.log.database.di

import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.hand.log.data.datasoure.local.HandRecordLocalDataSource
import com.hand.log.data.datasoure.local.PokerTableLocalDataSource
import com.hand.log.data.datasoure.local.QuizRecordLocalDataSource
import com.hand.log.data.datasoure.local.SavedPlayerLocalDataSource
import com.hand.log.database.AppDatabase
import com.hand.log.database.impl.HandRecordLocalDataSourceImpl
import com.hand.log.database.impl.PokerTableLocalDataSourceImpl
import com.hand.log.database.impl.QuizRecordLocalDataSourceImpl
import com.hand.log.database.impl.SavedPlayerLocalDataSourceImpl
import com.hand.log.database.migration.MIGRATION_2_3
import com.hand.log.database.migration.MIGRATION_3_4
import com.hand.log.database.migration.MIGRATION_4_5
import com.hand.log.database.migration.MIGRATION_5_6
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
			.addMigrations(MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6)
			.fallbackToDestructiveMigrationFrom(dropAllTables = true, 1)
			.build()
	}
	single { get<AppDatabase>().pokerTableDao() }
	single { get<AppDatabase>().handRecordDao() }
	single { get<AppDatabase>().savedPlayerDao() }
	single { get<AppDatabase>().quizRecordDao() }
}

val databaseDataSourceModule = module {
	singleOf(::PokerTableLocalDataSourceImpl) bind PokerTableLocalDataSource::class
	singleOf(::HandRecordLocalDataSourceImpl) bind HandRecordLocalDataSource::class
	singleOf(::SavedPlayerLocalDataSourceImpl) bind SavedPlayerLocalDataSource::class
	singleOf(::QuizRecordLocalDataSourceImpl) bind QuizRecordLocalDataSource::class
}

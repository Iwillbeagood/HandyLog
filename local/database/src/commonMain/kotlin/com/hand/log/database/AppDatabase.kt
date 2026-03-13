package com.hand.log.database

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.room.TypeConverters
import com.hand.log.database.converter.Converters
import com.hand.log.database.dao.HandRecordDao
import com.hand.log.database.dao.PokerTableDao
import com.hand.log.database.entity.HandRecordEntity
import com.hand.log.database.entity.HandStreetActionEntity
import com.hand.log.database.entity.HandStreetCardEntity
import com.hand.log.database.entity.PokerTableEntity
import com.hand.log.database.entity.TablePlayerEntity

@Database(
	entities = [
		PokerTableEntity::class,
		TablePlayerEntity::class,
		HandRecordEntity::class,
		HandStreetCardEntity::class,
		HandStreetActionEntity::class,
	],
	version = 1,
)
@TypeConverters(Converters::class)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {
	abstract fun pokerTableDao(): PokerTableDao
	abstract fun handRecordDao(): HandRecordDao
}

expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
	override fun initialize(): AppDatabase
}

internal const val DB_FILE_NAME = "hand.db"

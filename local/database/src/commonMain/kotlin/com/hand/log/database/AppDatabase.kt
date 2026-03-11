package com.hand.log.database

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import com.hand.log.database.dao.HandHistoryDao
import com.hand.log.database.entity.HandActionEntity
import com.hand.log.database.entity.HandCommunityCardEntity
import com.hand.log.database.entity.HandEntity
import com.hand.log.database.entity.HandPlayerEntity

@Database(
	entities = [
		HandEntity::class,
		HandPlayerEntity::class,
		HandActionEntity::class,
		HandCommunityCardEntity::class,
	],
	version = 1,
)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {
	abstract fun handHistoryDao(): HandHistoryDao
}

expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
	override fun initialize(): AppDatabase
}

internal const val DB_FILE_NAME = "hand.db"

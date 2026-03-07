package com.hand.log.database

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.room.TypeConverters
import com.hand.log.database.converter.Converters
import com.hand.log.database.dao.HandHistoryDao
import com.hand.log.database.entity.ActionEntity
import com.hand.log.database.entity.HandHistoryEntity
import com.hand.log.database.entity.PlayerHandEntity

@Database(
    entities = [
        HandHistoryEntity::class,
        PlayerHandEntity::class,
        ActionEntity::class,
    ],
    version = 1,
)
@ConstructedBy(AppDatabaseConstructor::class)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
	abstract fun handHistoryDao(): HandHistoryDao
}

expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
	override fun initialize(): AppDatabase
}

internal const val DB_FILE_NAME = "hand.db"

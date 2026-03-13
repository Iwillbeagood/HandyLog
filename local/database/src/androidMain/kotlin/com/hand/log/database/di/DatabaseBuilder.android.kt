package com.hand.log.database.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import com.hand.log.database.AppDatabase
import com.hand.log.database.DB_FILE_NAME
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

actual fun getDatabaseBuilder(): RoomDatabase.Builder<AppDatabase> {
	val context: Context = DatabaseKoinHelper.get()
	val dbFile = context.applicationContext.getDatabasePath(DB_FILE_NAME)
	return Room.databaseBuilder<AppDatabase>(
		context = context.applicationContext,
		name = dbFile.absolutePath,
	)
}

internal object DatabaseKoinHelper : KoinComponent

package com.hand.log.database.di

import androidx.room.Room
import androidx.room.RoomDatabase
import com.hand.log.database.AppDatabase
import com.hand.log.database.DB_FILE_NAME
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask

actual fun getDatabaseBuilder(): RoomDatabase.Builder<AppDatabase> {
	val dbFilePath = "${documentDirectory()}/$DB_FILE_NAME"
	return Room.databaseBuilder<AppDatabase>(
		name = dbFilePath,
	)
}

@OptIn(ExperimentalForeignApi::class)
private fun documentDirectory(): String {
	val documentDirectory: NSURL? = NSFileManager.defaultManager.URLForDirectory(
		directory = NSDocumentDirectory,
		inDomain = NSUserDomainMask,
		appropriateForURL = null,
		create = false,
		error = null,
	)
	return requireNotNull(documentDirectory).path!!
}

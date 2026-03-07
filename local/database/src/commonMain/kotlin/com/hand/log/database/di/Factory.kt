package com.hand.log.database.di

import com.hand.log.database.AppDatabase

expect class Factory {
	fun createRoomDatabase(): AppDatabase
}

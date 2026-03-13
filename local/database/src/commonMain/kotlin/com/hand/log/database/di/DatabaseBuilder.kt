package com.hand.log.database.di

import androidx.room.RoomDatabase
import com.hand.log.database.AppDatabase

expect fun getDatabaseBuilder(): RoomDatabase.Builder<AppDatabase>

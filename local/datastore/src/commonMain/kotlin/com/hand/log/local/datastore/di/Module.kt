package com.hand.log.local.datastore.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.hand.log.local.datastore.AppSettingsDataSource
import org.koin.dsl.module

val dataStoreModule = module {
	single<DataStore<Preferences>> { createPreferencesDataStore() }
	single { AppSettingsDataSource(get()) }
}

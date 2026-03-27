package com.hand.log.local.datastore.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.hand.log.data.datasoure.local.AppSettingsLocalDataSource
import com.hand.log.local.datastore.AppSettingsDataSource
import com.hand.log.local.datastore.impl.AppSettingsLocalDataSourceImpl
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val dataStoreModule = module {
	single<DataStore<Preferences>> { createPreferencesDataStore() }
	single { AppSettingsDataSource(get()) }
}

val dataStoreDataSourceModule = module {
	singleOf(::AppSettingsLocalDataSourceImpl) bind AppSettingsLocalDataSource::class
}

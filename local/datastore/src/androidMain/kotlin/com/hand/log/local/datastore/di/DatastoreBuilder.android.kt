package com.hand.log.local.datastore.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences

private lateinit var applicationContext: Context

fun initPreferencesDataStore(appContext: Context) {
	applicationContext = appContext
}

fun getPreferencesDataStorePath(appContext: Context): String =
	appContext.filesDir.resolve(dataStoreFileName).absolutePath

actual fun createPreferencesDataStore(): DataStore<Preferences> {
	val path = getPreferencesDataStorePath(applicationContext)
	return createDataStore(path)
}

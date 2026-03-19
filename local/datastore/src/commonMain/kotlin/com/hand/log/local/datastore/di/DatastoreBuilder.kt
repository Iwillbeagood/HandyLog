package com.hand.log.local.datastore.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import okio.Path.Companion.toPath

fun createDataStore(path: String): DataStore<Preferences> =
	PreferenceDataStoreFactory.createWithPath(
		produceFile = { path.toPath() },
	)

internal const val dataStoreFileName = "sample.preferences_pb"

expect fun createPreferencesDataStore(): DataStore<Preferences>

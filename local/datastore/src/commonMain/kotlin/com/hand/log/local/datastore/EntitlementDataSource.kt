package com.hand.log.local.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Pro 해금 엔타이틀먼트의 로컬 캐시. 스토어 조회 결과를 저장해 오프라인에서도 Pro 상태를 유지한다.
 */
class EntitlementDataSource(
	private val dataStore: DataStore<Preferences>,
) {
	private companion object {
		val PRO_ENTITLED = booleanPreferencesKey("pro_entitled")
	}

	fun observeProEntitled(): Flow<Boolean> = dataStore.data.map { prefs ->
		prefs[PRO_ENTITLED] ?: false
	}

	suspend fun setProEntitled(entitled: Boolean) {
		dataStore.edit { prefs ->
			prefs[PRO_ENTITLED] = entitled
		}
	}
}

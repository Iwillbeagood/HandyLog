package com.hand.log.local.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AppSettingsDataSource(
	private val dataStore: DataStore<Preferences>,
) {
	private companion object {
		val THEME_MODE = stringPreferencesKey("theme_mode")
		val BET_SIZE_PRESETS = stringPreferencesKey("bet_size_presets")
		val POT_PERCENT_PRESETS = stringPreferencesKey("pot_percent_presets")
		val SKIP_STEP_BACK_WARNING = booleanPreferencesKey("skip_step_back_warning")
		val HOME_TAB = stringPreferencesKey("home_tab")
	}

	fun observeThemeMode(): Flow<String> = dataStore.data.map { prefs ->
		prefs[THEME_MODE] ?: "auto"
	}

	fun observeBetSizePresets(): Flow<List<Double>> = dataStore.data.map { prefs ->
		val raw = prefs[BET_SIZE_PRESETS] ?: "2.0,2.5,3.0,4.0,5.0"
		raw.split(",").mapNotNull { it.toDoubleOrNull() }
	}

	suspend fun setThemeMode(mode: String) {
		dataStore.edit { prefs ->
			prefs[THEME_MODE] = mode
		}
	}

	suspend fun setBetSizePresets(presets: List<Double>) {
		dataStore.edit { prefs ->
			prefs[BET_SIZE_PRESETS] = presets.joinToString(",")
		}
	}

	fun observePotPercentPresets(): Flow<List<Int>> = dataStore.data.map { prefs ->
		val raw = prefs[POT_PERCENT_PRESETS] ?: "33,50,75,100"
		raw.split(",").mapNotNull { it.toIntOrNull() }
	}

	suspend fun setPotPercentPresets(presets: List<Int>) {
		dataStore.edit { prefs ->
			prefs[POT_PERCENT_PRESETS] = presets.joinToString(",")
		}
	}

	fun observeSkipStepBackWarning(): Flow<Boolean> = dataStore.data.map { prefs ->
		prefs[SKIP_STEP_BACK_WARNING] ?: false
	}

	suspend fun setSkipStepBackWarning(skip: Boolean) {
		dataStore.edit { prefs ->
			prefs[SKIP_STEP_BACK_WARNING] = skip
		}
	}

	fun observeHomeTab(): Flow<String> = dataStore.data.map { prefs ->
		prefs[HOME_TAB] ?: "table"
	}

	suspend fun setHomeTab(tab: String) {
		dataStore.edit { prefs ->
			prefs[HOME_TAB] = tab
		}
	}
}

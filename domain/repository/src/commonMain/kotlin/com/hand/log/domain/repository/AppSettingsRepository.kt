package com.hand.log.domain.repository

import com.hand.log.domain.model.ThemeMode
import kotlinx.coroutines.flow.Flow

interface AppSettingsRepository {
	fun observeThemeMode(): Flow<ThemeMode>
	fun observeBetSizePresets(): Flow<List<Double>>
	fun observePotPercentPresets(): Flow<List<Int>>
	fun observeSkipStepBackWarning(): Flow<Boolean>
	suspend fun setThemeMode(mode: ThemeMode)
	suspend fun setBetSizePresets(presets: List<Double>)
	suspend fun setPotPercentPresets(presets: List<Int>)
	suspend fun setSkipStepBackWarning(skip: Boolean)

	companion object {
		const val MAX_PRESETS = 4
	}
}

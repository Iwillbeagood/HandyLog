package com.hand.log.data.datasoure.local

import kotlinx.coroutines.flow.Flow

interface AppSettingsLocalDataSource {
	fun observeThemeMode(): Flow<String>
	fun observeBetSizePresets(): Flow<List<Double>>
	fun observePotPercentPresets(): Flow<List<Int>>
	suspend fun setThemeMode(mode: String)
	suspend fun setBetSizePresets(presets: List<Double>)
	suspend fun setPotPercentPresets(presets: List<Int>)
}

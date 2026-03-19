package com.hand.log.data.datasoure.local

import com.hand.log.local.datastore.AppSettingsDataSource
import kotlinx.coroutines.flow.Flow

internal class AppSettingsLocalDataSourceImpl(
	private val appSettingsDataSource: AppSettingsDataSource,
) : AppSettingsLocalDataSource {

	override fun observeThemeMode(): Flow<String> =
		appSettingsDataSource.observeThemeMode()

	override fun observeBetSizePresets(): Flow<List<Double>> =
		appSettingsDataSource.observeBetSizePresets()

	override suspend fun setThemeMode(mode: String) =
		appSettingsDataSource.setThemeMode(mode)

	override suspend fun setBetSizePresets(presets: List<Double>) =
		appSettingsDataSource.setBetSizePresets(presets)

	override fun observePotPercentPresets(): Flow<List<Int>> =
		appSettingsDataSource.observePotPercentPresets()

	override suspend fun setPotPercentPresets(presets: List<Int>) =
		appSettingsDataSource.setPotPercentPresets(presets)
}

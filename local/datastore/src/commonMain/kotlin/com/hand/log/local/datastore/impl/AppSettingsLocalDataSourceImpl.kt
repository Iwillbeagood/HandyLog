package com.hand.log.local.datastore.impl

import com.hand.log.data.datasoure.local.AppSettingsLocalDataSource
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

	override fun observeSkipStepBackWarning(): Flow<Boolean> =
		appSettingsDataSource.observeSkipStepBackWarning()

	override suspend fun setSkipStepBackWarning(skip: Boolean) =
		appSettingsDataSource.setSkipStepBackWarning(skip)

	override fun observeHomeTab(): Flow<String> =
		appSettingsDataSource.observeHomeTab()

	override suspend fun setHomeTab(tab: String) =
		appSettingsDataSource.setHomeTab(tab)
}

package com.hand.log.data.repositoryImpl

import com.hand.log.data.datasoure.local.AppSettingsLocalDataSource
import com.hand.log.domain.model.ThemeMode
import com.hand.log.domain.model.etc.HomeTab
import com.hand.log.domain.repository.AppSettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class AppSettingsRepositoryImpl(
	private val localDataSource: AppSettingsLocalDataSource,
) : AppSettingsRepository {

	override fun observeThemeMode(): Flow<ThemeMode> =
		localDataSource.observeThemeMode().map { str ->
			ThemeMode.entries.find { it.name.lowercase() == str } ?: ThemeMode.AUTO
		}

	override fun observeBetSizePresets(): Flow<List<Double>> =
		localDataSource.observeBetSizePresets().map { it.take(AppSettingsRepository.MAX_PRESETS) }

	override suspend fun setThemeMode(mode: ThemeMode) =
		localDataSource.setThemeMode(mode.name.lowercase())

	override suspend fun setBetSizePresets(presets: List<Double>) =
		localDataSource.setBetSizePresets(presets.take(AppSettingsRepository.MAX_PRESETS))

	override fun observePotPercentPresets(): Flow<List<Int>> =
		localDataSource.observePotPercentPresets().map { it.take(AppSettingsRepository.MAX_PRESETS) }

	override suspend fun setPotPercentPresets(presets: List<Int>) =
		localDataSource.setPotPercentPresets(presets.take(AppSettingsRepository.MAX_PRESETS))

	override fun observeSkipStepBackWarning(): Flow<Boolean> =
		localDataSource.observeSkipStepBackWarning()

	override suspend fun setSkipStepBackWarning(skip: Boolean) =
		localDataSource.setSkipStepBackWarning(skip)

	override fun observeHomeTab(): Flow<HomeTab> =
		localDataSource.observeHomeTab().map { str ->
			HomeTab.entries.find { it.name.lowercase() == str } ?: HomeTab.TABLE
		}

	override suspend fun setHomeTab(tab: HomeTab) =
		localDataSource.setHomeTab(tab.name.lowercase())
}

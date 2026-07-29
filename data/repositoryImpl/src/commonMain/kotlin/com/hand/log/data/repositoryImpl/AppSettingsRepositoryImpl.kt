package com.hand.log.data.repositoryImpl

import com.hand.log.data.datasoure.local.AppSettingsLocalDataSource
import com.hand.log.domain.model.Position
import com.hand.log.domain.model.ThemeMode
import com.hand.log.domain.model.etc.HomeTab
import com.hand.log.domain.model.preflop.PreflopScenario
import com.hand.log.domain.model.preflop.PreflopSelection
import com.hand.log.domain.model.preflop.PreflopStack
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

	override fun observePreflopSelection(): Flow<PreflopSelection> =
		localDataSource.observePreflopSelection().map { raw -> parsePreflopSelection(raw) }

	override suspend fun setPreflopSelection(selection: PreflopSelection) =
		localDataSource.setPreflopSelection(
			listOf(
				selection.stack.name,
				selection.scenario.name,
				selection.hero.name,
				selection.villain?.name ?: "",
			).joinToString(SELECTION_DELIMITER),
		)

	private fun parsePreflopSelection(raw: String): PreflopSelection {
		val parts = raw.split(SELECTION_DELIMITER)
		val default = PreflopSelection.DEFAULT
		return PreflopSelection(
			stack = parts.getOrNull(0)?.let { v ->
				PreflopStack.entries.find {
					it.name == v
				}
			} ?: default.stack,
			scenario = parts.getOrNull(1)?.let { v -> PreflopScenario.entries.find { it.name == v } }
				?: default.scenario,
			hero = parts.getOrNull(2)?.let { v -> Position.entries.find { it.name == v } } ?: default.hero,
			villain = parts.getOrNull(3)?.let { v -> Position.entries.find { it.name == v } },
		)
	}

	private companion object {
		const val SELECTION_DELIMITER = "|"
	}
}

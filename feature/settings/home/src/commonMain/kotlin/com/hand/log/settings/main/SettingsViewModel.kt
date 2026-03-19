package com.hand.log.settings.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hand.log.domain.model.ThemeMode
import com.hand.log.domain.repository.AppSettingsRepository
import com.hand.log.settings.main.contract.AppSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

internal class SettingsViewModel(
	private val appSettingsRepository: AppSettingsRepository,
) : ViewModel() {

	private val _settings = MutableStateFlow(AppSettings())
	val settings: StateFlow<AppSettings> get() = _settings

	init {
		combine(
			appSettingsRepository.observeThemeMode(),
			appSettingsRepository.observeBetSizePresets(),
		) { themeMode, presets ->
			AppSettings(themeMode = themeMode, betSizePresets = presets)
		}
			.onEach { _settings.value = it }
			.launchIn(viewModelScope)
	}

	fun updateTheme(mode: ThemeMode) {
		viewModelScope.launch {
			appSettingsRepository.setThemeMode(mode)
		}
	}

	fun addBetPreset(value: Double) {
		val current = _settings.value
		if (value <= 0 || value in current.betSizePresets) return
		val updated = (current.betSizePresets + value).sorted()
		viewModelScope.launch {
			appSettingsRepository.setBetSizePresets(updated)
		}
	}

	fun removeBetPreset(value: Double) {
		val current = _settings.value
		if (current.betSizePresets.size <= 1) return
		val updated = current.betSizePresets.filter { it != value }
		viewModelScope.launch {
			appSettingsRepository.setBetSizePresets(updated)
		}
	}
}

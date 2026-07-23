package com.hand.log.settings.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hand.log.domain.model.ProFeature
import com.hand.log.domain.model.ThemeMode
import com.hand.log.domain.repository.AppSettingsRepository
import com.hand.log.domain.usecase.ObserveProStatusUseCase
import com.hand.log.settings.main.contract.AppSettings
import com.hand.log.settings.main.contract.SettingsModalEffect
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class SettingsViewModel(
	private val appSettingsRepository: AppSettingsRepository,
	observeProStatus: ObserveProStatusUseCase,
) : ViewModel() {

	private val _settings = MutableStateFlow(AppSettings())
	val settings: StateFlow<AppSettings> get() = _settings

	private val _modalEffect = MutableStateFlow<SettingsModalEffect>(SettingsModalEffect.Idle)
	val modalEffect: StateFlow<SettingsModalEffect> get() = _modalEffect

	val isPro: StateFlow<Boolean> = observeProStatus()
		.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

	init {
		viewModelScope.launch {
			combine(
				appSettingsRepository.observeThemeMode(),
				appSettingsRepository.observeBetSizePresets(),
			) { themeMode, presets ->
				AppSettings(themeMode = themeMode, betSizePresets = presets)
			}.collect { settings -> _settings.update { settings } }
		}
	}

	fun updateTheme(mode: ThemeMode) {
		viewModelScope.launch {
			appSettingsRepository.setThemeMode(mode)
		}
	}

	fun canNavigateToBetSize(): Boolean = isPro.value

	fun showPresetsPaywall() {
		_modalEffect.update { SettingsModalEffect.ShowPaywall(ProFeature.CUSTOM_PRESETS) }
	}

	fun dismissModal() {
		_modalEffect.update { SettingsModalEffect.Idle }
	}
}

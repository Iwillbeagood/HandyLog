package com.hand.log.settings.betsize

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hand.log.domain.repository.AppSettingsRepository
import com.hand.log.settings.betsize.contract.BetSizeState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

internal class BetSizeViewModel(
	private val appSettingsRepository: AppSettingsRepository,
) : ViewModel() {

	val state: StateFlow<BetSizeState> = combine(
		appSettingsRepository.observeBetSizePresets(),
		appSettingsRepository.observePotPercentPresets(),
	) { preflop, postflop ->
		BetSizeState(
			preflopPresets = preflop,
			postflopPresets = postflop,
			canAddPreflop = preflop.size < AppSettingsRepository.MAX_PRESETS,
			canAddPostflop = postflop.size < AppSettingsRepository.MAX_PRESETS,
		)
	}.stateIn(
		scope = viewModelScope,
		started = SharingStarted.WhileSubscribed(5_000),
		initialValue = BetSizeState(),
	)

	fun addPreflopPreset(value: Double) {
		val current = state.value.preflopPresets
		if (value <= 0 || value in current) return
		viewModelScope.launch {
			appSettingsRepository.setBetSizePresets((current + value).sorted())
		}
	}

	fun removePreflopPreset(value: Double) {
		val current = state.value.preflopPresets
		if (current.size <= 1) return
		viewModelScope.launch {
			appSettingsRepository.setBetSizePresets(current.filter { it != value })
		}
	}

	fun addPostflopPreset(value: Int) {
		val current = state.value.postflopPresets
		if (value <= 0 || value in current) return
		viewModelScope.launch {
			appSettingsRepository.setPotPercentPresets((current + value).sorted())
		}
	}

	fun removePostflopPreset(value: Int) {
		val current = state.value.postflopPresets
		if (current.size <= 1) return
		viewModelScope.launch {
			appSettingsRepository.setPotPercentPresets(current.filter { it != value })
		}
	}
}

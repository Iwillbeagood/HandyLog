package com.hand.log.settings.main.contract

import androidx.compose.runtime.Immutable
import com.hand.log.domain.model.ProFeature

@Immutable
internal sealed interface SettingsModalEffect {
	data object Idle : SettingsModalEffect
	data class ShowPaywall(val feature: ProFeature) : SettingsModalEffect
}

package com.hand.log.playersetup.contract

import androidx.compose.runtime.Immutable

@Immutable
sealed interface PlayerSetupEffect {
	data class SaveComplete(val isEditMode: Boolean) : PlayerSetupEffect
	data object DeleteComplete : PlayerSetupEffect
	data object NameRequired : PlayerSetupEffect
}

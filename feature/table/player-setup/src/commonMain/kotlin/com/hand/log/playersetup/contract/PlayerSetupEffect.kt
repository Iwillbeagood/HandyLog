package com.hand.log.playersetup.contract

import androidx.compose.runtime.Immutable

@Immutable
sealed interface PlayerSetupEffect {
	data object SaveComplete : PlayerSetupEffect
	data object NameRequired : PlayerSetupEffect
}

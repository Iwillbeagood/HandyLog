package com.hand.log.playersetup.contract

import androidx.compose.runtime.Immutable
import com.hand.log.domain.model.Player

@Immutable
sealed interface PlayerSetupEffect {
	data class SaveComplete(val players: List<Player>) : PlayerSetupEffect
}

package com.hand.log.playersetup.contract

import androidx.compose.runtime.Immutable
import com.hand.log.domain.model.Player
import com.hand.log.domain.model.PlayerTendency

@Immutable
data class PlayerSetupState(
	val tableId: String = "",
	val player: Player = Player(seat = 0),
	val isHero: Boolean = false,
	val occupiedSeats: Set<Int> = emptySet(),
	val saveToMarking: Boolean = false,
) {
	val playerName: String get() = player.name ?: ""
	val selectedTendency: PlayerTendency? get() = player.tendency
	val playerMemo: String get() = player.memo ?: ""
}

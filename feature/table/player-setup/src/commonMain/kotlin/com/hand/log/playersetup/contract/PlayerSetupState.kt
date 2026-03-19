package com.hand.log.playersetup.contract

import androidx.compose.runtime.Immutable
import com.hand.log.domain.model.Player
import com.hand.log.domain.model.PlayerTendency

@Immutable
data class PlayerSetupState(
	val initialSeat: Int = 0,
	val isHero: Boolean = false,
	val startingStack: Double = 0.0,
	val editingPlayers: List<Player> = emptyList(),
	val saveToMarking: Boolean = false,
) {
	val currentPlayer: Player
		get() = editingPlayers.find { it.seat == initialSeat }
			?: Player(seat = initialSeat, stack = 0.0)

	val playerName: String get() = currentPlayer.name ?: ""
	val playerStack: String get() = if (currentPlayer.stack > 0) currentPlayer.stack.toLong().toString() else ""
	val selectedTendency: PlayerTendency? get() = currentPlayer.tendency
	val playerMemo: String get() = currentPlayer.memo ?: ""

	companion object {
		fun from(
			initialSeat: Int,
			isHero: Boolean,
			startingStack: Double,
			players: List<Player>,
		): PlayerSetupState = PlayerSetupState(
			initialSeat = initialSeat,
			isHero = isHero,
			startingStack = startingStack,
			editingPlayers = players,
		)
	}
}

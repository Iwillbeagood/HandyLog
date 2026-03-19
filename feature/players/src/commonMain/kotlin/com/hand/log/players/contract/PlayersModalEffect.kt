package com.hand.log.players.contract

import androidx.compose.runtime.Immutable
import com.hand.log.domain.model.SavedPlayer

@Immutable
internal sealed interface PlayersModalEffect {
	data object Idle : PlayersModalEffect
	data class ShowPlayerEdit(val player: SavedPlayer) : PlayersModalEffect
	data object ShowAddPlayer : PlayersModalEffect
}

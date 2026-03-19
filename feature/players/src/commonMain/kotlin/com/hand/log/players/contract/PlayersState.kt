package com.hand.log.players.contract

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import com.hand.log.domain.model.SavedPlayer

@Stable
internal sealed interface PlayersState {

	@Immutable
	data object Loading : PlayersState

	@Immutable
	data class Success(
		val players: List<SavedPlayer>,
	) : PlayersState
}

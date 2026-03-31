package com.hand.log.table.contract

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import com.hand.log.domain.model.Player

@Stable
internal sealed interface TableModalEffect {

	@Immutable
	data object Idle : TableModalEffect

	@Immutable
	data class ShowPlayerSetup(
		val tableId: String,
		val initialSeat: Int,
		val player: Player?,
		val occupiedSeats: Set<Int>,
		val maxPlayers: Int,
	) : TableModalEffect

	@Immutable
	data class ShowTableEdit(
		val table: com.hand.log.domain.model.PokerTable,
	) : TableModalEffect

	@Immutable
	data object ShowDeleteConfirm : TableModalEffect
}

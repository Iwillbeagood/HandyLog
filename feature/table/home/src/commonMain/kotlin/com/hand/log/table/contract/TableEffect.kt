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
		val initialSeat: Int,
		val isHero: Boolean,
		val startingStack: Double,
		val players: List<Player>,
	) : TableModalEffect

	@Immutable
	data class ShowTableEdit(
		val table: com.hand.log.domain.model.PokerTable,
	) : TableModalEffect

	@Immutable
	data object ShowDeleteConfirm : TableModalEffect
}

@Stable
internal sealed interface TableEffect {

	@Immutable
	data object PlayerSaved : TableEffect

	@Immutable
	data object HandDeleted : TableEffect

	@Immutable
	data object TableUpdated : TableEffect

	@Immutable
	data object TableDeleted : TableEffect

	@Immutable
	data class NavigateToRecordHand(val tableId: String) : TableEffect
}

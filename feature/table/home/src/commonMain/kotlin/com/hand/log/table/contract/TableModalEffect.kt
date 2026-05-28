package com.hand.log.table.contract

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import com.hand.log.domain.model.Player
import com.hand.log.domain.model.PokerTable
import com.hand.log.domain.model.ProFeature

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
	data class ShowHeroSeatSwap(
		val maxPlayers: Int,
		val heroSeat: Int,
	) : TableModalEffect

	@Immutable
	data class ShowTableEdit(
		val table: PokerTable,
	) : TableModalEffect

	@Immutable
	data object ShowDeleteConfirm : TableModalEffect

	@Immutable
	data class ShowTableBalance(
		val table: PokerTable,
	) : TableModalEffect

	@Immutable
	data class ShowPlayerPositionSetup(
		val tableId: String,
		val maxPlayers: Int,
		val heroSeat: Int,
		val playerCount: Int = 0,
	) : TableModalEffect

	@Immutable
	data class ShowPaywall(val feature: ProFeature) : TableModalEffect
}

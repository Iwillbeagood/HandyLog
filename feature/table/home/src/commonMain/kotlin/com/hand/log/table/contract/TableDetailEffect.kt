package com.hand.log.table.contract

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import com.hand.log.domain.model.Player

@Stable
internal sealed interface TableDetailModalEffect {

	@Immutable
	data object Idle : TableDetailModalEffect

	@Immutable
	data class ShowPlayerSetup(
		val initialSeat: Int,
		val isHero: Boolean,
		val startingStack: Double,
		val players: List<Player>,
	) : TableDetailModalEffect

	@Immutable
	data class ShowTableEdit(
		val table: com.hand.log.domain.model.PokerTable,
	) : TableDetailModalEffect
}

@Stable
internal sealed interface TableDetailEffect {

	@Immutable
	data class ShowSnackBar(val message: String) : TableDetailEffect
}

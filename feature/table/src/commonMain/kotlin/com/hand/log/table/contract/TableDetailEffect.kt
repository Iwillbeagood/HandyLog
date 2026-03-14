package com.hand.log.table.contract

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable

@Stable
internal sealed interface TableDetailModalEffect {

	@Immutable
	data object Idle : TableDetailModalEffect

	@Immutable
	data class ShowPlayerSetup(val initialSeat: Int) : TableDetailModalEffect

	@Immutable
	data object ShowTableEdit : TableDetailModalEffect
}

@Stable
internal sealed interface TableDetailEffect {

	@Immutable
	data class ShowSnackBar(val message: String) : TableDetailEffect
}

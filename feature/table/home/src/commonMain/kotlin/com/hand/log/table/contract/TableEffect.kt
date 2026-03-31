package com.hand.log.table.contract

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable

@Stable
internal sealed interface TableEffect {

	@Immutable
	data object PlayerAdded : TableEffect

	@Immutable
	data object PlayerUpdated : TableEffect

	@Immutable
	data object PlayerDeleted : TableEffect

	@Immutable
	data object HandDeleted : TableEffect

	@Immutable
	data object TableCreated : TableEffect

	@Immutable
	data object TableUpdated : TableEffect

	@Immutable
	data object TableDeleted : TableEffect

	@Immutable
	data class NavigateToRecordHand(val tableId: String) : TableEffect
}

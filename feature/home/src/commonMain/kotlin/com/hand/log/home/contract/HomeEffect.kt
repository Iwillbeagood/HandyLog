package com.hand.log.home.contract

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable

@Stable
internal sealed interface HomeModalEffect {

	@Immutable
	data object Idle : HomeModalEffect

	@Immutable
	data object TableEditSheet : HomeModalEffect
}

@Stable
internal sealed interface HomeEffect {

	@Immutable
	data object TableDeleted : HomeEffect

	@Immutable
	data class NavigateToTable(val tableId: String) : HomeEffect
}

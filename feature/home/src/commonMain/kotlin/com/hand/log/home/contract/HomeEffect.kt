package com.hand.log.home.contract

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable

@Stable
internal sealed interface HomeModalEffect {

	@Immutable
	data object Idle : HomeModalEffect
}

@Stable
internal sealed interface HomeEffect {

	@Immutable
	data class ShowSnackBar(val message: String) : HomeEffect

	@Immutable
	data class NavigateToTable(val tableId: String) : HomeEffect
}

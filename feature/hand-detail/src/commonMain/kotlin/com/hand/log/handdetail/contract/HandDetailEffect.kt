package com.hand.log.handdetail.contract

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable

@Stable
internal sealed interface HandDetailEffect {

	@Immutable
	data object HandDeleted : HandDetailEffect

	@Immutable
	data class ShareText(val text: String) : HandDetailEffect

	@Immutable
	data class ShareImage(val fileName: String) : HandDetailEffect

	@Immutable
	data class DownloadImage(val fileName: String) : HandDetailEffect

	@Immutable
	data class NavigateToPlayers(val tableId: String, val seat: Int) : HandDetailEffect
}

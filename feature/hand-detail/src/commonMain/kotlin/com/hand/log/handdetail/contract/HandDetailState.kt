package com.hand.log.handdetail.contract

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import com.hand.log.domain.model.HandRecord

@Stable
internal sealed interface HandDetailState {

	@Immutable
	data object Loading : HandDetailState

	@Immutable
	data class Success(
		val hand: HandRecord,
		val useBbUnit: Boolean = false,
	) : HandDetailState

	@Immutable
	data object Error : HandDetailState
}

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

@Stable
internal sealed interface HandDetailModalEffect {

	@Immutable
	data object Idle : HandDetailModalEffect

	@Immutable
	data object ConfirmDelete : HandDetailModalEffect

	@Immutable
	data class ShowPlayerMark(val seat: Int) : HandDetailModalEffect
}

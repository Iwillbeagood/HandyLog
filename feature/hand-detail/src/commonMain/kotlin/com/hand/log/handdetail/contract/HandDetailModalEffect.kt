package com.hand.log.handdetail.contract

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import com.hand.log.domain.model.Card

@Stable
internal sealed interface HandDetailModalEffect {

	@Immutable
	data object Idle : HandDetailModalEffect

	@Immutable
	data object ConfirmDelete : HandDetailModalEffect

	@Immutable
	data class ShowPlayerMark(val seat: Int) : HandDetailModalEffect

	@Immutable
	data class EditMemo(val currentMemo: String) : HandDetailModalEffect

	@Immutable
	data class EditHeroHand(val selectedCards: Set<Card>) : HandDetailModalEffect

	@Immutable
	data class EditShowdownHand(
		val seat: Int,
		val positionName: String,
		val selectedCards: Set<Card>,
	) : HandDetailModalEffect
}

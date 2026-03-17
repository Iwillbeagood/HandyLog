package com.hand.log.record.contract

import androidx.compose.runtime.Immutable
import com.hand.log.domain.model.Card

@Immutable
internal sealed interface RecordHandModalEffect {
	data object Idle : RecordHandModalEffect
	data class ShowCardSelector(
		val target: CardSelectorTarget,
		val selectedCards: Set<Card>,
	) : RecordHandModalEffect
}

@Immutable
internal sealed interface RecordHandEffect {
	data object SaveSuccess : RecordHandEffect
	data class SaveError(val message: String) : RecordHandEffect
}

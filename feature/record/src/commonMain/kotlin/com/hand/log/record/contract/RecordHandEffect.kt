package com.hand.log.record.contract

import androidx.compose.runtime.Immutable
import com.hand.log.domain.model.Card
import com.hand.log.domain.model.PokerTable

@Immutable
internal sealed interface RecordHandModalEffect {
	data object Idle : RecordHandModalEffect
	data class ShowCardSelector(
		val title: String,
		val target: CardSelectorTarget,
		val selectedCards: Set<Card>,
	) : RecordHandModalEffect
	data class ShowTableEdit(val table: PokerTable) : RecordHandModalEffect
}

@Immutable
internal sealed interface RecordHandEffect {
	data object SaveSuccess : RecordHandEffect
	data class SaveError(val message: String) : RecordHandEffect
}

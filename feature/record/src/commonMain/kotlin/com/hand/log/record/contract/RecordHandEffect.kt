package com.hand.log.record.contract

import androidx.compose.runtime.Immutable
import com.hand.log.domain.model.Card
import com.hand.log.domain.model.PocketCards
import com.hand.log.domain.model.PokerTable

@Immutable
internal sealed interface RecordHandModalEffect {
	data object Idle : RecordHandModalEffect
	data class ShowCardSelector(
		val target: CardSelectorTarget,
		val selectedCards: Set<Card>,
		val allowUnknown: Boolean = true,
		val heroHand: PocketCards? = null,
	) : RecordHandModalEffect
	data class ShowTableEdit(val table: PokerTable) : RecordHandModalEffect
	data class ConfirmStepBack(val targetStep: RecordStep) : RecordHandModalEffect
}

@Immutable
internal sealed interface RecordHandEffect {
	data object SaveSuccess : RecordHandEffect
	data object SaveError : RecordHandEffect
	data object FocusHeroStack : RecordHandEffect
}

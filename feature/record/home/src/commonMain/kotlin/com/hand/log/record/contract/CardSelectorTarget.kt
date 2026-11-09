package com.hand.log.record.contract

import androidx.compose.runtime.Immutable
import com.hand.log.domain.model.Street

@Immutable
internal sealed interface CardSelectorTarget {
	val maxCards: Int
	data class HeroCard(override val maxCards: Int = 2) : CardSelectorTarget
	data class AllBoardCards(override val maxCards: Int = 5) : CardSelectorTarget
	data class BoardCard(val street: Street, override val maxCards: Int) : CardSelectorTarget
	data class SingleBoardCard(val street: Street, val cardIndex: Int, override val maxCards: Int = 1) : CardSelectorTarget
	data class ShowdownCard(val seat: Int, val positionName: String, override val maxCards: Int = 2) : CardSelectorTarget
}

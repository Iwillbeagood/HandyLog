package com.hand.log.domain.model.quick

import com.hand.log.domain.model.ActionType
import com.hand.log.domain.model.Blinds
import com.hand.log.domain.model.Card
import com.hand.log.domain.model.Player
import com.hand.log.domain.model.Position
import com.hand.log.domain.model.Rank
import com.hand.log.domain.model.Suit

sealed interface QuickInput {
	data class Seat(val position: Position) : QuickInput
	data class Act(val type: ActionType) : QuickInput
	data class Digit(val value: Int) : QuickInput
	data object Dot : QuickInput
	data class PotSize(val size: BetSize) : QuickInput
	data class CardRank(val rank: Rank) : QuickInput
	data class CardSuit(val suit: Suit) : QuickInput
	data class Hero(val position: Position) : QuickInput
	data object NextLine : QuickInput
	data object CloseStreet : QuickInput
	data object ToggleAmountUnit : QuickInput
	data class FillCards(val owner: CardOwner, val cards: List<Card>) : QuickInput
}

enum class BetSize(val ratio: Double, val raiseMultiple: Double) {
	THIRD(1.0 / 3, 2.0),
	HALF(0.5, 2.5),
	TWO_THIRDS(2.0 / 3, 3.0),
	POT(1.0, 4.0),
}

enum class QuickPhase {
	ACTION,
	HERO_CARDS,
	BOARD,
	REVIEW,
}

sealed interface CardOwner {
	data object Hero : CardOwner

	data object Board : CardOwner

	data class Opponent(val position: Position) : CardOwner
}

data class QuickShowdownHand(
	val seat: Int,
	val position: Position,
	val cards: List<Card>,
)

data class QuickFinish(
	val heroPosition: Position?,
	val canSelectHeroPosition: Boolean,
	val heroCards: List<Card>,
	val board: List<Card>,
	val showdown: List<QuickShowdownHand>,
	val usedCards: Set<Card>,
)

sealed interface QuickPending {
	data class HeroCards(
		val position: Position,
		val cards: List<Card>,
		val rank: Rank?,
	) : QuickPending

	data class Actor(val seat: Int, val isChosen: Boolean) : QuickPending

	data class Amount(
		val seat: Int,
		val type: ActionType?,
		val betLevel: Int,
		val text: String,
		val potSize: BetSize? = null,
	) : QuickPending

	data class PlayerCards(
		val seat: Int,
		val cards: List<Card>,
		val rank: Rank?,
	) : QuickPending

	data class Staged(
		val seat: Int,
		val type: ActionType,
		val amount: Double?,
	) : QuickPending

	data class BoardCard(val cards: List<Card>, val rank: Rank?) : QuickPending
}

data class QuickHandConfig(
	val tableId: String?,
	val heroSeat: Int,
	val seats: List<Int>,
	val blinds: Blinds,
	val tablePlayers: List<Player> = emptyList(),
	val stackInBb: Double? = null,
	val startInChips: Boolean = false,
) {
	companion object {
		const val DEFAULT_PLAYER_COUNT = 7
		const val HERO_SEAT_WITHOUT_TABLE = 1
		val BB_UNIT_BLINDS = Blinds(sb = 0.5, bb = 1.0)
	}
}

package com.hand.log.record.quick.model

import androidx.compose.runtime.Immutable
import com.hand.log.domain.model.ActionType
import com.hand.log.domain.model.Card
import com.hand.log.domain.model.Position
import com.hand.log.domain.model.Rank
import com.hand.log.domain.model.Suit
import com.hand.log.domain.model.quick.BetSize
import com.hand.log.domain.model.quick.QuickInput
import com.hand.log.ui.poker.HandHistoryFormatter

@Immutable
internal sealed interface HandKey {
	data class Pos(val position: Position) : HandKey
	data class Act(val type: ActionType) : HandKey
	data class Sized(val size: BetSize) : HandKey
	data class Face(val rank: Rank) : HandKey
	data class Num(val value: Int) : HandKey
	data class SuitKey(val suit: Suit) : HandKey
	data object Dot : HandKey
	data object AmountUnit : HandKey
	data object NextLine : HandKey
	data object Backspace : HandKey
	data object CardPicker : HandKey
}

internal object HandKeyboardLayout {

	val actionRows: List<List<HandKey>> = listOf(
		listOf(ActionType.FOLD, ActionType.CHECK),
		listOf(ActionType.CALL, ActionType.BET),
		listOf(ActionType.RAISE, ActionType.ALL_IN),
	).map { row -> row.map { HandKey.Act(it) } } +
		BetSize.entries.chunked(2).map { row -> row.map { HandKey.Sized(it) } } +
		listOf(listOf(HandKey.AmountUnit))

	val padRows: List<List<HandKey>> = listOf(
		listOf(Rank.ACE, Rank.KING, Rank.QUEEN, Rank.JACK, Rank.TEN).map { HandKey.Face(it) },
		listOf(HandKey.Num(7), HandKey.Num(8), HandKey.Num(9), HandKey.SuitKey(Suit.SPADES)),
		listOf(HandKey.Num(4), HandKey.Num(5), HandKey.Num(6), HandKey.SuitKey(Suit.HEARTS)),
		listOf(HandKey.Num(1), HandKey.Num(2), HandKey.Num(3), HandKey.SuitKey(Suit.DIAMONDS)),
		listOf(HandKey.Num(0), HandKey.Dot, HandKey.SuitKey(Suit.CLUBS)),
		listOf(HandKey.CardPicker, HandKey.NextLine, HandKey.Backspace),
	)

	val fixedKeys: List<HandKey> = (actionRows + padRows).flatten()

	val potSizeKeys: List<HandKey> = BetSize.entries.map { HandKey.Sized(it) }

	val cardKeys: List<HandKey> = fixedKeys.filter {
		it is HandKey.Face || it is HandKey.SuitKey || (it is HandKey.Num && it.value in numberRanks)
	}

	val lineKeys: List<HandKey> = fixedKeys.filter {
		it is HandKey.Act || it is HandKey.Face || it is HandKey.Num || it is HandKey.SuitKey || it == HandKey.Dot
	}

	fun positionRows(positions: List<Position>): List<List<HandKey>> =
		positions.map { HandKey.Pos(it) }.chunked(POSITION_ROW_SIZE)

	const val POSITION_ROW_SIZE = 7
}

internal val HandKey.span: Int
	get() = if (this == HandKey.Num(0)) 2 else 1

internal val numberRanks = mapOf(
	9 to Rank.NINE,
	8 to Rank.EIGHT,
	7 to Rank.SEVEN,
	6 to Rank.SIX,
	5 to Rank.FIVE,
	4 to Rank.FOUR,
	3 to Rank.THREE,
	2 to Rank.TWO,
)

internal fun Card.toHandKeys(): List<HandKey> {
	val rankKey = numberRanks.entries.find {
		it.value == rank
	}?.let { HandKey.Num(it.key) } ?: HandKey.Face(rank)
	return listOfNotNull(rankKey, suit?.let { HandKey.SuitKey(it) })
}

internal fun HandKey.toInput(allowed: Set<QuickInput>): QuickInput? {
	val candidates: List<QuickInput> = when (this) {
		is HandKey.Pos -> listOf(QuickInput.Seat(position))
		is HandKey.Act -> listOf(QuickInput.Act(type))
		is HandKey.Sized -> listOf(QuickInput.PotSize(size))
		is HandKey.Face -> listOf(QuickInput.CardRank(rank))
		is HandKey.Num -> listOfNotNull(
			QuickInput.Digit(value),
			numberRanks[value]?.let { QuickInput.CardRank(it) },
		)
		is HandKey.SuitKey -> listOf(QuickInput.CardSuit(suit))
		HandKey.Dot -> listOf(QuickInput.Dot)
		HandKey.NextLine -> listOf(QuickInput.NextLine)
		HandKey.AmountUnit -> listOf(QuickInput.ToggleAmountUnit)
		HandKey.Backspace, HandKey.CardPicker -> emptyList()
	}
	return candidates.firstOrNull { it in allowed }
}

internal fun HandKey.label(sizedAsMultiple: Boolean): String =
	if (this is HandKey.Sized && sizedAsMultiple) {
		HandHistoryFormatter.formatPotSize(
			size,
			isRaise = true,
		)
	} else {
		notation
	}

internal val HandKey.notation: String
	get() = when (this) {
		is HandKey.Pos -> position.label
		is HandKey.Act -> when (type) {
			ActionType.FOLD -> "Fold"
			ActionType.CHECK -> "Check"
			ActionType.CALL -> "Call"
			ActionType.BET -> "Bet"
			ActionType.RAISE -> "Raise"
			ActionType.ALL_IN -> "All-in"
		}
		is HandKey.Sized -> HandHistoryFormatter.formatPotSize(size, isRaise = false)
		is HandKey.Face -> if (rank == Rank.TEN) "T" else rank.symbol
		is HandKey.Num -> value.toString()
		is HandKey.SuitKey -> suit.symbol
		HandKey.Dot -> "."
		HandKey.AmountUnit -> "BB"
		HandKey.NextLine, HandKey.Backspace, HandKey.CardPicker -> ""
	}

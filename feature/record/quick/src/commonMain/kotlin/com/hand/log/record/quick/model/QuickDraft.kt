package com.hand.log.record.quick.model

import com.hand.log.domain.model.ActionType
import com.hand.log.domain.model.Card
import com.hand.log.domain.model.Rank
import com.hand.log.domain.model.Suit

internal data class DraftContext(
	val maxCards: Int,
	val usedCards: Set<Card>,
	val numbersAreRanks: Boolean,
	val allInNeedsAmount: Boolean,
)

internal object QuickDraft {

	fun append(draft: List<HandKey>, key: HandKey, context: DraftContext): List<HandKey>? = when (key) {
		is HandKey.Pos -> replaceOrAppend(draft, key) { it is HandKey.Pos }
		is HandKey.Act -> replaceOrAppend(draft, key) { it is HandKey.Act }?.let { updated ->
			if (needsAmount(key.type, context)) updated else withoutAmount(updated)
		}
		is HandKey.Sized -> if (hasAmountlessAction(draft, context)) null else withoutAmount(draft) + key
		is HandKey.Num -> appendNumber(draft, key, context)
		HandKey.Dot -> appendDot(draft, context)
		is HandKey.Face -> if (canAddCard(draft, key.rank, context)) draft + key else null
		is HandKey.SuitKey -> appendSuit(draft, key.suit, context)
		else -> null
	}

	private fun replaceOrAppend(
		draft: List<HandKey>,
		key: HandKey,
		isSameKind: (HandKey) -> Boolean,
	): List<HandKey>? {
		if (key in draft) return null
		val index = draft.indexOfFirst(isSameKind)
		return if (index < 0) draft + key else draft.toMutableList().apply { set(index, key) }
	}

	private fun appendNumber(draft: List<HandKey>, key: HandKey.Num, context: DraftContext): List<HandKey>? {
		if (context.numbersAreRanks) {
			val rank = numberRanks[key.value] ?: return null
			return if (canAddCard(draft, rank, context)) draft + key else null
		}
		val base = draft.filterNot { it is HandKey.Sized }
		val amount = parse(base, context).amount
		val dot = amount.indexOf('.')
		if (dot >= 0 && amount.length - dot > 1) return null
		if (amount.length >= MAX_AMOUNT_LENGTH) return null
		return base + key
	}

	private fun appendDot(draft: List<HandKey>, context: DraftContext): List<HandKey>? {
		if (context.numbersAreRanks || hasAmountlessAction(draft, context)) return null
		val amount = parse(draft, context).amount
		if (amount.isEmpty() || '.' in amount || draft.lastOrNull() !is HandKey.Num) return null
		return draft + HandKey.Dot
	}

	private fun appendSuit(draft: List<HandKey>, suit: Suit, context: DraftContext): List<HandKey>? {
		val updated = draft + HandKey.SuitKey(suit)
		val line = QuickLineParser.ParsedLine.of(updated, context.numbersAreRanks, suitsShared = false) ?: return null
		if (line.cards.size > context.maxCards) return null
		return updated.takeUnless { hasCardConflict(line, context.usedCards) }
	}

	fun hasCardConflict(draft: List<HandKey>, context: DraftContext): Boolean {
		val line = QuickLineParser.ParsedLine.of(draft, context.numbersAreRanks) ?: return false
		return hasCardConflict(line, context.usedCards)
	}

	private fun hasCardConflict(line: QuickLineParser.ParsedLine, usedCards: Set<Card>): Boolean {
		val suited = line.cards.mapNotNull { (rank, cardSuit) -> cardSuit?.let { Card(rank, it) } }
		return suited.distinct().size != suited.size || suited.any { it in usedCards }
	}

	private fun canAddCard(draft: List<HandKey>, rank: Rank, context: DraftContext): Boolean {
		val cards = parse(draft, context).cards
		if (cards.size >= context.maxCards) return false
		val used = context.usedCards.count { it.rank == rank } + cards.count { it.first == rank }
		return used < Suit.entries.size
	}

	private fun withoutAmount(draft: List<HandKey>): List<HandKey> =
		draft.filterIndexed { index, key ->
			when (key) {
				is HandKey.Sized, HandKey.Dot -> false
				is HandKey.Num -> draft.rankAt(index, numbersAreRanks = false) != null
				else -> true
			}
		}

	private fun hasAmountlessAction(draft: List<HandKey>, context: DraftContext): Boolean =
		draft.filterIsInstance<HandKey.Act>().any { !needsAmount(it.type, context) }

	fun needsAmount(type: ActionType, context: DraftContext): Boolean =
		type == ActionType.BET || type == ActionType.RAISE || (type == ActionType.ALL_IN && context.allInNeedsAmount)

	private fun parse(draft: List<HandKey>, context: DraftContext) =
		QuickLineParser.ParsedLine.of(draft, context.numbersAreRanks) ?: QuickLineParser.ParsedLine.EMPTY

	private const val MAX_AMOUNT_LENGTH = 9
}

package com.hand.log.record.quick.model

import com.hand.log.domain.model.ActionType
import com.hand.log.domain.model.Position
import com.hand.log.domain.model.Rank
import com.hand.log.domain.model.Suit
import com.hand.log.domain.model.quick.BetSize
import com.hand.log.domain.model.quick.QuickHandSnapshot
import com.hand.log.domain.model.quick.QuickInput
import com.hand.log.domain.model.quick.QuickPhase

internal object QuickLineParser {

	fun parse(tokens: List<HandKey>, snapshot: QuickHandSnapshot): List<QuickInput>? {
		if (tokens.isEmpty()) return null
		val line = ParsedLine.of(tokens, numbersAreRanks = snapshot.phase == QuickPhase.BOARD) ?: return null
		return when (snapshot.phase) {
			QuickPhase.BOARD -> boardInputs(line, snapshot.boardSlots)
			QuickPhase.HERO_CARDS -> heroInputs(line, snapshot)
			QuickPhase.ACTION -> actionInputs(line, snapshot.actorPosition)
			QuickPhase.REVIEW -> null
		}
	}

	fun isBoardComplete(tokens: List<HandKey>, snapshot: QuickHandSnapshot): Boolean {
		if (snapshot.phase != QuickPhase.BOARD || snapshot.boardSlots == 0) return false
		val line = ParsedLine.of(tokens, numbersAreRanks = true) ?: return false
		return line.isCardsOnly && !line.hasOpenSuitRun && line.cards.size == snapshot.boardSlots && line.cards.all {
			it.second != null
		}
	}

	private fun boardInputs(line: ParsedLine, slots: Int): List<QuickInput>? {
		if (!line.isCardsOnly || line.cards.size != slots) return null
		return line.cardInputs + QuickInput.NextLine
	}

	private fun heroInputs(line: ParsedLine, snapshot: QuickHandSnapshot): List<QuickInput>? {
		if (line.cards.size > HOLE_CARDS) return null
		val heroCards = line.cardInputs + QuickInput.NextLine
		if (line.isCardsOnly) return heroCards
		val rest = actionInputs(line.copy(cards = emptyList()), snapshot.actorPosition) ?: return null
		return heroCards + rest
	}

	private fun actionInputs(line: ParsedLine, actor: Position?): List<QuickInput>? {
		if (line.cards.size > HOLE_CARDS) return null
		if (line.action == null && (line.amount.isNotEmpty() || line.potSize != null)) return null
		val target = line.position ?: actor
		return buildList {
			line.position?.let { add(QuickInput.Seat(it)) }
			if (line.cards.isNotEmpty()) {
				if (line.position == null) add(QuickInput.Seat(target ?: return null))
				addAll(line.cardInputs)
			}
			line.action?.let { action ->
				add(QuickInput.Act(action))
				line.potSize?.let { add(QuickInput.PotSize(it)) }
				line.amount.forEach { char ->
					add(if (char == '.') QuickInput.Dot else QuickInput.Digit(char.digitToInt()))
				}
			}
			add(QuickInput.NextLine)
		}
	}

	internal data class ParsedLine(
		val position: Position?,
		val action: ActionType?,
		val potSize: BetSize?,
		val amount: String,
		val cards: List<Pair<Rank, Suit?>>,
		val hasOpenSuitRun: Boolean = false,
	) {
		val isCardsOnly: Boolean
			get() = position == null && action == null && potSize == null && amount.isEmpty()

		val cardInputs: List<QuickInput>
			get() = cards.flatMap { (rank, suit) ->
				listOfNotNull(QuickInput.CardRank(rank), suit?.let { QuickInput.CardSuit(it) })
			}

		companion object {
			val EMPTY = ParsedLine(null, null, null, "", emptyList())

			fun of(tokens: List<HandKey>, numbersAreRanks: Boolean = false, suitsShared: Boolean = true): ParsedLine? {
				var position: Position? = null
				var action: ActionType? = null
				var potSize: BetSize? = null
				val amount = StringBuilder()
				val cards = mutableListOf<Pair<Rank, Suit?>>()
				val run = mutableListOf<Int>()
				var runSuits = 0
				tokens.forEachIndexed { index, token ->
					val rank = tokens.rankAt(index, numbersAreRanks)
					if (rank != null) {
						if (runSuits > 0) {
							run.clear()
							runSuits = 0
						}
						run += cards.size
						cards += rank to null
						return@forEachIndexed
					}
					if (token is HandKey.SuitKey) {
						if (runSuits >= run.size) return null
						if (runSuits == 1) run.drop(1).forEach { cards[it] = cards[it].first to null }
						val targets = when {
							runSuits > 0 -> listOf(run[runSuits])
							suitsShared && run.map { cards[it].first }.distinct().size == run.size -> run
							else -> listOf(run.first())
						}
						targets.forEach { cardIndex -> cards[cardIndex] = cards[cardIndex].first to token.suit }
						runSuits++
						return@forEachIndexed
					}
					when (token) {
						is HandKey.Pos -> position = token.position
						is HandKey.Act -> action = token.type
						is HandKey.Sized -> potSize = token.size
						is HandKey.Num -> amount.append(token.value)
						HandKey.Dot -> amount.append('.')
						else -> Unit
					}
					run.clear()
					runSuits = 0
				}
				return ParsedLine(
					position = position,
					action = action,
					potSize = potSize,
					amount = amount.toString(),
					cards = cards,
					hasOpenSuitRun = runSuits in 1 until run.size,
				)
			}
		}
	}

	private const val HOLE_CARDS = 2
}

internal fun List<HandKey>.rankAt(index: Int, numbersAreRanks: Boolean): Rank? = when (
	val token = get(
		index,
	)
) {
	is HandKey.Face -> token.rank
	is HandKey.Num -> numberRanks[token.value]?.takeIf {
		numbersAreRanks || getOrNull(index + 1) is HandKey.SuitKey || isRankPair(index)
	}
	else -> null
}

private fun List<HandKey>.isRankPair(index: Int): Boolean {
	val start = (index downTo 0).takeWhile { get(it) is HandKey.Num }.last()
	val end = (index until size).takeWhile { get(it) is HandKey.Num }.last()
	val numbers = subList(start, end + 1).filterIsInstance<HandKey.Num>()
	return numbers.size == PAIR_RUN &&
		numbers.all { it.value in numberRanks } &&
		getOrNull(end + 1) is HandKey.SuitKey
}

private const val PAIR_RUN = 2

internal fun List<HandKey>.draftText(sizedAsMultiple: Boolean = false): String =
	buildString {
		this@draftText.forEachIndexed { index, key ->
			val previous = this@draftText.getOrNull(index - 1)
			val attaches = key is HandKey.SuitKey ||
				((key is HandKey.Num || key == HandKey.Dot) && (previous is HandKey.Num || previous == HandKey.Dot))
			if (index > 0 && !attaches) append(' ')
			append(if (key is HandKey.SuitKey) key.suit.symbol else key.label(sizedAsMultiple))
		}
	}

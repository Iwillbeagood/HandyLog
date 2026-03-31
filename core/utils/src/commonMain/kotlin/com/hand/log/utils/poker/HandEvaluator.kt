package com.hand.log.utils.poker

import com.hand.log.domain.model.Card
import com.hand.log.domain.model.EvaluatedHand
import com.hand.log.domain.model.HandRanking
import com.hand.log.domain.model.Rank
import com.hand.log.domain.model.ShowdownEntry
import com.hand.log.domain.model.ShowdownOutcome
import com.hand.log.domain.model.ShowdownResult

object HandEvaluator {

	/** Rank를 숫자로 변환 (ACE=14, KING=13, ..., TWO=2) */
	private fun Rank.value(): Int = when (this) {
		Rank.ACE -> 14
		Rank.KING -> 13
		Rank.QUEEN -> 12
		Rank.JACK -> 11
		Rank.TEN -> 10
		Rank.NINE -> 9
		Rank.EIGHT -> 8
		Rank.SEVEN -> 7
		Rank.SIX -> 6
		Rank.FIVE -> 5
		Rank.FOUR -> 4
		Rank.THREE -> 3
		Rank.TWO -> 2
	}

	/** 5장 핸드를 평가 */
	private fun evaluate5(cards: List<Card>): EvaluatedHand {
		require(cards.size == 5)
		val values = cards.map { it.rank.value() }.sortedDescending()
		val suits = cards.map { it.suit }

		val isFlush = suits.toSet().size == 1
		val isWheel = values == listOf(14, 5, 4, 3, 2)
		val isStraight = if (isWheel) {
			true
		} else {
			values.zipWithNext().all { (a, b) -> a - b == 1 }
		}

		val straightHighValue = if (isWheel) 5 else values.first()

		val freqMap = values.groupBy { it }.mapValues { it.value.size }
		val freqs = freqMap.entries.sortedWith(
			compareByDescending<Map.Entry<Int, Int>> { it.value }.thenByDescending { it.key },
		)

		return when {
			isFlush && isStraight && straightHighValue == 14 && !isWheel ->
				EvaluatedHand(HandRanking.ROYAL_FLUSH, listOf(14))

			isFlush && isStraight ->
				EvaluatedHand(HandRanking.STRAIGHT_FLUSH, listOf(straightHighValue))

			freqs[0].value == 4 ->
				EvaluatedHand(HandRanking.FOUR_OF_A_KIND, listOf(freqs[0].key, freqs[1].key))

			freqs[0].value == 3 && freqs[1].value == 2 ->
				EvaluatedHand(HandRanking.FULL_HOUSE, listOf(freqs[0].key, freqs[1].key))

			isFlush ->
				EvaluatedHand(HandRanking.FLUSH, values)

			isStraight ->
				EvaluatedHand(HandRanking.STRAIGHT, listOf(straightHighValue))

			freqs[0].value == 3 -> {
				val kickers = freqs.drop(1).map { it.key }.sortedDescending()
				EvaluatedHand(HandRanking.THREE_OF_A_KIND, listOf(freqs[0].key) + kickers)
			}

			freqs[0].value == 2 && freqs[1].value == 2 -> {
				val pair1 = maxOf(freqs[0].key, freqs[1].key)
				val pair2 = minOf(freqs[0].key, freqs[1].key)
				EvaluatedHand(HandRanking.TWO_PAIR, listOf(pair1, pair2, freqs[2].key))
			}

			freqs[0].value == 2 -> {
				val kickers = freqs.drop(1).map { it.key }.sortedDescending()
				EvaluatedHand(HandRanking.ONE_PAIR, listOf(freqs[0].key) + kickers)
			}

			else -> EvaluatedHand(HandRanking.HIGH_CARD, values)
		}
	}

	/** 5~7장에서 가능한 최고의 5장 핸드를 찾음 */
	fun evaluateBest(cards: List<Card>): EvaluatedHand {
		require(cards.size in 5..7)
		if (cards.size == 5) return evaluate5(cards)

		var best: EvaluatedHand? = null
		val indices = cards.indices.toList()

		combinations(indices, 5).forEach { combo ->
			val hand = combo.map { cards[it] }
			val evaluated = evaluate5(hand)
			if (best == null || evaluated < best!!) {
				best = evaluated
			}
		}

		return best!!
	}

	/** 5~7장에서 최고의 5장 핸드와 해당 카드를 함께 반환 */
	fun evaluateBestWithCards(cards: List<Card>): Pair<EvaluatedHand, List<Card>> {
		require(cards.size in 5..7)
		if (cards.size == 5) return evaluate5(cards) to cards.sortedBy { it.rank.ordinal }

		var best: EvaluatedHand? = null
		var bestCards: List<Card> = emptyList()
		val indices = cards.indices.toList()

		combinations(indices, 5).forEach { combo ->
			val hand = combo.map { cards[it] }
			val evaluated = evaluate5(hand)
			if (best == null || evaluated < best!!) {
				best = evaluated
				bestCards = hand
			}
		}

		return best!! to bestCards.sortedBy { it.rank.ordinal }
	}

	/** 쇼다운 결과 계산. 보드 카드 + 각 플레이어 홀카드로 승자 결정 */
	fun calculateShowdown(
		boardCards: List<Card>,
		playerHands: List<ShowdownEntry>,
	): List<ShowdownResult> {
		if (playerHands.isEmpty()) return emptyList()

		val evaluated = playerHands.map { entry ->
			val allCards = boardCards + listOf(entry.card1, entry.card2)
			if (allCards.size >= 5) {
				val (hand, cards) = evaluateBestWithCards(allCards)
				Triple(entry.seat, hand, cards)
			} else {
				Triple(entry.seat, null, emptyList<Card>())
			}
		}

		val bestHand = evaluated.mapNotNull { it.second }.minOrNull()
		val winnerCount = if (bestHand != null) {
			evaluated.count { it.second != null && it.second == bestHand }
		} else {
			0
		}
		val isSplit = winnerCount > 1

		return evaluated.map { (seat, hand, cards) ->
			val isBest = hand != null && hand == bestHand
			ShowdownResult(
				seat = seat,
				ranking = hand?.ranking ?: HandRanking.HIGH_CARD,
				bestCards = cards,
				outcome = when {
					isBest && isSplit -> ShowdownOutcome.SPLIT
					isBest -> ShowdownOutcome.WIN
					else -> ShowdownOutcome.LOSE
				},
			)
		}
	}

	private fun <T> combinations(list: List<T>, k: Int): List<List<T>> {
		if (k == 0) return listOf(emptyList())
		if (list.size < k) return emptyList()
		if (list.size == k) return listOf(list)

		val first = list.first()
		val rest = list.drop(1)
		return combinations(rest, k - 1).map { listOf(first) + it } + combinations(rest, k)
	}
}

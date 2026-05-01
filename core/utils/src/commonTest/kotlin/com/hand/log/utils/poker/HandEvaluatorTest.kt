package com.hand.log.utils.poker

import com.hand.log.domain.model.Card
import com.hand.log.domain.model.HandRanking
import com.hand.log.domain.model.Rank
import com.hand.log.domain.model.PocketCards
import com.hand.log.domain.model.ShowdownEntry
import com.hand.log.domain.model.Suit
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HandEvaluatorTest {

	private fun card(rank: Rank, suit: Suit) = Card(rank, suit)

	@Test
	fun `로열 플러시 판별`() {
		val cards = listOf(
			card(Rank.ACE, Suit.SPADES),
			card(Rank.KING, Suit.SPADES),
			card(Rank.QUEEN, Suit.SPADES),
			card(Rank.JACK, Suit.SPADES),
			card(Rank.TEN, Suit.SPADES),
		)
		val result = HandEvaluator.evaluateBest(cards)
		assertEquals(HandRanking.ROYAL_FLUSH, result.ranking)
	}

	@Test
	fun `스트레이트 플러시 판별`() {
		val cards = listOf(
			card(Rank.NINE, Suit.HEARTS),
			card(Rank.EIGHT, Suit.HEARTS),
			card(Rank.SEVEN, Suit.HEARTS),
			card(Rank.SIX, Suit.HEARTS),
			card(Rank.FIVE, Suit.HEARTS),
		)
		val result = HandEvaluator.evaluateBest(cards)
		assertEquals(HandRanking.STRAIGHT_FLUSH, result.ranking)
	}

	@Test
	fun `A2345 휠 스트레이트 플러시`() {
		val cards = listOf(
			card(Rank.ACE, Suit.DIAMONDS),
			card(Rank.TWO, Suit.DIAMONDS),
			card(Rank.THREE, Suit.DIAMONDS),
			card(Rank.FOUR, Suit.DIAMONDS),
			card(Rank.FIVE, Suit.DIAMONDS),
		)
		val result = HandEvaluator.evaluateBest(cards)
		assertEquals(HandRanking.STRAIGHT_FLUSH, result.ranking)
	}

	@Test
	fun `포카드 판별`() {
		val cards = listOf(
			card(Rank.ACE, Suit.SPADES),
			card(Rank.ACE, Suit.HEARTS),
			card(Rank.ACE, Suit.DIAMONDS),
			card(Rank.ACE, Suit.CLUBS),
			card(Rank.KING, Suit.SPADES),
		)
		val result = HandEvaluator.evaluateBest(cards)
		assertEquals(HandRanking.FOUR_OF_A_KIND, result.ranking)
	}

	@Test
	fun `풀하우스 판별`() {
		val cards = listOf(
			card(Rank.ACE, Suit.SPADES),
			card(Rank.ACE, Suit.HEARTS),
			card(Rank.ACE, Suit.DIAMONDS),
			card(Rank.KING, Suit.SPADES),
			card(Rank.KING, Suit.HEARTS),
		)
		val result = HandEvaluator.evaluateBest(cards)
		assertEquals(HandRanking.FULL_HOUSE, result.ranking)
	}

	@Test
	fun `플러시 판별`() {
		val cards = listOf(
			card(Rank.ACE, Suit.HEARTS),
			card(Rank.TEN, Suit.HEARTS),
			card(Rank.SEVEN, Suit.HEARTS),
			card(Rank.FOUR, Suit.HEARTS),
			card(Rank.TWO, Suit.HEARTS),
		)
		val result = HandEvaluator.evaluateBest(cards)
		assertEquals(HandRanking.FLUSH, result.ranking)
	}

	@Test
	fun `스트레이트 판별`() {
		val cards = listOf(
			card(Rank.TEN, Suit.SPADES),
			card(Rank.NINE, Suit.HEARTS),
			card(Rank.EIGHT, Suit.DIAMONDS),
			card(Rank.SEVEN, Suit.CLUBS),
			card(Rank.SIX, Suit.SPADES),
		)
		val result = HandEvaluator.evaluateBest(cards)
		assertEquals(HandRanking.STRAIGHT, result.ranking)
	}

	@Test
	fun `투페어 판별`() {
		val cards = listOf(
			card(Rank.ACE, Suit.SPADES),
			card(Rank.ACE, Suit.HEARTS),
			card(Rank.KING, Suit.DIAMONDS),
			card(Rank.KING, Suit.CLUBS),
			card(Rank.QUEEN, Suit.SPADES),
		)
		val result = HandEvaluator.evaluateBest(cards)
		assertEquals(HandRanking.TWO_PAIR, result.ranking)
	}

	@Test
	fun `원페어 판별`() {
		val cards = listOf(
			card(Rank.ACE, Suit.SPADES),
			card(Rank.ACE, Suit.HEARTS),
			card(Rank.KING, Suit.DIAMONDS),
			card(Rank.QUEEN, Suit.CLUBS),
			card(Rank.JACK, Suit.SPADES),
		)
		val result = HandEvaluator.evaluateBest(cards)
		assertEquals(HandRanking.ONE_PAIR, result.ranking)
	}

	@Test
	fun `하이카드 판별`() {
		val cards = listOf(
			card(Rank.ACE, Suit.SPADES),
			card(Rank.KING, Suit.HEARTS),
			card(Rank.QUEEN, Suit.DIAMONDS),
			card(Rank.JACK, Suit.CLUBS),
			card(Rank.NINE, Suit.SPADES),
		)
		val result = HandEvaluator.evaluateBest(cards)
		assertEquals(HandRanking.HIGH_CARD, result.ranking)
	}

	@Test
	fun `7장에서 최고 핸드 선택`() {
		// 홀카드: AKs, 보드: A Q J 10 2 → 원페어(AA)가 아니라 스트레이트(A-K-Q-J-10)
		val cards = listOf(
			card(Rank.ACE, Suit.SPADES),
			card(Rank.KING, Suit.SPADES),
			card(Rank.ACE, Suit.HEARTS),
			card(Rank.QUEEN, Suit.DIAMONDS),
			card(Rank.JACK, Suit.CLUBS),
			card(Rank.TEN, Suit.HEARTS),
			card(Rank.TWO, Suit.SPADES),
		)
		val result = HandEvaluator.evaluateBest(cards)
		assertEquals(HandRanking.STRAIGHT, result.ranking)
	}

	@Test
	fun `쇼다운 승자 판별`() {
		val board = listOf(
			card(Rank.ACE, Suit.HEARTS),
			card(Rank.KING, Suit.DIAMONDS),
			card(Rank.QUEEN, Suit.CLUBS),
			card(Rank.JACK, Suit.SPADES),
			card(Rank.TWO, Suit.HEARTS),
		)

		val players = listOf(
			ShowdownEntry(
				seat = 1,
				cards = PocketCards(card(Rank.TEN, Suit.HEARTS), card(Rank.NINE, Suit.HEARTS)),
			),
			ShowdownEntry(
				seat = 2,
				cards = PocketCards(card(Rank.ACE, Suit.SPADES), card(Rank.ACE, Suit.CLUBS)),
			),
		)

		val results = HandEvaluator.calculateShowdown(board, players)

		// seat 1: A-K-Q-J-10 스트레이트
		// seat 2: AAA + K + Q 트리플
		// 스트레이트 > 트리플
		val winner = results.find { it.isWinner }
		assertEquals(1, winner?.seat)
		assertEquals(HandRanking.STRAIGHT, winner?.ranking)
	}

	@Test
	fun `같은 족보면 키커로 비교`() {
		val board = listOf(
			card(Rank.ACE, Suit.HEARTS),
			card(Rank.KING, Suit.DIAMONDS),
			card(Rank.SEVEN, Suit.CLUBS),
			card(Rank.FOUR, Suit.SPADES),
			card(Rank.TWO, Suit.HEARTS),
		)

		val players = listOf(
			ShowdownEntry(
				seat = 1,
				cards = PocketCards(card(Rank.ACE, Suit.SPADES), card(Rank.QUEEN, Suit.HEARTS)),
			),
			ShowdownEntry(
				seat = 2,
				cards = PocketCards(card(Rank.ACE, Suit.CLUBS), card(Rank.JACK, Suit.HEARTS)),
			),
		)

		val results = HandEvaluator.calculateShowdown(board, players)

		// 둘 다 원페어(AA), seat 1 키커 Q > seat 2 키커 J
		val winner = results.find { it.isWinner }
		assertEquals(1, winner?.seat)
	}

	@Test
	fun `1명만 쇼다운 시 자동 승리`() {
		val board = listOf(
			card(Rank.ACE, Suit.HEARTS),
			card(Rank.KING, Suit.DIAMONDS),
			card(Rank.QUEEN, Suit.CLUBS),
			card(Rank.JACK, Suit.SPADES),
			card(Rank.TWO, Suit.HEARTS),
		)

		val players = listOf(
			ShowdownEntry(
				seat = 1,
				cards = PocketCards(card(Rank.TEN, Suit.HEARTS), card(Rank.NINE, Suit.HEARTS)),
			),
		)

		val results = HandEvaluator.calculateShowdown(board, players)
		assertEquals(1, results.size)
		assertTrue(results.first().isWinner)
		assertEquals(HandRanking.STRAIGHT, results.first().ranking)
	}

	@Test
	fun `풀하우스가 플러시를 이긴다`() {
		val hand1 = HandEvaluator.evaluateBest(
			listOf(
				card(Rank.ACE, Suit.SPADES),
				card(Rank.ACE, Suit.HEARTS),
				card(Rank.ACE, Suit.DIAMONDS),
				card(Rank.KING, Suit.SPADES),
				card(Rank.KING, Suit.HEARTS),
			),
		)
		val hand2 = HandEvaluator.evaluateBest(
			listOf(
				card(Rank.ACE, Suit.CLUBS),
				card(Rank.TEN, Suit.CLUBS),
				card(Rank.SEVEN, Suit.CLUBS),
				card(Rank.FOUR, Suit.CLUBS),
				card(Rank.TWO, Suit.CLUBS),
			),
		)
		assertTrue(hand1 < hand2) // 풀하우스가 더 강함 (compareTo < 0)
	}
}

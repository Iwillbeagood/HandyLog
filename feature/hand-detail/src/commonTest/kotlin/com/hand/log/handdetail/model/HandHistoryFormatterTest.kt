package com.hand.log.handdetail.model

import com.hand.log.domain.model.Action
import com.hand.log.domain.model.ActionType
import com.hand.log.domain.model.Blinds
import com.hand.log.domain.model.Card
import com.hand.log.domain.model.FlopStreet
import com.hand.log.domain.model.HandRecord
import com.hand.log.domain.model.HandStreets
import com.hand.log.domain.model.PocketCards
import com.hand.log.domain.model.PreflopStreet
import com.hand.log.domain.model.Rank
import com.hand.log.domain.model.RiverStreet
import com.hand.log.domain.model.ShowdownEntry
import com.hand.log.domain.model.Suit
import com.hand.log.domain.model.TurnStreet
import com.hand.log.utils.poker.HandEvaluator
import kotlin.test.Test
import kotlin.test.assertEquals

class HandHistoryFormatterTest {
	private fun c(r: Rank, s: Suit) = Card(r, s)
	private fun p(c1: Card, c2: Card) = PocketCards(c1, c2)
	private val ante = Blinds(sb = 500.0, bb = 1000.0, isBigBlindAnte = true)
	private val noAnte = Blinds(sb = 500.0, bb = 1000.0)
	private fun sd(board: List<Card>, e: List<ShowdownEntry>) = HandEvaluator.calculateShowdown(
		board,
		e,
	)
	private fun assertFmt(hand: HandRecord, expected: String) =
		assertEquals(expected.trimIndent().trim(), HandHistoryFormatter.format(hand).trim())

	@Test
	fun `BB앤티 헤즈업 - SB 올인 AA vs BB 콜 KK`() {
		val board =
			listOf(
				c(Rank.SEVEN, Suit.DIAMONDS),
				c(Rank.FOUR, Suit.CLUBS),
				c(Rank.TWO, Suit.SPADES),
				c(Rank.JACK, Suit.HEARTS),
				c(Rank.THREE, Suit.DIAMONDS),
			)
		val e =
			listOf(
				ShowdownEntry(5, p(c(Rank.ACE, Suit.SPADES), c(Rank.ACE, Suit.HEARTS))),
				ShowdownEntry(6, p(c(Rank.KING, Suit.SPADES), c(Rank.KING, Suit.HEARTS))),
			)
		assertFmt(
			HandRecord(
				id = "h1", tableId = "t1", createdAt = 0L, blinds = ante, heroHand = e[0].cards, heroSeat = 5, heroStack = 50000.0, buttonSeat = 4,
				streets = HandStreets(
					preflop = PreflopStreet(
						listOf(
							Action(1, ActionType.FOLD),
							Action(2, ActionType.FOLD),
							Action(3, ActionType.FOLD),
							Action(4, ActionType.FOLD),
							Action(5, ActionType.ALL_IN, 49000.0, 50000.0),
							Action(6, ActionType.CALL, 49000.0, 49000.0),
						),
					),
					flop = FlopStreet(board[0], board[1], board[2]),
					turn = TurnStreet(board[3]),
					river = RiverStreet(board[4]),
				),
				showdown = e, showdownResults = sd(board, e), result = 49000.0,
			),
			"""
            [Stacks]
            Hero (SB) - 50BB A♠A♥
            BB - 49BB

            [Preflop]
            Hero (SB) bets 49BB (all-in)
            BB calls all-in for 48BB

            → Pot: 99BB (Heads-up)

            [Flop] 7♦ 4♣ 2♠
            → Pot: 99BB (Heads-up)

            [Turn] J♥
            → Pot: 99BB

            [River] 3♦
            → Pot: 99BB

            [Showdown]
            Hero (SB): A♠ A♥ — ONE PAIR [WIN]
            BB: K♠ K♥ — ONE PAIR [LOSE]

            [Result] +50BB
            Hero (SB): 100BB
            BB: 0BB
            """,
		)
	}

	@Test
	fun `BB앤티 스플릿 - SB AK vs BB AK`() {
		val board =
			listOf(
				c(Rank.ACE, Suit.DIAMONDS),
				c(Rank.TEN, Suit.CLUBS),
				c(Rank.SEVEN, Suit.HEARTS),
				c(Rank.FOUR, Suit.SPADES),
				c(Rank.TWO, Suit.CLUBS),
			)
		val e =
			listOf(
				ShowdownEntry(5, p(c(Rank.ACE, Suit.SPADES), c(Rank.KING, Suit.HEARTS))),
				ShowdownEntry(6, p(c(Rank.ACE, Suit.HEARTS), c(Rank.KING, Suit.DIAMONDS))),
			)
		assertFmt(
			HandRecord(
				id = "h2", tableId = "t1", createdAt = 0L, blinds = ante, heroHand = e[0].cards, heroSeat = 5, heroStack = 40000.0, buttonSeat = 4,
				streets = HandStreets(
					preflop = PreflopStreet(
						listOf(
							Action(1, ActionType.FOLD),
							Action(2, ActionType.FOLD),
							Action(3, ActionType.FOLD),
							Action(4, ActionType.FOLD),
							Action(5, ActionType.ALL_IN, 39000.0, 40000.0),
							Action(6, ActionType.CALL, 39000.0, 39000.0),
						),
					),
					flop = FlopStreet(board[0], board[1], board[2]),
					turn = TurnStreet(board[3]),
					river = RiverStreet(board[4]),
				),
				showdown = e, showdownResults = sd(board, e), result = 0.0,
			),
			"""
            [Stacks]
            Hero (SB) - 40BB A♠K♥
            BB - 39BB

            [Preflop]
            Hero (SB) bets 39BB (all-in)
            BB calls all-in for 38BB

            → Pot: 79BB (Heads-up)

            [Flop] A♦ 10♣ 7♥
            → Pot: 79BB (Heads-up)

            [Turn] 4♠
            → Pot: 79BB

            [River] 2♣
            → Pot: 79BB

            [Showdown]
            Hero (SB): A♠ K♥ — ONE PAIR [SPLIT]
            BB: A♥ K♦ — ONE PAIR [SPLIT]

            [Result] +0.5BB
            Hero (SB): 40.5BB
            BB: 39.5BB
            """,
		)
	}

	@Test
	fun `BB앤티 3way 사이드팟 - QQ vs AK vs JJ`() {
		val board =
			listOf(
				c(Rank.TEN, Suit.SPADES),
				c(Rank.EIGHT, Suit.HEARTS),
				c(Rank.THREE, Suit.CLUBS),
				c(Rank.FIVE, Suit.DIAMONDS),
				c(Rank.TWO, Suit.HEARTS),
			)
		val e =
			listOf(
				ShowdownEntry(4, p(c(Rank.QUEEN, Suit.HEARTS), c(Rank.QUEEN, Suit.DIAMONDS))),
				ShowdownEntry(5, p(c(Rank.ACE, Suit.CLUBS), c(Rank.KING, Suit.CLUBS))),
				ShowdownEntry(1, p(c(Rank.JACK, Suit.SPADES), c(Rank.JACK, Suit.HEARTS))),
			)
		assertFmt(
			HandRecord(
				id = "h3", tableId = "t1", createdAt = 0L, blinds = ante, heroHand = e[0].cards, heroSeat = 4, heroStack = 25000.0, buttonSeat = 1,
				streets = HandStreets(
					preflop = PreflopStreet(
						listOf(
							Action(
								4,
								ActionType.RAISE,
								2200.0,
								25000.0,
								betLevel = 2,
							),
							Action(
								5,
								ActionType.CALL,
								2200.0,
								30000.0,
							),
							Action(
								6,
								ActionType.FOLD,
							),
							Action(
								7,
								ActionType.FOLD,
							),
							Action(
								8,
								ActionType.FOLD,
							),
							Action(
								9,
								ActionType.FOLD,
							),
							Action(
								1,
								ActionType.ALL_IN,
								15000.0,
								15000.0,
							),
							Action(
								2,
								ActionType.FOLD,
							),
							Action(
								3,
								ActionType.FOLD,
							),
							Action(
								4,
								ActionType.ALL_IN,
								25000.0,
								22800.0,
							),
							Action(5, ActionType.CALL, 25000.0, 27800.0),
						),
					),
					flop = FlopStreet(board[0], board[1], board[2]),
					turn = TurnStreet(board[3]),
					river = RiverStreet(board[4]),
				),
				showdown = e, showdownResults = sd(board, e), result = 41000.0,
			),
			"""
            [Stacks]
            BTN - 15BB
            Hero (UTG) - 25BB Q♥Q♦
            UTG+1 - 30BB

            [Preflop]
            Hero (UTG) opens to 2.2BB
            UTG+1 calls 2.2BB
            BTN raises to 15BB (all-in)
            Hero (UTG) raises to 25BB (all-in)
            UTG+1 calls 22.8BB

            → Pot: 67.5BB (3-way)

            [Flop] 10♠ 8♥ 3♣
            → Pot: 67.5BB (3-way)

            [Turn] 5♦
            → Pot: 67.5BB

            [River] 2♥
            → Pot: 67.5BB

            [Pot]
            Main: 47.5BB (3-way)
            Side 1: 20BB (Heads-up)

            [Showdown]
            Hero (UTG): Q♥ Q♦ — ONE PAIR [Main WIN, Side 1 WIN]
            UTG+1: A♣ K♣ — HIGH CARD [LOSE]
            BTN: J♠ J♥ — ONE PAIR [LOSE]

            [Result] +42.5BB
            BTN: 0BB
            Hero (UTG): 67.5BB
            UTG+1: 5BB
            """,
		)
	}

	@Test
	fun `BB앤티 프리플랍 4벳 폴드 승리`() {
		assertFmt(
			HandRecord(
				id = "h4", tableId = "t1", createdAt = 0L, blinds = ante,
				heroHand = p(
					c(Rank.ACE, Suit.SPADES),
					c(Rank.ACE, Suit.HEARTS),
				),
				heroSeat = 4, heroStack = 50000.0, buttonSeat = 1,
				streets = HandStreets(
					preflop = PreflopStreet(
						listOf(
							Action(
								4,
								ActionType.RAISE,
								2500.0,
								50000.0,
								betLevel = 2,
							),
							Action(
								5,
								ActionType.CALL,
								2500.0,
								50000.0,
							),
							Action(
								6,
								ActionType.RAISE,
								8000.0,
								50000.0,
								betLevel = 3,
							),
							Action(
								7,
								ActionType.FOLD,
							),
							Action(
								8,
								ActionType.FOLD,
							),
							Action(
								9,
								ActionType.FOLD,
							),
							Action(
								1,
								ActionType.FOLD,
							),
							Action(
								2,
								ActionType.FOLD,
							),
							Action(
								3,
								ActionType.FOLD,
							),
							Action(
								4,
								ActionType.RAISE,
								22000.0,
								47500.0,
								betLevel = 4,
							),
							Action(5, ActionType.FOLD), Action(6, ActionType.FOLD),
						),
					),
				),
				result = 13000.0,
			),
			"""
            [Stacks]
            Hero (UTG) - 50BB A♠A♥
            UTG+1 - 50BB
            MP - 50BB

            [Preflop]
            Hero (UTG) opens to 2.5BB
            UTG+1 calls 2.5BB
            MP 3-bets to 8BB
            Hero (UTG) 4-bets to 22BB
            UTG+1 folds
            MP folds

            → Pot: 35BB (3-way)

            [Result] +13BB
            Hero (UTG): 63BB
            UTG+1: 47.5BB
            MP: 42BB
            """,
		)
	}

	@Test
	fun `앤티없음 - 헤즈업 AK vs AK 스플릿`() {
		val board =
			listOf(
				c(Rank.ACE, Suit.DIAMONDS),
				c(Rank.TEN, Suit.CLUBS),
				c(Rank.SEVEN, Suit.HEARTS),
				c(Rank.FOUR, Suit.SPADES),
				c(Rank.TWO, Suit.CLUBS),
			)
		val e =
			listOf(
				ShowdownEntry(1, p(c(Rank.ACE, Suit.SPADES), c(Rank.KING, Suit.HEARTS))),
				ShowdownEntry(2, p(c(Rank.ACE, Suit.HEARTS), c(Rank.KING, Suit.DIAMONDS))),
			)
		assertFmt(
			HandRecord(
				id = "h6", tableId = "t1", createdAt = 0L, blinds = noAnte, heroHand = e[0].cards, heroSeat = 1, heroStack = 50000.0, buttonSeat = 1,
				streets = HandStreets(
					preflop = PreflopStreet(
						listOf(
							Action(1, ActionType.ALL_IN, 50000.0, 50000.0),
							Action(2, ActionType.CALL, 50000.0, 50000.0),
						),
					),
					flop = FlopStreet(board[0], board[1], board[2]),
					turn = TurnStreet(board[3]),
					river = RiverStreet(board[4]),
				),
				showdown = e, showdownResults = sd(board, e), result = 0.0,
			),
			"""
            [Stacks]
            Hero (BTN) - 50BB A♠K♥
            SB - 50BB

            [Preflop]
            Hero (BTN) bets 50BB (all-in)
            SB calls all-in for 49.5BB

            → Pot: 100BB (Heads-up)

            [Flop] A♦ 10♣ 7♥
            → Pot: 100BB (Heads-up)

            [Turn] 4♠
            → Pot: 100BB

            [River] 2♣
            → Pot: 100BB

            [Showdown]
            Hero (BTN): A♠ K♥ — ONE PAIR [SPLIT]
            SB: A♥ K♦ — ONE PAIR [SPLIT]

            [Result] +0BB
            Hero (BTN): 50BB
            SB: 50BB
            """,
		)
	}

	// ===== 엣지 케이스 =====

	@Test
	fun `멀티 사이드팟 + 일부만 쇼다운`() {
		val board =
			listOf(
				c(Rank.KING, Suit.SPADES),
				c(Rank.NINE, Suit.HEARTS),
				c(Rank.THREE, Suit.DIAMONDS),
				c(Rank.SEVEN, Suit.CLUBS),
				c(Rank.TWO, Suit.SPADES),
			)
		val e =
			listOf(
				ShowdownEntry(4, p(c(Rank.ACE, Suit.SPADES), c(Rank.ACE, Suit.HEARTS))),
				ShowdownEntry(5, p(c(Rank.KING, Suit.HEARTS), c(Rank.QUEEN, Suit.HEARTS))),
				ShowdownEntry(1, p(c(Rank.JACK, Suit.DIAMONDS), c(Rank.TEN, Suit.DIAMONDS))),
			)
		assertFmt(
			HandRecord(
				id = "e1", tableId = "t1", createdAt = 0L, blinds = ante, heroHand = e[0].cards, heroSeat = 4, heroStack = 100000.0, buttonSeat = 1,
				streets = HandStreets(
					preflop = PreflopStreet(
						listOf(
							Action(4, ActionType.RAISE, 2500.0, 100000.0, betLevel = 2),
							Action(5, ActionType.CALL, 2500.0, 80000.0),
							Action(6, ActionType.CALL, 2500.0, 40000.0),
							Action(7, ActionType.FOLD),
							Action(8, ActionType.FOLD),
							Action(9, ActionType.FOLD),
							Action(1, ActionType.ALL_IN, 20000.0, 20000.0),
							Action(2, ActionType.FOLD),
							Action(3, ActionType.FOLD),
							Action(4, ActionType.CALL, 20000.0, 97500.0),
							Action(5, ActionType.CALL, 20000.0, 77500.0),
							Action(6, ActionType.CALL, 20000.0, 37500.0),
						),
					),
					flop = FlopStreet(
						board[0],
						board[1],
						board[2],
						listOf(
							Action(4, ActionType.BET, 20000.0, 60000.0, betLevel = 1),
							Action(5, ActionType.CALL, 20000.0, 60000.0),
							Action(6, ActionType.FOLD),
						),
					),
					turn = TurnStreet(
						board[3],
						listOf(
							Action(5, ActionType.ALL_IN, 40000.0, 40000.0),
							Action(4, ActionType.CALL, 40000.0, 20000.0),
						),
					),
					river = RiverStreet(board[4]),
				),
				showdown = e, showdownResults = sd(board, e), result = 80000.0,
			),
			"""
		[Stacks]
		BTN - 20BB
		Hero (UTG) - 100BB A♠A♥
		UTG+1 - 80BB
		MP - 40BB

		[Preflop]
		Hero (UTG) opens to 2.5BB
		UTG+1 calls 2.5BB
		MP calls 2.5BB
		BTN raises to 20BB (all-in)
		Hero (UTG) calls 17.5BB
		UTG+1 calls 17.5BB
		MP calls 17.5BB

		→ Pot: 82.5BB (4-way)

		[Flop] K♠ 9♥ 3♦
		Hero (UTG) bets 20BB
		UTG+1 calls 20BB
		MP folds
		→ Pot: 122.5BB (3-way)

		[Turn] 7♣
		UTG+1 bets 40BB (all-in)
		Hero (UTG) calls all-in for 40BB
		→ Pot: 202.5BB

		[River] 2♠
		→ Pot: 202.5BB

		[Pot]
		Main: 82.5BB (3-way)
		Side 1: 120BB (Heads-up)

		[Showdown]
		Hero (UTG): A♠ A♥ — ONE PAIR [Main WIN, Side 1 WIN]
		UTG+1: K♥ Q♥ — ONE PAIR [LOSE]
		BTN: J♦ 10♦ — HIGH CARD [LOSE]

		[Result] +122.5BB
		BTN: 0BB
		Hero (UTG): 222.5BB
		UTG+1: 0BB
		MP: 20BB
		""",

		)
	}

	@Test
	fun `언콜 베팅 반환`() {
		val board =
			listOf(
				c(Rank.ACE, Suit.SPADES),
				c(Rank.KING, Suit.DIAMONDS),
				c(Rank.SEVEN, Suit.CLUBS),
				c(Rank.TWO, Suit.HEARTS),
				c(Rank.THREE, Suit.CLUBS),
			)
		val e =
			listOf(
				ShowdownEntry(5, p(c(Rank.ACE, Suit.HEARTS), c(Rank.ACE, Suit.DIAMONDS))),
				ShowdownEntry(6, p(c(Rank.KING, Suit.SPADES), c(Rank.KING, Suit.HEARTS))),
			)
		assertFmt(
			HandRecord(
				id = "e2", tableId = "t1", createdAt = 0L, blinds = ante, heroHand = e[0].cards, heroSeat = 5, heroStack = 50000.0, buttonSeat = 4,
				streets = HandStreets(
					preflop = PreflopStreet(
						listOf(
							Action(1, ActionType.FOLD),
							Action(2, ActionType.FOLD),
							Action(3, ActionType.FOLD),
							Action(4, ActionType.FOLD),
							Action(5, ActionType.ALL_IN, 49000.0, 50000.0),
							Action(6, ActionType.CALL, 29000.0, 29000.0),
						),
					),
					flop = FlopStreet(board[0], board[1], board[2]),
					turn = TurnStreet(board[3]),
					river = RiverStreet(board[4]),
				),
				showdown = e, showdownResults = sd(board, e), result = 29000.0,
			),
			"""
		[Stacks]
		Hero (SB) - 50BB A♥A♦
		BB - 29BB

		[Preflop]
		Hero (SB) bets 49BB (all-in)
		BB calls all-in for 28BB

		→ Pot: 59BB (Heads-up)

		[Flop] A♠ K♦ 7♣
		→ Pot: 59BB (Heads-up)

		[Turn] 2♥
		→ Pot: 59BB

		[River] 3♣
		→ Pot: 59BB

		[Showdown]
		Hero (SB): A♥ A♦ — THREE OF A KIND [WIN]
		BB: K♠ K♥ — THREE OF A KIND [LOSE]

		[Result] +30BB
		Hero (SB): 80BB
		BB: 0BB
		""",
		)
	}

	@Test
	fun `사이드팟 다른 승자 - 22 메인팟 AA 사이드팟`() {
		val board =
			listOf(
				c(Rank.TWO, Suit.HEARTS),
				c(Rank.FIVE, Suit.CLUBS),
				c(Rank.EIGHT, Suit.DIAMONDS),
				c(Rank.JACK, Suit.SPADES),
				c(Rank.QUEEN, Suit.CLUBS),
			)
		val e =
			listOf(
				ShowdownEntry(4, p(c(Rank.ACE, Suit.SPADES), c(Rank.ACE, Suit.HEARTS))),
				ShowdownEntry(5, p(c(Rank.KING, Suit.SPADES), c(Rank.KING, Suit.HEARTS))),
				ShowdownEntry(6, p(c(Rank.TWO, Suit.DIAMONDS), c(Rank.TWO, Suit.CLUBS))),
			)
		assertFmt(
			HandRecord(
				id = "e3", tableId = "t1", createdAt = 0L, blinds = ante, heroHand = e[0].cards, heroSeat = 4, heroStack = 100000.0, buttonSeat = 1,
				streets = HandStreets(
					preflop = PreflopStreet(
						listOf(
							Action(
								4,
								ActionType.RAISE,
								2500.0,
								100000.0,
								betLevel = 2,
							),
							Action(
								5,
								ActionType.RAISE,
								8000.0,
								50000.0,
								betLevel = 3,
							),
							Action(
								6,
								ActionType.ALL_IN,
								20000.0,
								20000.0,
							),
							Action(
								7,
								ActionType.FOLD,
							),
							Action(
								8,
								ActionType.FOLD,
							),
							Action(
								9,
								ActionType.FOLD,
							),
							Action(
								1,
								ActionType.FOLD,
							),
							Action(
								2,
								ActionType.FOLD,
							),
							Action(
								3,
								ActionType.FOLD,
							),
							Action(
								4,
								ActionType.RAISE,
								50000.0,
								97500.0,
								betLevel = 4,
							),
							Action(5, ActionType.CALL, 50000.0, 42000.0),
						),
					),
					flop = FlopStreet(board[0], board[1], board[2]),
					turn = TurnStreet(board[3]),
					river = RiverStreet(board[4]),
				),
				showdown = e, showdownResults = sd(board, e), result = 30000.0,
			),
			"""
		[Stacks]
		Hero (UTG) - 100BB A♠A♥
		UTG+1 - 50BB
		MP - 20BB

		[Preflop]
		Hero (UTG) opens to 2.5BB
		UTG+1 3-bets to 8BB
		MP raises to 20BB (all-in)
		Hero (UTG) 4-bets to 50BB
		UTG+1 calls all-in for 42BB

		→ Pot: 122.5BB (3-way)

		[Flop] 2♥ 5♣ 8♦
		→ Pot: 122.5BB (3-way)

		[Turn] J♠
		→ Pot: 122.5BB

		[River] Q♣
		→ Pot: 122.5BB

		[Pot]
		Main: 62.5BB (3-way)
		Side 1: 60BB (Heads-up)

		[Showdown]
		Hero (UTG): A♠ A♥ — ONE PAIR [Side 1 WIN]
		UTG+1: K♠ K♥ — ONE PAIR [LOSE]
		MP: 2♦ 2♣ — THREE OF A KIND [Main WIN]

		[Result] +10BB
		Hero (UTG): 110BB
		UTG+1: 0BB
		MP: 62.5BB
		""",
		)
	}

	@Test
	fun `스플릿 + 사이드팟 - AK vs AK vs QQ`() {
		val board =
			listOf(
				c(Rank.ACE, Suit.DIAMONDS),
				c(Rank.KING, Suit.CLUBS),
				c(Rank.SEVEN, Suit.HEARTS),
				c(Rank.FOUR, Suit.SPADES),
				c(Rank.THREE, Suit.DIAMONDS),
			)
		val e =
			listOf(
				ShowdownEntry(4, p(c(Rank.ACE, Suit.SPADES), c(Rank.KING, Suit.HEARTS))),
				ShowdownEntry(5, p(c(Rank.ACE, Suit.HEARTS), c(Rank.KING, Suit.DIAMONDS))),
				ShowdownEntry(6, p(c(Rank.QUEEN, Suit.HEARTS), c(Rank.QUEEN, Suit.SPADES))),
			)
		assertFmt(
			HandRecord(
				id = "e4", tableId = "t1", createdAt = 0L, blinds = ante, heroHand = e[0].cards, heroSeat = 4, heroStack = 100000.0, buttonSeat = 1,
				streets = HandStreets(
					preflop = PreflopStreet(
						listOf(
							Action(
								4,
								ActionType.RAISE,
								2500.0,
								100000.0,
								betLevel = 2,
							),
							Action(
								5,
								ActionType.RAISE,
								8000.0,
								100000.0,
								betLevel = 3,
							),
							Action(
								6,
								ActionType.ALL_IN,
								50000.0,
								50000.0,
							),
							Action(
								7,
								ActionType.FOLD,
							),
							Action(
								8,
								ActionType.FOLD,
							),
							Action(
								9,
								ActionType.FOLD,
							),
							Action(
								1,
								ActionType.FOLD,
							),
							Action(
								2,
								ActionType.FOLD,
							),
							Action(
								3,
								ActionType.FOLD,
							),
							Action(
								4,
								ActionType.ALL_IN,
								100000.0,
								97500.0,
							),
							Action(5, ActionType.CALL, 100000.0, 92000.0),
						),
					),
					flop = FlopStreet(board[0], board[1], board[2]),
					turn = TurnStreet(board[3]),
					river = RiverStreet(board[4]),
				),
				showdown = e, showdownResults = sd(board, e), result = 0.0,
			),
			"""
		[Stacks]
		Hero (UTG) - 100BB A♠K♥
		UTG+1 - 100BB
		MP - 50BB

		[Preflop]
		Hero (UTG) opens to 2.5BB
		UTG+1 3-bets to 8BB
		MP raises to 50BB (all-in)
		Hero (UTG) raises to 100BB (all-in)
		UTG+1 calls all-in for 92BB

		→ Pot: 252.5BB (3-way)

		[Flop] A♦ K♣ 7♥
		→ Pot: 252.5BB (3-way)

		[Turn] 4♠
		→ Pot: 252.5BB

		[River] 3♦
		→ Pot: 252.5BB

		[Pot]
		Main: 152.5BB (3-way)
		Side 1: 100BB (Heads-up)

		[Showdown]
		Hero (UTG): A♠ K♥ — TWO PAIR [Main SPLIT, Side 1 SPLIT]
		UTG+1: A♥ K♦ — TWO PAIR [Main SPLIT, Side 1 SPLIT]
		MP: Q♥ Q♠ — ONE PAIR [LOSE]

		[Result] +26.25BB
		Hero (UTG): 126.25BB
		UTG+1: 126.25BB
		MP: 0BB
		""",
		)
	}

	@Test
	fun `스플릿 + 사이드팟 - AK vs AK vs QQ (UTG+1 50BB, MP 80BB)`() {
		val board =
			listOf(
				c(Rank.ACE, Suit.DIAMONDS),
				c(Rank.KING, Suit.CLUBS),
				c(Rank.SEVEN, Suit.HEARTS),
				c(Rank.FOUR, Suit.SPADES),
				c(Rank.THREE, Suit.DIAMONDS),
			)
		val e =
			listOf(
				ShowdownEntry(4, p(c(Rank.ACE, Suit.SPADES), c(Rank.KING, Suit.HEARTS))),
				ShowdownEntry(5, p(c(Rank.ACE, Suit.HEARTS), c(Rank.KING, Suit.DIAMONDS))),
				ShowdownEntry(6, p(c(Rank.QUEEN, Suit.HEARTS), c(Rank.QUEEN, Suit.SPADES))),
			)
		assertFmt(
			HandRecord(
				id = "e10", tableId = "t1", createdAt = 0L, blinds = ante,
				heroHand = e[0].cards, heroSeat = 4, heroStack = 100000.0, buttonSeat = 1,
				streets = HandStreets(
					preflop = PreflopStreet(
						listOf(
							Action(4, ActionType.RAISE, 2500.0, 100000.0, betLevel = 2),
							Action(5, ActionType.RAISE, 8000.0, 50000.0, betLevel = 3),
							Action(6, ActionType.ALL_IN, 80000.0, 80000.0),
							Action(7, ActionType.FOLD),
							Action(8, ActionType.FOLD),
							Action(9, ActionType.FOLD),
							Action(1, ActionType.FOLD),
							Action(2, ActionType.FOLD),
							Action(3, ActionType.FOLD),
							Action(4, ActionType.ALL_IN, 100000.0, 97500.0),
							Action(5, ActionType.CALL, 50000.0, 42000.0),
						),
					),
					flop = FlopStreet(board[0], board[1], board[2]),
					turn = TurnStreet(board[3]),
					river = RiverStreet(board[4]),
				),
				showdown = e, showdownResults = sd(board, e), result = 56250.0,
			),
			"""
		[Stacks]
		Hero (UTG) - 100BB A♠K♥
		UTG+1 - 50BB
		MP - 80BB

		[Preflop]
		Hero (UTG) opens to 2.5BB
		UTG+1 3-bets to 8BB
		MP raises to 80BB (all-in)
		Hero (UTG) raises to 100BB (all-in)
		UTG+1 calls all-in for 42BB

		→ Pot: 212.5BB (3-way)

		[Flop] A♦ K♣ 7♥
		→ Pot: 212.5BB (3-way)

		[Turn] 4♠
		→ Pot: 212.5BB

		[River] 3♦
		→ Pot: 212.5BB

		[Pot]
		Main: 152.5BB (3-way)
		Side 1: 60BB (Heads-up)

		[Showdown]
		Hero (UTG): A♠ K♥ — TWO PAIR [Main SPLIT, Side 1 WIN]
		UTG+1: A♥ K♦ — TWO PAIR [Main SPLIT]
		MP: Q♥ Q♠ — ONE PAIR [LOSE]

		[Result] +56.25BB
		Hero (UTG): 156.25BB
		UTG+1: 76.25BB
		MP: 0BB
		""",
		)
	}

	@Test
	fun `BB앤티 단순 폴드 - SB오픈 BB폴드`() {
		assertFmt(
			HandRecord(
				id = "e5", tableId = "t1", createdAt = 0L, blinds = ante,
				heroHand = p(
					c(Rank.ACE, Suit.SPADES),
					c(Rank.KING, Suit.HEARTS),
				),
				heroSeat = 5, heroStack = 50000.0, buttonSeat = 4,
				streets = HandStreets(
					preflop = PreflopStreet(
						listOf(
							Action(1, ActionType.FOLD),
							Action(2, ActionType.FOLD),
							Action(3, ActionType.FOLD),
							Action(4, ActionType.FOLD),
							Action(5, ActionType.RAISE, 2500.0, 50000.0, betLevel = 2),
							Action(6, ActionType.FOLD),
						),
					),
				),
				result = 2500.0,
			),
			"""
		[Stacks]
		Hero (SB) - 50BB A♠K♥

		[Preflop]
		Hero (SB) opens to 2.5BB

		→ Pot: 4.5BB (Heads-up)

		[Result] +2BB
		Hero (SB): 52BB
		""",
		)
	}

	@Test
	fun `리버 올인 폴드 - 쇼다운 없음`() {
		assertFmt(
			HandRecord(
				id = "e6", tableId = "t1", createdAt = 0L, blinds = ante,
				heroHand = p(
					c(Rank.SEVEN, Suit.HEARTS),
					c(Rank.TWO, Suit.HEARTS),
				),
				heroSeat = 5, heroStack = 50000.0, buttonSeat = 4,
				streets = HandStreets(
					preflop = PreflopStreet(
						listOf(
							Action(1, ActionType.FOLD),
							Action(2, ActionType.FOLD),
							Action(3, ActionType.FOLD),
							Action(4, ActionType.FOLD),
							Action(5, ActionType.CALL, 1000.0, 50000.0),
							Action(6, ActionType.CHECK),
						),
					),
					flop = FlopStreet(
						c(Rank.ACE, Suit.SPADES),
						c(Rank.KING, Suit.DIAMONDS),
						c(Rank.NINE, Suit.CLUBS),
						listOf(Action(5, ActionType.CHECK), Action(6, ActionType.CHECK)),
					),
					turn = TurnStreet(
						c(Rank.FOUR, Suit.HEARTS),
						listOf(Action(5, ActionType.CHECK), Action(6, ActionType.CHECK)),
					),
					river = RiverStreet(
						c(Rank.THREE, Suit.DIAMONDS),
						listOf(Action(5, ActionType.ALL_IN, 49000.0, 49000.0), Action(6, ActionType.FOLD)),
					),
				),
				result = 2000.0,
			),
			"""
		[Stacks]
		Hero (SB) - 50BB 7♥2♥

		[Preflop]
		Hero (SB) calls 0.5BB
		BB checks

		→ Pot: 3BB (Heads-up)

		[Flop] A♠ K♦ 9♣
		Hero (SB) checks
		BB checks
		→ Pot: 3BB (Heads-up)

		[Turn] 4♥
		Hero (SB) checks
		BB checks
		→ Pot: 3BB

		[River] 3♦
		Hero (SB) bets 49BB (all-in)
		BB folds
		→ Pot: 52BB

		[Result] +2BB
		Hero (SB): 52BB
		""",
		)
	}

	@Test
	fun `일부 카드 미공개 muck`() {
		val board =
			listOf(
				c(Rank.ACE, Suit.HEARTS),
				c(Rank.TEN, Suit.DIAMONDS),
				c(Rank.SEVEN, Suit.CLUBS),
				c(Rank.KING, Suit.HEARTS),
				c(Rank.TWO, Suit.CLUBS),
			)
		val e =
			listOf(
				ShowdownEntry(4, p(c(Rank.ACE, Suit.SPADES), c(Rank.KING, Suit.SPADES))),
				ShowdownEntry(5, p(c(Rank.QUEEN, Suit.HEARTS), c(Rank.JACK, Suit.HEARTS))),
			)
		assertFmt(
			HandRecord(
				id = "e7", tableId = "t1", createdAt = 0L, blinds = ante, heroHand = e[0].cards, heroSeat = 4, heroStack = 50000.0, buttonSeat = 1,
				streets = HandStreets(
					preflop = PreflopStreet(
						listOf(
							Action(
								4,
								ActionType.RAISE,
								2500.0,
								50000.0,
								betLevel = 2,
							),
							Action(
								5,
								ActionType.CALL,
								2500.0,
								50000.0,
							),
							Action(
								6,
								ActionType.CALL,
								2500.0,
								50000.0,
							),
							Action(
								7,
								ActionType.FOLD,
							),
							Action(
								8,
								ActionType.FOLD,
							),
							Action(
								9,
								ActionType.FOLD,
							),
							Action(1, ActionType.FOLD), Action(2, ActionType.FOLD), Action(3, ActionType.FOLD),
						),
					),
					flop = FlopStreet(
						board[0],
						board[1],
						board[2],
						listOf(
							Action(4, ActionType.BET, 5000.0, 47500.0, betLevel = 1),
							Action(5, ActionType.CALL, 5000.0, 47500.0),
							Action(6, ActionType.CALL, 5000.0, 47500.0),
						),
					),
					turn = TurnStreet(
						board[3],
						listOf(
							Action(4, ActionType.BET, 15000.0, 42500.0, betLevel = 1),
							Action(5, ActionType.CALL, 15000.0, 42500.0),
							Action(6, ActionType.FOLD),
						),
					),
					river = RiverStreet(
						board[4],
						listOf(Action(4, ActionType.CHECK), Action(5, ActionType.CHECK)),
					),
				),
				showdown = e, showdownResults = sd(board, e), result = 42500.0,
			),
			"""
		[Stacks]
		Hero (UTG) - 50BB A♠K♠
		UTG+1 - 50BB
		MP - 50BB

		[Preflop]
		Hero (UTG) opens to 2.5BB
		UTG+1 calls 2.5BB
		MP calls 2.5BB

		→ Pot: 10BB (3-way)

		[Flop] A♥ 10♦ 7♣
		Hero (UTG) bets 5BB
		UTG+1 calls 5BB
		MP calls 5BB
		→ Pot: 25BB (3-way)

		[Turn] K♥
		Hero (UTG) bets 15BB
		UTG+1 calls 15BB
		MP folds
		→ Pot: 55BB

		[River] 2♣
		Hero (UTG) checks
		UTG+1 checks
		→ Pot: 55BB

		[Showdown]
		Hero (UTG): A♠ K♠ — TWO PAIR [LOSE]
		UTG+1: Q♥ J♥ — STRAIGHT [WIN]

		[Result] -22.5BB
		Hero (UTG): 27.5BB
		UTG+1: 82.5BB
		MP: 42.5BB
		""",
		)
	}

	@Test
	fun `헤즈업 BTN포지션 검증`() {
		val board =
			listOf(
				c(Rank.ACE, Suit.HEARTS),
				c(Rank.TEN, Suit.DIAMONDS),
				c(Rank.SEVEN, Suit.CLUBS),
				c(Rank.FOUR, Suit.SPADES),
				c(Rank.TWO, Suit.CLUBS),
			)
		val e =
			listOf(
				ShowdownEntry(1, p(c(Rank.ACE, Suit.SPADES), c(Rank.KING, Suit.HEARTS))),
				ShowdownEntry(2, p(c(Rank.KING, Suit.DIAMONDS), c(Rank.QUEEN, Suit.DIAMONDS))),
			)
		assertFmt(
			HandRecord(
				id = "e8", tableId = "t1", createdAt = 0L, blinds = ante, heroHand = e[0].cards, heroSeat = 1, heroStack = 50000.0, buttonSeat = 1,
				streets = HandStreets(
					preflop = PreflopStreet(
						listOf(
							Action(1, ActionType.RAISE, 3000.0, 50000.0, betLevel = 2),
							Action(2, ActionType.CALL, 3000.0, 49000.0),
						),
					),
					flop = FlopStreet(
						board[0],
						board[1],
						board[2],
						listOf(
							Action(2, ActionType.CHECK),
							Action(1, ActionType.BET, 4000.0, 47000.0, betLevel = 1),
							Action(2, ActionType.CALL, 4000.0, 46000.0),
						),
					),
					turn = TurnStreet(board[3], listOf(Action(2, ActionType.CHECK), Action(1, ActionType.CHECK))),
					river = RiverStreet(
						board[4],
						listOf(Action(2, ActionType.CHECK), Action(1, ActionType.CHECK)),
					),
				),
				showdown = e, showdownResults = sd(board, e), result = 7000.0,
			),
			"""
		[Stacks]
		Hero (BTN) - 50BB A♠K♥
		SB - 49BB

		[Preflop]
		Hero (BTN) opens to 3BB
		SB calls 2.5BB

		→ Pot: 7BB (Heads-up)

		[Flop] A♥ 10♦ 7♣
		SB checks
		Hero (BTN) bets 4BB
		SB calls 4BB
		→ Pot: 15BB (Heads-up)

		[Turn] 4♠
		SB checks
		Hero (BTN) checks
		→ Pot: 15BB

		[River] 2♣
		SB checks
		Hero (BTN) checks
		→ Pot: 15BB

		[Showdown]
		Hero (BTN): A♠ K♥ — ONE PAIR [WIN]
		SB: K♦ Q♦ — HIGH CARD [LOSE]

		[Result] +8BB
		Hero (BTN): 58BB
		SB: 42BB
		""",
		)
	}

	@Test
	fun `프리플랍 올인 후 체크다운`() {
		val board =
			listOf(
				c(Rank.ACE, Suit.HEARTS),
				c(Rank.TEN, Suit.DIAMONDS),
				c(Rank.SEVEN, Suit.CLUBS),
				c(Rank.FOUR, Suit.SPADES),
				c(Rank.TWO, Suit.CLUBS),
			)
		val e =
			listOf(
				ShowdownEntry(1, p(c(Rank.ACE, Suit.SPADES), c(Rank.ACE, Suit.HEARTS))),
				ShowdownEntry(2, p(c(Rank.KING, Suit.SPADES), c(Rank.KING, Suit.HEARTS))),
			)
		assertFmt(
			HandRecord(
				id = "e9", tableId = "t1", createdAt = 0L, blinds = ante, heroHand = e[0].cards, heroSeat = 1, heroStack = 30000.0, buttonSeat = 1,
				streets = HandStreets(
					preflop = PreflopStreet(
						listOf(
							Action(1, ActionType.ALL_IN, 30000.0, 30000.0),
							Action(2, ActionType.CALL, 29000.0, 29000.0),
						),
					),
					flop = FlopStreet(board[0], board[1], board[2]),
					turn = TurnStreet(board[3]),
					river = RiverStreet(board[4]),
				),
				showdown = e, showdownResults = sd(board, e), result = 29000.0,
			),
			"""
		[Stacks]
		Hero (BTN) - 30BB A♠A♥
		SB - 29BB

		[Preflop]
		Hero (BTN) bets 30BB (all-in)
		SB calls all-in for 28.5BB

		→ Pot: 59BB (Heads-up)

		[Flop] A♥ 10♦ 7♣
		→ Pot: 59BB (Heads-up)

		[Turn] 4♠
		→ Pot: 59BB

		[River] 2♣
		→ Pot: 59BB

		[Showdown]
		Hero (BTN): A♠ A♥ — THREE OF A KIND [WIN]
		SB: K♠ K♥ — ONE PAIR [LOSE]

		[Result] +30BB
		Hero (BTN): 60BB
		SB: 0BB
		""",
		)
	}
}

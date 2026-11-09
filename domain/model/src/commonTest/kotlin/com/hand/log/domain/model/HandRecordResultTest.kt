package com.hand.log.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class HandRecordResultTest {

	private val highCardWins: (List<Card>, List<ShowdownEntry>) -> List<ShowdownResult> = { _, entries ->
		fun top(entry: ShowdownEntry) = minOf(entry.card1.rank.ordinal, entry.card2.rank.ordinal)
		val best = entries.minOf { top(it) }
		val winnerCount = entries.count { top(it) == best }
		entries.map {
			ShowdownResult(
				seat = it.seat,
				ranking = HandRanking.HIGH_CARD,
				outcome = when {
					top(it) != best -> ShowdownOutcome.LOSE
					winnerCount > 1 -> ShowdownOutcome.SPLIT
					else -> ShowdownOutcome.WIN
				},
			)
		}
	}

	private val board = listOf(
		Card(Rank.NINE, Suit.HEARTS),
		Card(Rank.SEVEN, Suit.CLUBS),
		Card(Rank.FIVE, Suit.DIAMONDS),
		Card(Rank.FOUR, Suit.SPADES),
		Card(Rank.TWO, Suit.HEARTS),
	)

	private fun pocket(r1: Rank, r2: Rank) = PocketCards(Card(r1, Suit.SPADES), Card(r2, Suit.CLUBS))

	private fun hand(
		blinds: Blinds = Blinds(sb = 1.0, bb = 2.0),
		heroSeat: Int = 1,
		streets: HandStreets,
		players: List<HandPlayer> = emptyList(),
		result: Double? = null,
	) = HandRecord(
		id = "h",
		tableId = "t1",
		createdAt = 0L,
		blinds = blinds,
		heroSeat = heroSeat,
		buttonSeat = 1,
		streets = streets,
		players = players,
		result = result,
	)

	@Test
	fun `BB가 프리플랍에 체크하고 플랍에 베팅해도 BB 블라인드가 투입액에 남는다`() {
		val record = hand(
			streets = HandStreets(
				preflop = PreflopStreet(
					listOf(
						Action(1, ActionType.CALL, 2.0),
						Action(2, ActionType.CALL, 2.0),
						Action(3, ActionType.CHECK),
					),
				),
				flop = FlopStreet(
					board[0],
					board[1],
					board[2],
					listOf(
						Action(2, ActionType.CHECK),
						Action(3, ActionType.BET, 4.0),
						Action(1, ActionType.FOLD),
						Action(2, ActionType.FOLD),
					),
				),
			),
		)

		assertEquals(6.0, record.seatInvestments[3])
	}

	@Test
	fun `BB가 프리플랍 체크 후 베팅하고 쇼다운에서 지면 블라인드까지 잃고 팟은 13이다`() {
		val record = hand(
			streets = HandStreets(
				preflop = PreflopStreet(
					listOf(
						Action(1, ActionType.CALL, 2.0),
						Action(2, ActionType.FOLD),
						Action(3, ActionType.CHECK),
					),
				),
				flop = FlopStreet(
					board[0],
					board[1],
					board[2],
					listOf(Action(3, ActionType.BET, 4.0), Action(1, ActionType.CALL, 4.0)),
				),
				turn = TurnStreet(board[3], listOf(Action(3, ActionType.CHECK), Action(1, ActionType.CHECK))),
				river = RiverStreet(board[4], listOf(Action(3, ActionType.CHECK), Action(1, ActionType.CHECK))),
			),
			players = listOf(
				HandPlayer(seat = 1, cards = pocket(Rank.ACE, Rank.ACE), initialStack = 100.0, isHero = true),
				HandPlayer(seat = 2, initialStack = 100.0),
				HandPlayer(seat = 3, cards = pocket(Rank.KING, Rank.THREE), initialStack = 100.0),
			),
		)

		assertEquals(13.0, record.getPotAtStreet(Street.RIVER))
		assertEquals(mapOf(1 to 107.0, 2 to 99.0, 3 to 94.0), record.getFinalStacks(highCardWins))
	}

	@Test
	fun `SB 림프와 BB 체크로 쇼다운까지 가면 프리플랍 팟은 두 블라인드 합이다`() {
		val record = hand(
			streets = HandStreets(
				preflop = PreflopStreet(
					listOf(
						Action(1, ActionType.FOLD),
						Action(2, ActionType.CALL, 2.0),
						Action(3, ActionType.CHECK),
					),
				),
				flop = FlopStreet(board[0], board[1], board[2]),
				turn = TurnStreet(board[3]),
				river = RiverStreet(board[4]),
			),
		)

		assertEquals(4.0, record.getPotAtStreet(Street.PREFLOP))
	}

	@Test
	fun `BB앤티로 모두 폴드해 BB가 이기면 앤티를 낸 BB의 순수익은 SB만큼이다`() {
		val record = hand(
			blinds = Blinds(sb = 500.0, bb = 1000.0, isBigBlindAnte = true),
			heroSeat = 3,
			streets = HandStreets(
				preflop = PreflopStreet(listOf(Action(1, ActionType.FOLD), Action(2, ActionType.FOLD))),
			),
			players = listOf(
				HandPlayer(seat = 1, initialStack = 100000.0),
				HandPlayer(seat = 2, initialStack = 100000.0),
				HandPlayer(seat = 3, initialStack = 100000.0, isHero = true),
			),
		)

		assertEquals(2500.0, record.getPotAtStreet(Street.PREFLOP))
		assertEquals(
			mapOf(1 to 100000.0, 2 to 99500.0, 3 to 100500.0),
			record.getFinalStacks(highCardWins),
		)
	}

	@Test
	fun `액션 로그로만 스택을 아는 BB는 앤티를 뺀 스택에 앤티를 더해 시작 스택으로 본다`() {
		val record = hand(
			blinds = Blinds(sb = 500.0, bb = 1000.0, isBigBlindAnte = true),
			streets = HandStreets(
				preflop = PreflopStreet(
					listOf(
						Action(1, ActionType.RAISE, 3000.0, stackBefore = 100000.0, betLevel = 2),
						Action(2, ActionType.FOLD, stackBefore = 100000.0),
						Action(3, ActionType.FOLD, stackBefore = 99000.0),
					),
				),
			),
		)

		assertEquals(100000.0, record.getInitialStack(3))
		assertEquals(98000.0, record.getFinalStacks(highCardWins)[3])
		assertEquals(102500.0, record.getFinalStacks(highCardWins)[1])
	}

	@Test
	fun `쇼다운에서 진 히어로는 결과 금액이 없어도 패배로 표시된다`() {
		val streets = HandStreets(
			preflop = PreflopStreet(
				listOf(
					Action(1, ActionType.RAISE, 6.0, betLevel = 2),
					Action(2, ActionType.FOLD),
					Action(3, ActionType.CALL, 6.0),
				),
			),
			flop = FlopStreet(board[0], board[1], board[2]),
			turn = TurnStreet(board[3]),
			river = RiverStreet(board[4]),
		)
		val players = listOf(
			HandPlayer(
				seat = 1,
				cards = pocket(Rank.THREE, Rank.TWO),
				outcome = ShowdownOutcome.LOSE,
				isHero = true,
			),
			HandPlayer(seat = 3, cards = pocket(Rank.ACE, Rank.KING), outcome = ShowdownOutcome.WIN),
		)

		listOf(null, 0.0).forEach { result ->
			val record = hand(streets = streets, players = players, result = result)
			assertEquals(HeroResultType.SHOWDOWN_LOSE, record.resolvedHeroResultType)
			assertEquals(HeroOutcome.LOSE, record.heroOutcome)
		}
	}

	@Test
	fun `폴드한 히어로는 다른 플레이어가 폴드로 이겨도 폴드 패배다`() {
		val record = hand(
			heroSeat = 4,
			streets = HandStreets(
				preflop = PreflopStreet(
					listOf(
						Action(4, ActionType.FOLD),
						Action(1, ActionType.RAISE, 5.0, betLevel = 2),
						Action(2, ActionType.FOLD),
						Action(3, ActionType.FOLD),
					),
				),
			),
			players = listOf(HandPlayer(seat = 4, isHero = true)),
			result = 0.0,
		)

		assertEquals(HeroResultType.FOLD_LOSE, record.resolvedHeroResultType)
		assertEquals(HeroOutcome.LOSE, record.heroOutcome)
		assertEquals(setOf(1), record.winnerSeats)
	}

	@Test
	fun `폴드한 히어로는 다른 둘이 쇼다운해도 승자가 아니고 폴드 패배다`() {
		val record = hand(
			heroSeat = 4,
			streets = HandStreets(
				preflop = PreflopStreet(
					listOf(
						Action(4, ActionType.FOLD),
						Action(1, ActionType.CALL, 2.0),
						Action(2, ActionType.FOLD),
						Action(3, ActionType.CHECK),
					),
				),
				flop = FlopStreet(board[0], board[1], board[2]),
				turn = TurnStreet(board[3]),
				river = RiverStreet(board[4]),
			),
			players = listOf(HandPlayer(seat = 4, isHero = true)),
			result = 0.0,
		)

		assertEquals(HeroResultType.FOLD_LOSE, record.resolvedHeroResultType)
		assertEquals(HeroOutcome.LOSE, record.heroOutcome)
		assertTrue(4 !in record.winnerSeats)
	}

	@Test
	fun `혼자 남은 히어로는 폴드 승리다`() {
		val record = hand(
			streets = HandStreets(
				preflop = PreflopStreet(
					listOf(
						Action(1, ActionType.RAISE, 5.0, betLevel = 2),
						Action(2, ActionType.FOLD),
						Action(3, ActionType.FOLD),
					),
				),
			),
			players = listOf(HandPlayer(seat = 1, isHero = true)),
			result = 3.0,
		)

		assertEquals(HeroResultType.FOLD_WIN, record.resolvedHeroResultType)
		assertEquals(HeroOutcome.WIN, record.heroOutcome)
	}

	@Test
	fun `헤즈업은 버튼이 SB이고 프리플랍에 먼저 포스트플랍에 나중에 액션한다`() {
		val ring = SeatRing(listOf(1, 2), buttonSeat = 1)

		assertEquals(1, ring.sbSeat)
		assertEquals(2, ring.bbSeat)
		assertEquals(listOf(1, 2), ring.preflopOrder)
		assertEquals(listOf(2, 1), ring.postflopOrder)
		assertEquals(Position.BTN, ring.positionOf(1))
		assertEquals(Position.BB, ring.positionOf(2))
		assertEquals(Position.forPlayerCount(2), ring.preflopOrder.map { ring.positionOf(it) })
	}

	@Test
	fun `헤즈업에서 BTN이 3BB로 레이즈하고 BB가 폴드하면 팟은 4BB이고 BTN은 1BB를 번다`() {
		val record = hand(
			blinds = Blinds(sb = 0.5, bb = 1.0),
			streets = HandStreets(
				preflop = PreflopStreet(
					listOf(Action(1, ActionType.RAISE, 3.0, betLevel = 2), Action(2, ActionType.FOLD)),
				),
			),
			players = listOf(
				HandPlayer(seat = 1, initialStack = 100.0, isHero = true),
				HandPlayer(seat = 2, initialStack = 100.0),
			),
		)

		assertEquals(4.0, record.getPotAtStreet(Street.PREFLOP))
		assertEquals(mapOf(1 to 101.0, 2 to 99.0), record.getFinalStacks(highCardWins))
	}

	private val showdownStreets = HandStreets(
		preflop = PreflopStreet(
			listOf(
				Action(1, ActionType.RAISE, 6.0, betLevel = 2),
				Action(2, ActionType.FOLD),
				Action(3, ActionType.CALL, 6.0),
			),
		),
		flop = FlopStreet(board[0], board[1], board[2]),
		turn = TurnStreet(board[3]),
		river = RiverStreet(board[4]),
	)

	private val heroLost = hand(
		streets = showdownStreets,
		players = listOf(
			HandPlayer(
				seat = 1,
				cards = pocket(Rank.THREE, Rank.TWO),
				initialStack = 100.0,
				ranking = HandRanking.HIGH_CARD,
				outcome = ShowdownOutcome.LOSE,
				isHero = true,
			),
			HandPlayer(seat = 2, initialStack = 100.0),
			HandPlayer(
				seat = 3,
				cards = pocket(Rank.KING, Rank.QUEEN),
				initialStack = 100.0,
				ranking = HandRanking.HIGH_CARD,
				outcome = ShowdownOutcome.WIN,
			),
		),
		result = -6.0,
	)

	private fun HandRecord.withCards(seat: Int, cards: PocketCards) =
		copy(players = players.map { if (it.seat == seat) it.copy(cards = cards) else it })

	@Test
	fun `카드를 바꾸면 쇼다운 승패와 결과 금액을 다시 계산한다`() {
		val resolved = heroLost.withCards(1, pocket(Rank.ACE, Rank.ACE)).resolveShowdown(highCardWins)

		assertEquals(ShowdownOutcome.WIN, resolved.players.single { it.seat == 1 }.outcome)
		assertEquals(ShowdownOutcome.LOSE, resolved.players.single { it.seat == 3 }.outcome)
		assertEquals(7.0, resolved.result)
		assertEquals(HeroResultType.SHOWDOWN_WIN, resolved.resolvedHeroResultType)
	}

	@Test
	fun `보드가 다 깔리지 않은 핸드는 카드를 바꾸면 이전 승패를 지운다`() {
		val partialBoard = heroLost.copy(streets = showdownStreets.copy(river = null))
		val resolved = partialBoard.withCards(1, pocket(Rank.ACE, Rank.ACE)).resolveShowdown(highCardWins)

		assertTrue(resolved.players.all { it.outcome == null && it.ranking == null })
	}

	@Test
	fun `결과 금액을 모르던 핸드는 카드를 바꿔도 결과 금액을 비워 둔다`() {
		val unknown = heroLost.copy(result = null)
		val resolved = unknown.withCards(1, pocket(Rank.ACE, Rank.ACE)).resolveShowdown(highCardWins)

		assertNull(resolved.result)
		assertEquals(HeroResultType.SHOWDOWN_WIN, resolved.resolvedHeroResultType)
	}
}

package com.hand.log.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HandRecordPotTest {

	private val bbAnte4000 = Blinds(sb = 2000.0, bb = 4000.0, isBigBlindAnte = true)

	@Test
	fun `BB앤티 4000-4000-2000 UTG 2BB레이즈 LJ BTN 콜 시 팟에 앤티 포함`() {
		val hand = HandRecord(
			id = "pot1",
			tableId = "t1",
			createdAt = 0L,
			blinds = bbAnte4000,
			heroSeat = 4,
			buttonSeat = 1,
			streets = HandStreets(
				preflop = PreflopStreet(
					actions = listOf(
						Action(playerSeat = 4, type = ActionType.RAISE, amount = 8000.0, betLevel = 2),
						Action(playerSeat = 5, type = ActionType.FOLD),
						Action(playerSeat = 6, type = ActionType.CALL, amount = 8000.0),
						Action(playerSeat = 7, type = ActionType.FOLD),
						Action(playerSeat = 8, type = ActionType.FOLD),
						Action(playerSeat = 1, type = ActionType.CALL, amount = 8000.0),
						Action(playerSeat = 2, type = ActionType.FOLD),
						Action(playerSeat = 3, type = ActionType.FOLD),
					),
				),
			),
		)

		val pot = hand.getPotAtStreet(Street.PREFLOP)
		assertEquals(34000.0, pot)
	}

	@Test
	fun `BB앤티 없을 때 팟에 앤티 미포함`() {
		val noAnte = Blinds(sb = 2000.0, bb = 4000.0, isBigBlindAnte = false)
		val hand = HandRecord(
			id = "pot2",
			tableId = "t1",
			createdAt = 0L,
			blinds = noAnte,
			heroSeat = 4,
			buttonSeat = 1,
			streets = HandStreets(
				preflop = PreflopStreet(
					actions = listOf(
						Action(playerSeat = 4, type = ActionType.RAISE, amount = 8000.0, betLevel = 2),
						Action(playerSeat = 5, type = ActionType.FOLD),
						Action(playerSeat = 6, type = ActionType.CALL, amount = 8000.0),
						Action(playerSeat = 7, type = ActionType.FOLD),
						Action(playerSeat = 8, type = ActionType.FOLD),
						Action(playerSeat = 1, type = ActionType.CALL, amount = 8000.0),
						Action(playerSeat = 2, type = ActionType.FOLD),
						Action(playerSeat = 3, type = ActionType.FOLD),
					),
				),
			),
		)

		val pot = hand.getPotAtStreet(Street.PREFLOP)
		assertEquals(30000.0, pot)
	}

	@Test
	fun `폴드 승리 핸드에서 isFoldWin이 true이면 패배자는 폴드 패배로 표시해야 한다`() {
		val hand = HandRecord(
			id = "fold1",
			tableId = "t1",
			createdAt = 0L,
			blinds = bbAnte4000,
			heroSeat = 4,
			buttonSeat = 1,
			streets = HandStreets(
				preflop = PreflopStreet(
					actions = listOf(
						Action(playerSeat = 4, type = ActionType.RAISE, amount = 8000.0, betLevel = 2),
						Action(playerSeat = 5, type = ActionType.FOLD),
						Action(playerSeat = 6, type = ActionType.FOLD),
						Action(playerSeat = 7, type = ActionType.FOLD),
						Action(playerSeat = 8, type = ActionType.FOLD),
						Action(playerSeat = 1, type = ActionType.FOLD),
						Action(playerSeat = 2, type = ActionType.FOLD),
						Action(playerSeat = 3, type = ActionType.FOLD),
					),
				),
			),
			players = listOf(
				HandPlayer(
					seat = 4,
					ranking = HandRanking.WIN_BY_FOLD,
					outcome = ShowdownOutcome.WIN,
					isHero = true,
				),
				HandPlayer(seat = 5, ranking = HandRanking.WIN_BY_FOLD, outcome = ShowdownOutcome.LOSE),
			),
		)

		assertTrue(hand.isFoldWin)
		assertEquals(HandRanking.WIN_BY_FOLD, hand.getShowdownResult(4)?.ranking)
		assertEquals(HandRanking.WIN_BY_FOLD, hand.getShowdownResult(5)?.ranking)
		assertEquals(ShowdownOutcome.WIN, hand.getShowdownResult(4)?.outcome)
		assertEquals(ShowdownOutcome.LOSE, hand.getShowdownResult(5)?.outcome)
	}

	@Test
	fun `폴드 패배 시 ranking은 FOLD이어야 한다`() {
		val hand = HandRecord(
			id = "fold2",
			tableId = "t1",
			createdAt = 0L,
			blinds = Blinds(sb = 500.0, bb = 1000.0),
			heroSeat = 3,
			buttonSeat = 1,
			streets = HandStreets(
				preflop = PreflopStreet(
					actions = listOf(
						Action(playerSeat = 1, type = ActionType.RAISE, amount = 2500.0, betLevel = 2),
						Action(playerSeat = 2, type = ActionType.FOLD),
						Action(playerSeat = 3, type = ActionType.FOLD),
					),
				),
			),
			players = listOf(
				HandPlayer(seat = 1, ranking = HandRanking.WIN_BY_FOLD, outcome = ShowdownOutcome.WIN),
				HandPlayer(seat = 2, ranking = HandRanking.WIN_BY_FOLD, outcome = ShowdownOutcome.LOSE),
				HandPlayer(
					seat = 3,
					ranking = HandRanking.WIN_BY_FOLD,
					outcome = ShowdownOutcome.LOSE,
					isHero = true,
				),
			),
		)

		assertTrue(hand.isFoldWin)
		val heroResult = hand.getShowdownResult(3)
		assertEquals(HandRanking.WIN_BY_FOLD, heroResult?.ranking)
		assertEquals(ShowdownOutcome.LOSE, heroResult?.outcome)
	}
}

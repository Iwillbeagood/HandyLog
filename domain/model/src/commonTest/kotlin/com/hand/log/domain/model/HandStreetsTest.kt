package com.hand.log.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class HandStreetsTest {

	private fun fullBoard(): HandStreets = HandStreets(
		preflop = PreflopStreet(
			actions = listOf(Action(playerSeat = 4, type = ActionType.RAISE, amount = 2500.0)),
		),
		flop = FlopStreet(
			card1 = Card(Rank.ACE, Suit.HEARTS),
			card2 = Card(Rank.KING, Suit.DIAMONDS),
			card3 = Card(Rank.QUEEN, Suit.CLUBS),
			actions = listOf(Action(playerSeat = 2, type = ActionType.BET, amount = 2000.0)),
		),
		turn = TurnStreet(
			card = Card(Rank.SEVEN, Suit.SPADES),
			actions = listOf(Action(playerSeat = 2, type = ActionType.CHECK)),
		),
		river = RiverStreet(
			card = Card(Rank.TWO, Suit.CLUBS),
			actions = listOf(Action(playerSeat = 2, type = ActionType.CHECK)),
		),
	)

	@Test
	fun `clearActionsAfter 프리플랍으로 돌아가도 보드 카드는 보존된다`() {
		val cleared = fullBoard().clearActionsAfter(Street.PREFLOP)

		// 보드 카드는 그대로 남아 스트릿 진입 시 재입력이 필요 없다
		assertTrue(cleared.isBoardReady(Street.FLOP))
		assertTrue(cleared.isBoardReady(Street.TURN))
		assertTrue(cleared.isBoardReady(Street.RIVER))
		assertEquals(5, cleared.boardCards.size)

		// 이후 스트릿의 액션만 제거된다
		assertTrue(cleared.getActions(Street.FLOP).isEmpty())
		assertTrue(cleared.getActions(Street.TURN).isEmpty())
		assertTrue(cleared.getActions(Street.RIVER).isEmpty())
	}

	@Test
	fun `clearActionsAfter 플랍으로 돌아가면 플랍 액션은 유지되고 이후만 제거된다`() {
		val cleared = fullBoard().clearActionsAfter(Street.FLOP)

		assertEquals(1, cleared.getActions(Street.FLOP).size)
		assertTrue(cleared.getActions(Street.TURN).isEmpty())
		assertTrue(cleared.getActions(Street.RIVER).isEmpty())
		assertEquals(5, cleared.boardCards.size)
	}

	@Test
	fun `clearAfter 는 이후 스트릿을 통째로 제거한다 - 대비용`() {
		val cleared = fullBoard().clearAfter(Street.PREFLOP)

		assertNull(cleared.flop)
		assertNull(cleared.turn)
		assertNull(cleared.river)
	}
}

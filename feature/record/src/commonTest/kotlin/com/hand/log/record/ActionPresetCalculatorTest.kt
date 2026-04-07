package com.hand.log.record

import com.hand.log.domain.model.Action
import com.hand.log.domain.model.ActionType
import com.hand.log.domain.model.Street
import com.hand.log.record.model.calculateLastRaiseTo
import kotlin.test.Test
import kotlin.test.assertEquals

class ActionPresetCalculatorTest {

	@Test
	fun `프리플랍 레이즈 후 숏스택 올인 - lastRaiseTo는 레이즈 금액`() {
		// BTN raises to 130,000, SB all-in for 100,000 (short stack)
		val actions = listOf(
			Action(playerSeat = 1, type = ActionType.RAISE, amount = 130000.0),
			Action(playerSeat = 2, type = ActionType.ALL_IN, amount = 100000.0),
		)
		val result = calculateLastRaiseTo(actions, Street.PREFLOP, bb = 10000.0)
		// SB의 숏스택 올인(100,000)은 BTN 레이즈(130,000)보다 작으므로 무시
		// x2 = 130,000 * 2 = 260,000이 되어야 함
		assertEquals(130000.0, result)
	}

	@Test
	fun `프리플랍 레이즈 후 오버올인 - lastRaiseTo는 올인 금액`() {
		// BTN raises to 130,000, SB all-in for 500,000
		val actions = listOf(
			Action(playerSeat = 1, type = ActionType.RAISE, amount = 130000.0),
			Action(playerSeat = 2, type = ActionType.ALL_IN, amount = 500000.0),
		)
		val result = calculateLastRaiseTo(actions, Street.PREFLOP, bb = 10000.0)
		assertEquals(500000.0, result)
	}

	@Test
	fun `프리플랍 오픈레이즈만 있을 때`() {
		val actions = listOf(
			Action(playerSeat = 4, type = ActionType.RAISE, amount = 2500.0),
		)
		val result = calculateLastRaiseTo(actions, Street.PREFLOP, bb = 1000.0)
		assertEquals(2500.0, result)
	}

	@Test
	fun `프리플랍 3벳 후 숏스택 올인 - lastRaiseTo는 3벳 금액`() {
		// UTG raises to 2500, CO 3-bets to 8000, BTN all-in for 5000 (short stack)
		val actions = listOf(
			Action(playerSeat = 4, type = ActionType.RAISE, amount = 2500.0),
			Action(playerSeat = 6, type = ActionType.RAISE, amount = 8000.0),
			Action(playerSeat = 1, type = ActionType.ALL_IN, amount = 5000.0),
		)
		val result = calculateLastRaiseTo(actions, Street.PREFLOP, bb = 1000.0)
		// BTN의 5000 올인은 CO의 8000보다 작으므로 무시
		assertEquals(8000.0, result)
	}

	@Test
	fun `프리플랍 액션 없으면 0`() {
		val actions = emptyList<Action>()
		val result = calculateLastRaiseTo(actions, Street.PREFLOP, bb = 1000.0)
		assertEquals(0.0, result)
	}

	@Test
	fun `프리플랍 폴드와 콜만 있으면 0`() {
		val actions = listOf(
			Action(playerSeat = 4, type = ActionType.FOLD),
			Action(playerSeat = 5, type = ActionType.CALL, amount = 1000.0),
		)
		val result = calculateLastRaiseTo(actions, Street.PREFLOP, bb = 1000.0)
		// CALL은 aggressive action이 아니므로 0
		assertEquals(0.0, result)
	}

	@Test
	fun `포스트플랍 벳 후 숏스택 올인 - lastRaiseTo는 벳 금액`() {
		// SB bets 5000, BB all-in for 3000 (short stack)
		val actions = listOf(
			Action(playerSeat = 2, type = ActionType.BET, amount = 5000.0),
			Action(playerSeat = 3, type = ActionType.ALL_IN, amount = 3000.0),
		)
		val result = calculateLastRaiseTo(actions, Street.FLOP, bb = 1000.0)
		assertEquals(5000.0, result)
	}

	@Test
	fun `포스트플랍 벳만 있을 때`() {
		val actions = listOf(
			Action(playerSeat = 2, type = ActionType.BET, amount = 3000.0),
		)
		val result = calculateLastRaiseTo(actions, Street.FLOP, bb = 1000.0)
		assertEquals(3000.0, result)
	}

	@Test
	fun `프리플랍 연속 레이즈 - 마지막 레이즈 금액`() {
		val actions = listOf(
			Action(playerSeat = 4, type = ActionType.RAISE, amount = 2500.0),
			Action(playerSeat = 5, type = ActionType.RAISE, amount = 8000.0),
			Action(playerSeat = 6, type = ActionType.RAISE, amount = 20000.0),
		)
		val result = calculateLastRaiseTo(actions, Street.PREFLOP, bb = 1000.0)
		assertEquals(20000.0, result)
	}

	@Test
	fun `프리플랍 레이즈 후 여러 숏스택 올인 - lastRaiseTo는 레이즈 금액`() {
		// BTN raises to 13,000,000, SB all-in for 1,000,000, BB deciding
		val actions = listOf(
			Action(playerSeat = 1, type = ActionType.RAISE, amount = 13000000.0),
			Action(playerSeat = 2, type = ActionType.ALL_IN, amount = 1000000.0),
		)
		val result = calculateLastRaiseTo(actions, Street.PREFLOP, bb = 1000000.0)
		assertEquals(13000000.0, result)
	}

	@Test
	fun `BB가 레이즈 후 숏스택 올인은 무시되어야 함 - 큰 블라인드 케이스`() {
		// BB = 100,000. BTN raises to 1,300,000. SB all-in for 100,000.
		val actions = listOf(
			Action(playerSeat = 1, type = ActionType.RAISE, amount = 1300000.0),
			Action(playerSeat = 2, type = ActionType.ALL_IN, amount = 100000.0),
		)
		val result = calculateLastRaiseTo(actions, Street.PREFLOP, bb = 100000.0)
		assertEquals(1300000.0, result)
	}
}

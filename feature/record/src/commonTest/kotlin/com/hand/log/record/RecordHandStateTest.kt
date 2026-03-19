package com.hand.log.record

import com.hand.log.domain.model.Action
import com.hand.log.domain.model.ActionType
import com.hand.log.domain.model.Blinds
import com.hand.log.domain.model.Card
import com.hand.log.domain.model.FlopStreet
import com.hand.log.domain.model.GameType
import com.hand.log.domain.model.HeroHand
import com.hand.log.domain.model.PokerTable
import com.hand.log.domain.model.PreflopStreet
import com.hand.log.domain.model.Rank
import com.hand.log.domain.model.Street
import com.hand.log.domain.model.Suit
import com.hand.log.record.contract.RecordHandState
import com.hand.log.record.contract.RecordStep
import com.hand.log.record.model.PlayerStatus
import com.hand.log.record.model.RecordPlayer
import com.hand.log.record.model.RecordPlayers
import com.hand.log.record.model.RecordStreets
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RecordHandStateTest {

	private fun makeState(
		playerCount: Int = 9,
		heroSeat: Int = 3,
		buttonSeat: Int = 1,
		bb: Double = 1000.0,
		sb: Double = 500.0,
		isBigBlindAnte: Boolean = false,
		startingStack: Double = 50000.0,
		currentStreet: Street = Street.PREFLOP,
		currentStep: RecordStep = RecordStep.PREFLOP,
		currentActionSeat: Int? = null,
		streets: RecordStreets = RecordStreets(),
		players: RecordPlayers? = null,
	): RecordHandState.Recording {
		val table = PokerTable(
			id = "test",
			date = LocalDate(2026, 3, 17),
			gameType = GameType.TOURNAMENT,
			startingStack = startingStack,
			blinds = Blinds(sb = sb, bb = bb, isBigBlindAnte = isBigBlindAnte),
			playerCount = playerCount,
			heroSeat = heroSeat,
			createdAt = 0L,
		)
		return RecordHandState.Recording(
			tableId = "test",
			table = table,
			heroHand = HeroHand(Card(Rank.ACE, Suit.SPADES), Card(Rank.KING, Suit.SPADES)),
			buttonSeat = buttonSeat,
			blinds = Blinds(sb = sb, bb = bb, isBigBlindAnte = isBigBlindAnte),
			players = players ?: RecordPlayers.create(playerCount, startingStack),
			streets = streets,
			currentStreet = currentStreet,
			currentStep = currentStep,
			currentActionSeat = currentActionSeat,
		)
	}

	// ===== 팟 계산 =====

	@Test
	fun `팟에 블라인드가 포함된다`() {
		val state = makeState(bb = 1000.0, sb = 500.0)
		assertEquals(1500.0, state.currentPot)
	}

	@Test
	fun `팟에 빅블라인드 앤티가 포함된다`() {
		val state = makeState(bb = 1000.0, sb = 500.0, isBigBlindAnte = true)
		assertEquals(2500.0, state.currentPot)
	}

	@Test
	fun `팟에 액션 금액이 포함된다`() {
		val state = makeState(
			bb = 1000.0,
			sb = 500.0,
			streets = RecordStreets(
				preflop = PreflopStreet(
					actions = listOf(
						Action(playerSeat = 4, type = ActionType.RAISE, amount = 2500.0),
						Action(playerSeat = 5, type = ActionType.CALL, amount = 2500.0),
					),
				),
			),
		)
		assertEquals(6500.0, state.currentPot)
	}

	@Test
	fun `앤티 포함 두명 올인 시 팟 계산`() {
		val state = makeState(
			bb = 10000.0,
			sb = 5000.0,
			isBigBlindAnte = true,
			startingStack = 50000.0,
			streets = RecordStreets(
				preflop = PreflopStreet(
					actions = listOf(
						Action(playerSeat = 4, type = ActionType.ALL_IN, amount = 50000.0),
						Action(playerSeat = 5, type = ActionType.ALL_IN, amount = 50000.0),
					),
				),
			),
		)
		assertEquals(125000.0, state.currentPot)
	}

	// ===== 가능한 액션 =====

	@Test
	fun `프리플랍 UTG는 폴드 콜 레이즈 올인 가능`() {
		val state = makeState(buttonSeat = 1, currentActionSeat = 4)
		val actions = state.availableActions
		assertTrue(ActionType.FOLD in actions)
		assertTrue(ActionType.CALL in actions)
		assertTrue(ActionType.RAISE in actions)
		assertTrue(ActionType.ALL_IN in actions)
		assertFalse(ActionType.CHECK in actions)
		assertFalse(ActionType.BET in actions)
	}

	@Test
	fun `프리플랍 BB 옵션은 체크 레이즈 올인`() {
		val state = makeState(buttonSeat = 1, currentActionSeat = 3)
		val actions = state.availableActions
		assertTrue(ActionType.CHECK in actions)
		assertTrue(ActionType.RAISE in actions)
		assertTrue(ActionType.ALL_IN in actions)
		assertFalse(ActionType.FOLD in actions)
		assertFalse(ActionType.CALL in actions)
	}

	@Test
	fun `프리플랍 레이즈 후 BB는 폴드 콜 레이즈 올인`() {
		val state = makeState(
			buttonSeat = 1,
			currentActionSeat = 3,
			streets = RecordStreets(
				preflop = PreflopStreet(
					actions = listOf(
						Action(playerSeat = 4, type = ActionType.RAISE, amount = 2500.0),
					),
				),
			),
		)
		val actions = state.availableActions
		assertTrue(ActionType.FOLD in actions)
		assertTrue(ActionType.CALL in actions)
		assertTrue(ActionType.RAISE in actions)
		assertTrue(ActionType.ALL_IN in actions)
	}

	@Test
	fun `포스트플랍 벳 없을 때 체크 벳 올인만 가능`() {
		val state = makeState(
			currentStreet = Street.FLOP,
			currentStep = RecordStep.FLOP,
			currentActionSeat = 2,
		)
		val actions = state.availableActions
		assertTrue(ActionType.CHECK in actions)
		assertTrue(ActionType.BET in actions)
		assertTrue(ActionType.ALL_IN in actions)
		assertFalse(ActionType.FOLD in actions)
	}

	@Test
	fun `포스트플랍 벳 후 폴드 콜 레이즈 올인 가능`() {
		val state = makeState(
			currentStreet = Street.FLOP,
			currentStep = RecordStep.FLOP,
			currentActionSeat = 3,
			streets = RecordStreets(
				preflop = PreflopStreet(),
				flop = FlopStreet(
					card1 = Card(Rank.ACE, Suit.HEARTS),
					card2 = Card(Rank.KING, Suit.HEARTS),
					card3 = Card(Rank.QUEEN, Suit.HEARTS),
					actions = listOf(
						Action(playerSeat = 2, type = ActionType.BET, amount = 2000.0),
					),
				),
			),
		)
		val actions = state.availableActions
		assertTrue(ActionType.FOLD in actions)
		assertTrue(ActionType.CALL in actions)
		assertTrue(ActionType.RAISE in actions)
		assertTrue(ActionType.ALL_IN in actions)
	}

	@Test
	fun `스택이 민레이즈 미만이면 레이즈 불가`() {
		val state = makeState(
			bb = 1000.0,
			currentActionSeat = 5,
			players = RecordPlayers(
				player1 = RecordPlayer(seat = 1, stack = 50000.0),
				player2 = RecordPlayer(seat = 2, stack = 50000.0),
				player3 = RecordPlayer(seat = 3, stack = 50000.0),
				player4 = RecordPlayer(seat = 4, stack = 47500.0),
				player5 = RecordPlayer(seat = 5, stack = 3000.0),
				player6 = RecordPlayer(seat = 6, stack = 50000.0),
				player7 = RecordPlayer(seat = 7, stack = 50000.0),
				player8 = RecordPlayer(seat = 8, stack = 50000.0),
				player9 = RecordPlayer(seat = 9, stack = 50000.0),
			),
			streets = RecordStreets(
				preflop = PreflopStreet(
					actions = listOf(
						Action(playerSeat = 4, type = ActionType.RAISE, amount = 2500.0),
					),
				),
			),
		)
		val actions = state.availableActions
		assertTrue(ActionType.FOLD in actions)
		assertTrue(ActionType.CALL in actions)
		assertFalse(ActionType.RAISE in actions)
		assertTrue(ActionType.ALL_IN in actions)
	}

	@Test
	fun `스택이 콜 금액 미만이면 콜 불가`() {
		val state = makeState(
			bb = 1000.0,
			currentActionSeat = 5,
			players = RecordPlayers(
				player1 = RecordPlayer(seat = 1, stack = 50000.0),
				player2 = RecordPlayer(seat = 2, stack = 50000.0),
				player3 = RecordPlayer(seat = 3, stack = 50000.0),
				player4 = RecordPlayer(seat = 4, stack = 0.0, status = PlayerStatus.ALL_IN),
				player5 = RecordPlayer(seat = 5, stack = 30000.0),
				player6 = RecordPlayer(seat = 6, stack = 50000.0),
				player7 = RecordPlayer(seat = 7, stack = 50000.0),
				player8 = RecordPlayer(seat = 8, stack = 50000.0),
				player9 = RecordPlayer(seat = 9, stack = 50000.0),
			),
			streets = RecordStreets(
				preflop = PreflopStreet(
					actions = listOf(
						Action(playerSeat = 4, type = ActionType.ALL_IN, amount = 50000.0),
					),
				),
			),
		)
		val actions = state.availableActions
		assertTrue(ActionType.FOLD in actions)
		assertFalse(ActionType.CALL in actions)
		assertTrue(ActionType.ALL_IN in actions)
	}

	// ===== 민레이즈 =====

	@Test
	fun `프리플랍 오픈 민레이즈는 2BB`() {
		val state = makeState(bb = 1000.0)
		assertEquals(2000.0, state.minRaiseAmount)
	}

	@Test
	fun `2500 레이즈 후 민3벳은 4000`() {
		val state = makeState(
			bb = 1000.0,
			streets = RecordStreets(
				preflop = PreflopStreet(
					actions = listOf(
						Action(playerSeat = 4, type = ActionType.RAISE, amount = 2500.0),
					),
				),
			),
		)
		assertEquals(4000.0, state.minRaiseAmount)
	}

	@Test
	fun `3벳 8000 후 민4벳은 13500`() {
		val state = makeState(
			bb = 1000.0,
			streets = RecordStreets(
				preflop = PreflopStreet(
					actions = listOf(
						Action(playerSeat = 4, type = ActionType.RAISE, amount = 2500.0),
						Action(playerSeat = 5, type = ActionType.RAISE, amount = 8000.0),
					),
				),
			),
		)
		assertEquals(13500.0, state.minRaiseAmount)
	}

	@Test
	fun `올인 레이즈 후 민리레이즈 계산`() {
		val state = makeState(
			bb = 1000.0,
			streets = RecordStreets(
				preflop = PreflopStreet(
					actions = listOf(
						Action(playerSeat = 4, type = ActionType.RAISE, amount = 2500.0),
						Action(playerSeat = 5, type = ActionType.ALL_IN, amount = 10000.0),
					),
				),
			),
		)
		assertEquals(17500.0, state.minRaiseAmount)
	}

	// ===== 액션 순서 =====

	@Test
	fun `프리플랍 액션 순서는 UTG부터 시작`() {
		val state = makeState(buttonSeat = 1)
		val order = state.preflopActionOrder
		assertEquals(4, order.first())
		assertEquals(3, order.last())
	}

	@Test
	fun `폴드한 플레이어는 액션 순서에서 제외`() {
		val state = makeState(
			buttonSeat = 1,
			players = RecordPlayers(
				player1 = RecordPlayer(seat = 1, stack = 50000.0),
				player2 = RecordPlayer(seat = 2, stack = 50000.0),
				player3 = RecordPlayer(seat = 3, stack = 50000.0),
				player4 = RecordPlayer(seat = 4, stack = 50000.0, status = PlayerStatus.FOLDED),
				player5 = RecordPlayer(seat = 5, stack = 50000.0),
				player6 = RecordPlayer(seat = 6, stack = 50000.0),
				player7 = RecordPlayer(seat = 7, stack = 50000.0),
				player8 = RecordPlayer(seat = 8, stack = 50000.0),
				player9 = RecordPlayer(seat = 9, stack = 50000.0),
			),
		)
		assertFalse(4 in state.actionOrder)
	}

	@Test
	fun `올인한 플레이어는 액션 순서에서 제외`() {
		val state = makeState(
			buttonSeat = 1,
			players = RecordPlayers(
				player1 = RecordPlayer(seat = 1, stack = 50000.0),
				player2 = RecordPlayer(seat = 2, stack = 50000.0),
				player3 = RecordPlayer(seat = 3, stack = 50000.0),
				player4 = RecordPlayer(seat = 4, stack = 0.0, status = PlayerStatus.ALL_IN),
				player5 = RecordPlayer(seat = 5, stack = 50000.0),
				player6 = RecordPlayer(seat = 6, stack = 50000.0),
				player7 = RecordPlayer(seat = 7, stack = 50000.0),
				player8 = RecordPlayer(seat = 8, stack = 50000.0),
				player9 = RecordPlayer(seat = 9, stack = 50000.0),
			),
		)
		assertFalse(4 in state.actionOrder)
	}

	@Test
	fun `모든 플레이어가 올인 또는 폴드면 액션 순서가 비어있다`() {
		val state = makeState(
			playerCount = 3,
			buttonSeat = 1,
			players = RecordPlayers(
				player1 = RecordPlayer(seat = 1, stack = 50000.0, status = PlayerStatus.FOLDED),
				player2 = RecordPlayer(seat = 2, stack = 0.0, status = PlayerStatus.ALL_IN),
				player3 = RecordPlayer(seat = 3, stack = 0.0, status = PlayerStatus.ALL_IN),
			),
		)
		assertTrue(state.actionOrder.isEmpty())
	}

	// ===== 포지션 =====

	@Test
	fun `9인 포지션 이름 확인`() {
		val state = makeState(playerCount = 9, buttonSeat = 1)
		assertEquals("BTN", state.positionName(1))
		assertEquals("SB", state.positionName(2))
		assertEquals("BB", state.positionName(3))
		assertEquals("UTG", state.positionName(4))
	}

	@Test
	fun `6인 포지션 이름 확인`() {
		val state = makeState(playerCount = 6, buttonSeat = 1)
		assertEquals("BTN", state.positionName(1))
		assertEquals("SB", state.positionName(2))
		assertEquals("BB", state.positionName(3))
		assertEquals("UTG", state.positionName(4))
		assertEquals("MP", state.positionName(5))
		assertEquals("CO", state.positionName(6))
	}

	// ===== 벳 레벨 =====

	@Test
	fun `프리플랍 초기 벳 레벨은 1`() {
		val state = makeState()
		assertEquals(1, state.currentBetLevel)
	}

	@Test
	fun `프리플랍 오픈레이즈 후 벳 레벨은 2`() {
		val state = makeState(
			streets = RecordStreets(
				preflop = PreflopStreet(
					actions = listOf(
						Action(playerSeat = 4, type = ActionType.RAISE, amount = 2500.0),
					),
				),
			),
		)
		assertEquals(2, state.currentBetLevel)
		assertEquals("3벳", state.nextRaiseLabel)
	}

	@Test
	fun `포스트플랍 초기 벳 레벨은 0`() {
		val state = makeState(currentStreet = Street.FLOP, currentStep = RecordStep.FLOP)
		assertEquals(0, state.currentBetLevel)
		assertEquals("벳", state.nextRaiseLabel)
	}

	@Test
	fun `포스트플랍 벳 후 레벨은 1이고 다음은 레이즈`() {
		val state = makeState(
			currentStreet = Street.FLOP,
			currentStep = RecordStep.FLOP,
			streets = RecordStreets(
				preflop = PreflopStreet(),
				flop = FlopStreet(
					actions = listOf(
						Action(playerSeat = 2, type = ActionType.BET, amount = 2000.0),
					),
				),
			),
		)
		assertEquals(1, state.currentBetLevel)
		assertEquals("레이즈", state.nextRaiseLabel)
	}

	// ===== 보드 카드 =====

	@Test
	fun `프리플랍은 항상 보드 준비 완료`() {
		val state = makeState()
		assertTrue(state.streets.isBoardReady(Street.PREFLOP))
	}

	@Test
	fun `플랍 카드 없으면 보드 준비 안됨`() {
		val state = makeState(
			currentStreet = Street.FLOP,
			streets = RecordStreets(preflop = PreflopStreet(), flop = FlopStreet()),
		)
		assertFalse(state.streets.isBoardReady(Street.FLOP))
	}

	@Test
	fun `플랍 카드 3장이면 보드 준비 완료`() {
		val state = makeState(
			currentStreet = Street.FLOP,
			streets = RecordStreets(
				preflop = PreflopStreet(),
				flop = FlopStreet(
					card1 = Card(Rank.ACE, Suit.HEARTS),
					card2 = Card(Rank.KING, Suit.HEARTS),
					card3 = Card(Rank.QUEEN, Suit.HEARTS),
				),
			),
		)
		assertTrue(state.streets.isBoardReady(Street.FLOP))
	}
}

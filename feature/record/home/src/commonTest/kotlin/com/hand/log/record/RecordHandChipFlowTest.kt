package com.hand.log.record

import com.hand.log.domain.model.Action
import com.hand.log.domain.model.ActionType
import com.hand.log.domain.model.Blinds
import com.hand.log.domain.model.Card
import com.hand.log.domain.model.FlopStreet
import com.hand.log.domain.model.GameType
import com.hand.log.domain.model.HandStreets
import com.hand.log.domain.model.Player
import com.hand.log.domain.model.PocketCards
import com.hand.log.domain.model.PokerTable
import com.hand.log.domain.model.PreflopStreet
import com.hand.log.domain.model.Rank
import com.hand.log.domain.model.RiverStreet
import com.hand.log.domain.model.Suit
import com.hand.log.domain.model.TurnStreet
import com.hand.log.record.contract.RecordHandState
import com.hand.log.record.contract.RecordStep
import com.hand.log.record.model.PlayerStatus
import com.hand.log.record.model.RecordPlayer
import com.hand.log.record.model.RecordPlayers
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class RecordHandChipFlowTest {

	private val heroCards = PocketCards(Card(Rank.ACE, Suit.SPADES), Card(Rank.KING, Suit.SPADES))
	private val queens = PocketCards(Card(Rank.QUEEN, Suit.SPADES), Card(Rank.QUEEN, Suit.HEARTS))
	private val nines = PocketCards(Card(Rank.NINE, Suit.CLUBS), Card(Rank.NINE, Suit.DIAMONDS))

	private fun recording(
		playerCount: Int,
		heroSeat: Int,
		currentStep: RecordStep,
		buttonSeat: Int = 1,
		sb: Double = 1.0,
		bb: Double = 2.0,
		players: Map<Int, RecordPlayer> = (1..playerCount).associateWith { RecordPlayer(seat = it) },
		streets: HandStreets = HandStreets(),
		currentActionSeat: Int? = null,
	): RecordHandState.Recording = RecordHandState.Recording(
		tableId = "test",
		table = PokerTable(
			id = "test",
			date = LocalDate(2026, 3, 17),
			gameType = GameType.Tournament(isBigBlindAnte = false),
			heroSeat = heroSeat,
			players = (1..playerCount).map { Player(seat = it) },
			createdAt = 0L,
		),
		buttonSeat = buttonSeat,
		blinds = Blinds(sb = sb, bb = bb),
		players = RecordPlayers(players),
		streets = streets,
		currentStep = currentStep,
		currentActionSeat = currentActionSeat,
	)

	private val heroBetsVillainCallsDown = HandStreets(
		preflop = PreflopStreet(
			actions = listOf(
				Action(playerSeat = 1, type = ActionType.RAISE, amount = 6.0),
				Action(playerSeat = 2, type = ActionType.FOLD),
				Action(playerSeat = 3, type = ActionType.CALL, amount = 6.0),
			),
		),
		flop = FlopStreet(
			card1 = Card(Rank.ACE, Suit.HEARTS),
			card2 = Card(Rank.KING, Suit.DIAMONDS),
			card3 = Card(Rank.QUEEN, Suit.CLUBS),
			actions = listOf(
				Action(playerSeat = 3, type = ActionType.CHECK),
				Action(playerSeat = 1, type = ActionType.BET, amount = 10.0),
				Action(playerSeat = 3, type = ActionType.CALL, amount = 10.0),
			),
		),
		turn = TurnStreet(
			card = Card(Rank.SEVEN, Suit.HEARTS),
			actions = listOf(
				Action(playerSeat = 3, type = ActionType.CHECK),
				Action(playerSeat = 1, type = ActionType.CHECK),
			),
		),
		river = RiverStreet(
			card = Card(Rank.TWO, Suit.DIAMONDS),
			actions = listOf(
				Action(playerSeat = 3, type = ActionType.CHECK),
				Action(playerSeat = 1, type = ActionType.CHECK),
			),
		),
	)

	private fun showdownAgainst(
		villainCards: PocketCards,
		heroInitialStack: Double? = 100.0,
		villainInitialStack: Double? = 100.0,
	) = recording(
		playerCount = 3,
		heroSeat = 1,
		currentStep = RecordStep.SHOWDOWN,
		streets = heroBetsVillainCallsDown,
		players = mapOf(
			1 to RecordPlayer(
				seat = 1,
				cards = heroCards,
				stack = heroInitialStack?.minus(16.0) ?: 0.0,
				initialStack = heroInitialStack,
			),
			2 to RecordPlayer(seat = 2, stack = 99.0, initialStack = 100.0, status = PlayerStatus.FOLDED),
			3 to RecordPlayer(
				seat = 3,
				cards = villainCards,
				stack = villainInitialStack?.minus(16.0) ?: 0.0,
				initialStack = villainInitialStack,
			),
		),
	)

	@Test
	fun `세팅에서 입력한 SB 히어로 스택은 프리플랍 진입 시 블라인드만 한 번 차감된다`() {
		val setup = recording(playerCount = 3, heroSeat = 2, currentStep = RecordStep.SETUP)
			.withInitialStack(seat = 2, totalStack = 100.0)
		assertEquals(100.0, setup.players[2]?.stack)

		val preflop = setup.advancedToNextStep()
		assertEquals(RecordStep.PREFLOP, preflop.currentStep)
		assertEquals(99.0, preflop.players[2]?.stack)
		assertEquals(1.0, preflop.players[2]?.currentBet)
	}

	@Test
	fun `세팅으로 되돌아갔다가 다시 프리플랍에 들어가도 블라인드는 한 번만 차감된다`() {
		val preflop = recording(playerCount = 3, heroSeat = 3, currentStep = RecordStep.SETUP)
			.withInitialStack(seat = 3, totalStack = 100.0)
			.advancedToNextStep()

		val reentered = preflop.rewoundTo(RecordStep.SETUP).advancedToNextStep()
		assertEquals(98.0, reentered.players[3]?.stack)
		assertEquals(2.0, reentered.players[3]?.currentBet)
	}

	@Test
	fun `베팅한 뒤 입력한 스택은 이미 투입한 금액을 뺀 값이 된다`() {
		val state = recording(
			playerCount = 4,
			heroSeat = 1,
			currentStep = RecordStep.PREFLOP,
			sb = 500.0,
			bb = 1000.0,
			streets = HandStreets(
				preflop = PreflopStreet(
					actions = listOf(Action(playerSeat = 4, type = ActionType.RAISE, amount = 3000.0)),
				),
			),
		).withInitialStack(seat = 4, totalStack = 10000.0)
		assertEquals(7000.0, state.players[4]?.stack)
	}

	@Test
	fun `상대 스택을 모르는 쇼다운에서 진 히어로 결과는 손실이다`() {
		val state = showdownAgainst(queens, villainInitialStack = null)
		assertEquals(mapOf(3 to 33.0), state.showdownWinnings)
		assertEquals(-16.0, state.heroResult)
	}

	@Test
	fun `히어로 스택을 모르는 쇼다운에서도 결과는 액션 로그로 계산된다`() {
		val state = showdownAgainst(queens, heroInitialStack = null)
		assertEquals(-16.0, state.heroResult)
	}

	@Test
	fun `쇼다운 카드를 고쳐도 팟은 한 번만 분배된다`() {
		val heroWins = showdownAgainst(nines)
		assertEquals(117.0, heroWins.getPlayerStack(1))
		assertEquals(84.0, heroWins.getPlayerStack(3))

		val corrected = heroWins.copy(players = heroWins.players.update(3) { copy(cards = queens) })
		assertEquals(84.0, corrected.getPlayerStack(1))
		assertEquals(117.0, corrected.getPlayerStack(3))
		assertEquals(-16.0, corrected.heroResult)
	}

	@Test
	fun `스택을 모르는 상대가 폴드하면 그 상대의 베팅도 히어로가 가져간다`() {
		val state = recording(
			playerCount = 3,
			heroSeat = 3,
			currentStep = RecordStep.SHOWDOWN,
			streets = HandStreets(
				preflop = PreflopStreet(
					actions = listOf(
						Action(playerSeat = 1, type = ActionType.RAISE, amount = 6.0),
						Action(playerSeat = 2, type = ActionType.FOLD),
						Action(playerSeat = 3, type = ActionType.RAISE, amount = 20.0),
						Action(playerSeat = 1, type = ActionType.FOLD),
					),
				),
			),
			players = mapOf(
				1 to RecordPlayer(seat = 1, status = PlayerStatus.FOLDED),
				2 to RecordPlayer(seat = 2, stack = 99.0, initialStack = 100.0, status = PlayerStatus.FOLDED),
				3 to RecordPlayer(seat = 3, cards = heroCards, stack = 80.0, initialStack = 100.0),
			),
		)
		assertEquals(7.0, state.heroResult)
	}

	@Test
	fun `레이즈 후 폴드한 금액도 팟에 남는다`() {
		val state = recording(
			playerCount = 4,
			heroSeat = 1,
			currentStep = RecordStep.PREFLOP,
			sb = 500.0,
			bb = 1000.0,
			streets = HandStreets(
				preflop = PreflopStreet(
					actions = listOf(
						Action(playerSeat = 4, type = ActionType.RAISE, amount = 3000.0),
						Action(playerSeat = 1, type = ActionType.RAISE, amount = 9000.0),
						Action(playerSeat = 2, type = ActionType.FOLD),
						Action(playerSeat = 3, type = ActionType.FOLD),
						Action(playerSeat = 4, type = ActionType.FOLD),
					),
				),
			),
		)
		assertEquals(13500.0, state.currentPot)
	}

	@Test
	fun `3벳을 받은 오프너는 남은 스택과 이미 낸 금액으로 콜할 수 있다`() {
		val state = recording(
			playerCount = 4,
			heroSeat = 1,
			currentStep = RecordStep.PREFLOP,
			sb = 500.0,
			bb = 1000.0,
			currentActionSeat = 4,
			streets = HandStreets(
				preflop = PreflopStreet(
					actions = listOf(
						Action(playerSeat = 4, type = ActionType.RAISE, amount = 3000.0),
						Action(playerSeat = 1, type = ActionType.RAISE, amount = 9000.0),
						Action(playerSeat = 2, type = ActionType.FOLD),
						Action(playerSeat = 3, type = ActionType.FOLD),
					),
				),
			),
			players = mapOf(
				1 to RecordPlayer(seat = 1, stack = 1000.0, initialStack = 10000.0),
				2 to RecordPlayer(seat = 2, stack = 9500.0, initialStack = 10000.0, status = PlayerStatus.FOLDED),
				3 to RecordPlayer(seat = 3, stack = 9000.0, initialStack = 10000.0, status = PlayerStatus.FOLDED),
				4 to RecordPlayer(seat = 4, stack = 7000.0, initialStack = 10000.0, currentBet = 3000.0),
			),
		)
		assertEquals(listOf(ActionType.FOLD, ActionType.CALL, ActionType.ALL_IN), state.availableActions)
	}

	@Test
	fun `BB는 낸 블라인드를 포함해 레이즈에 콜할 수 있다`() {
		val state = recording(
			playerCount = 4,
			heroSeat = 1,
			currentStep = RecordStep.PREFLOP,
			sb = 500.0,
			bb = 1000.0,
			currentActionSeat = 3,
			streets = HandStreets(
				preflop = PreflopStreet(
					actions = listOf(
						Action(playerSeat = 4, type = ActionType.RAISE, amount = 2500.0),
						Action(playerSeat = 1, type = ActionType.FOLD),
						Action(playerSeat = 2, type = ActionType.FOLD),
					),
				),
			),
			players = mapOf(
				1 to RecordPlayer(seat = 1, stack = 10000.0, initialStack = 10000.0, status = PlayerStatus.FOLDED),
				2 to RecordPlayer(seat = 2, stack = 9500.0, initialStack = 10000.0, status = PlayerStatus.FOLDED),
				3 to RecordPlayer(seat = 3, stack = 2000.0, initialStack = 3000.0, currentBet = 1000.0),
				4 to RecordPlayer(seat = 4, stack = 7500.0, initialStack = 10000.0, currentBet = 2500.0),
			),
		)
		assertEquals(listOf(ActionType.FOLD, ActionType.CALL, ActionType.ALL_IN), state.availableActions)
	}

	@Test
	fun `블라인드의 체크와 폴드를 되돌려도 낸 블라인드는 유지된다`() {
		val state = recording(
			playerCount = 3,
			heroSeat = 1,
			currentStep = RecordStep.PREFLOP,
			streets = HandStreets(
				preflop = PreflopStreet(
					actions = listOf(
						Action(
							playerSeat = 1,
							type = ActionType.CALL,
							amount = 2.0,
							stackBefore = 100.0,
							stackAfter = 98.0,
						),
						Action(playerSeat = 2, type = ActionType.FOLD, stackBefore = 100.0, stackAfter = 99.0),
						Action(playerSeat = 3, type = ActionType.CHECK, stackBefore = 100.0, stackAfter = 98.0),
					),
				),
			),
			players = mapOf(
				1 to RecordPlayer(seat = 1, stack = 98.0, initialStack = 100.0, currentBet = 2.0),
				2 to RecordPlayer(seat = 2, stack = 99.0, initialStack = 100.0, currentBet = 1.0, status = PlayerStatus.FOLDED),
				3 to RecordPlayer(seat = 3, stack = 98.0, initialStack = 100.0, currentBet = 2.0),
			),
		)

		val bbUndone = state.withLastActionUndone()
		assertEquals(3, bbUndone.currentActionSeat)
		assertEquals(98.0, bbUndone.players[3]?.stack)
		assertEquals(2.0, bbUndone.players[3]?.currentBet)

		val sbUndone = bbUndone.withLastActionUndone()
		assertEquals(2, sbUndone.currentActionSeat)
		assertEquals(99.0, sbUndone.players[2]?.stack)
		assertEquals(1.0, sbUndone.players[2]?.currentBet)
		assertEquals(PlayerStatus.ACTIVE, sbUndone.players[2]?.status)
	}

	@Test
	fun `레이즈를 되돌리면 어그레서가 이전 베터로 돌아간다`() {
		val state = recording(
			playerCount = 3,
			heroSeat = 1,
			currentStep = RecordStep.FLOP,
			streets = HandStreets(
				flop = FlopStreet(
					card1 = Card(Rank.ACE, Suit.HEARTS),
					card2 = Card(Rank.KING, Suit.DIAMONDS),
					card3 = Card(Rank.QUEEN, Suit.CLUBS),
					actions = listOf(
						Action(playerSeat = 2, type = ActionType.BET, amount = 10.0),
						Action(playerSeat = 3, type = ActionType.RAISE, amount = 30.0),
					),
				),
			),
		)
		assertEquals(3, state.lastAggressorSeat)

		val raiseUndone = state.withLastActionUndone()
		assertEquals(2, raiseUndone.lastAggressorSeat)
		assertEquals(3, raiseUndone.currentActionSeat)

		assertNull(raiseUndone.withLastActionUndone().lastAggressorSeat)
	}

	@Test
	fun `헤즈업 프리플랍은 버튼이 먼저 포스트플랍은 BB가 먼저 액션한다`() {
		val state = recording(playerCount = 2, heroSeat = 2, currentStep = RecordStep.PREFLOP)
		assertEquals(listOf(1, 2), state.preflopActionOrder)
		assertEquals(listOf(2, 1), state.postflopActionOrder)
	}

	@Test
	fun `헤즈업 BB는 버튼의 림프 후 체크할 수 있다`() {
		val state = recording(
			playerCount = 2,
			heroSeat = 2,
			currentStep = RecordStep.PREFLOP,
			currentActionSeat = 2,
			streets = HandStreets(
				preflop = PreflopStreet(
					actions = listOf(Action(playerSeat = 1, type = ActionType.CALL, amount = 2.0)),
				),
			),
		)
		assertEquals(
			listOf(ActionType.CHECK, ActionType.RAISE, ActionType.ALL_IN),
			state.availableActions,
		)
	}

	@Test
	fun `헤즈업 포지션은 BTN과 BB이고 BB를 고르면 상대가 버튼이 된다`() {
		val state = recording(playerCount = 2, heroSeat = 2, currentStep = RecordStep.SETUP)
		assertEquals("BTN", state.positionName(1))
		assertEquals("BB", state.positionName(2))
		assertEquals(1, state.buttonSeatForHeroPosition("BB"))
		assertEquals(2, state.buttonSeatForHeroPosition("BTN"))
	}

	@Test
	fun `저장 중에는 쇼다운에서 저장할 수 없다`() {
		val showdown = recording(playerCount = 3, heroSeat = 1, currentStep = RecordStep.SHOWDOWN)
		assertTrue(showdown.canProceed)
		assertFalse(showdown.copy(isSaving = true).canProceed)
	}
}

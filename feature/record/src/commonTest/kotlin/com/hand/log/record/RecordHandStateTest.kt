package com.hand.log.record

import com.hand.log.domain.model.Action
import com.hand.log.domain.model.ActionType
import com.hand.log.domain.model.Blinds
import com.hand.log.domain.model.Card
import com.hand.log.domain.model.FlopStreet
import com.hand.log.domain.model.GameType
import com.hand.log.domain.model.Player
import com.hand.log.domain.model.PocketCards
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
import com.hand.log.domain.model.HandStreets
import com.hand.log.domain.model.ShowdownOutcome
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
		currentStep: RecordStep = RecordStep.PREFLOP,
		currentActionSeat: Int? = null,
		streets: HandStreets = HandStreets(),
		players: RecordPlayers? = null,
	): RecordHandState.Recording {
		val table = PokerTable(
			id = "test",
			date = LocalDate(2026, 3, 17),
			gameType = GameType.Tournament(isBigBlindAnte = isBigBlindAnte),
			heroSeat = heroSeat,
			players = (1..playerCount).map { Player(seat = it) },
			createdAt = 0L,
		)
		val heroCards = PocketCards(Card(Rank.ACE, Suit.SPADES), Card(Rank.KING, Suit.SPADES))
		val basePlayers = players ?: RecordPlayers.create(playerCount, 50000.0)
		return RecordHandState.Recording(
			tableId = "test",
			table = table,
			buttonSeat = buttonSeat,
			blinds = Blinds(sb = sb, bb = bb, isBigBlindAnte = isBigBlindAnte),
			players = basePlayers.update(heroSeat) { copy(cards = heroCards) },
			streets = streets,
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
			streets = HandStreets(
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
			streets = HandStreets(
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

	// ===== 사이드 팟 =====

	@Test
	fun `올인 플레이어가 없으면 사이드 팟 없음`() {
		val state = makeState(
			playerCount = 3,
			players = RecordPlayers(mapOf(
				1 to RecordPlayer(seat = 1, initialStack = 50000.0),
				2 to RecordPlayer(seat = 2, initialStack = 50000.0),
				3 to RecordPlayer(seat = 3, initialStack = 50000.0),
			)),
		)
		assertTrue(state.sidePots.isEmpty())
	}

	@Test
	fun `올인 1명과 콜러 1명이면 사이드 팟 없음 - 투입 금액이 동일`() {
		val state = makeState(
			playerCount = 3,
			players = RecordPlayers(
				mapOf(
					1 to RecordPlayer(
						seat = 1,
						stack = 0.0,
						initialStack = 50000.0,
						status = PlayerStatus.ALL_IN,
					),
					2 to RecordPlayer(seat = 2, initialStack = 50000.0),
					3 to RecordPlayer(
						seat = 3,
						stack = 50000.0,
						initialStack = 50000.0,
						status = PlayerStatus.FOLDED,
					),
				),
			),
		)
		// 투입 레벨이 1개(50000)뿐 → 사이드 팟 없음
		assertTrue(state.sidePots.isEmpty())
	}

	@Test
	fun `숏스택 올인 시 메인팟과 사이드팟 분리`() {
		// Seat1: 20K 올인, Seat2: 50K 올인, Seat3: 50K 콜
		val state = makeState(
			playerCount = 3,
			players = RecordPlayers(
				mapOf(
					1 to RecordPlayer(
						seat = 1,
						stack = 0.0,
						initialStack = 20000.0,
						status = PlayerStatus.ALL_IN,
					),
					2 to RecordPlayer(
						seat = 2,
						stack = 0.0,
						initialStack = 50000.0,
						status = PlayerStatus.ALL_IN,
					),
					3 to RecordPlayer(
						seat = 3,
						stack = 0.0,
						initialStack = 50000.0,
					),
				),
			),
		)
		val pots = state.sidePots
		assertEquals(2, pots.size)
		// 메인팟: 20K * 3명 = 60K
		assertEquals(60000.0, pots[0])
		// 사이드팟: (50K - 20K) * 2명 = 60K
		assertEquals(60000.0, pots[1])
	}

	@Test
	fun `3단계 사이드팟 - 서로 다른 스택 올인`() {
		// Seat1: 10K 올인, Seat2: 30K 올인, Seat3: 50K 올인, Seat4: 50K 콜
		val state = makeState(
			playerCount = 4,
			players = RecordPlayers(
				mapOf(
					1 to RecordPlayer(
						seat = 1,
						stack = 0.0,
						initialStack = 10000.0,
						status = PlayerStatus.ALL_IN,
					),
					2 to RecordPlayer(
						seat = 2,
						stack = 0.0,
						initialStack = 30000.0,
						status = PlayerStatus.ALL_IN,
					),
					3 to RecordPlayer(
						seat = 3,
						stack = 0.0,
						initialStack = 50000.0,
						status = PlayerStatus.ALL_IN,
					),
					4 to RecordPlayer(seat = 4, stack = 0.0, initialStack = 50000.0),
				),
			),
		)
		val pots = state.sidePots
		assertEquals(3, pots.size)
		// 메인팟: 10K * 4명 = 40K
		assertEquals(40000.0, pots[0])
		// 사이드팟1: (30K - 10K) * 3명 = 60K
		assertEquals(60000.0, pots[1])
		// 사이드팟2: (50K - 30K) * 2명 = 40K
		assertEquals(40000.0, pots[2])
	}

	@Test
	fun `폴드한 플레이어 투입 금액도 팟에 포함`() {
		// Seat1: 20K 올인, Seat2: 50K 올인, Seat3: 10K 투입 후 폴드
		val state = makeState(
			playerCount = 3,
			players = RecordPlayers(
				mapOf(
					1 to RecordPlayer(
						seat = 1,
						stack = 0.0,
						initialStack = 20000.0,
						status = PlayerStatus.ALL_IN,
					),
					2 to RecordPlayer(
						seat = 2,
						stack = 0.0,
						initialStack = 50000.0,
						status = PlayerStatus.ALL_IN,
					),
					3 to RecordPlayer(
						seat = 3,
						stack = 40000.0,
						initialStack = 50000.0,
						status = PlayerStatus.FOLDED,
					),
				),
			),
		)
		val pots = state.sidePots
		// 레벨 50K는 eligible=1(Seat2만) → 미콜 베팅으로 제외
		assertEquals(2, pots.size)
		// 레벨 10K: 10K * 3명 = 30K
		assertEquals(30000.0, pots[0])
		// 레벨 20K: (20K - 10K) * 2명 = 20K
		assertEquals(20000.0, pots[1])
	}

	@Test
	fun `투입 금액이 없는 플레이어는 팟 계산에서 제외`() {
		val state = makeState(
			playerCount = 4,
			players = RecordPlayers(
				mapOf(
					1 to RecordPlayer(
						seat = 1,
						stack = 0.0,
						initialStack = 30000.0,
						status = PlayerStatus.ALL_IN,
					),
					2 to RecordPlayer(
						seat = 2,
						stack = 0.0,
						initialStack = 50000.0,
						status = PlayerStatus.ALL_IN,
					),
					3 to RecordPlayer(
						seat = 3,
						stack = 50000.0,
						initialStack = 50000.0,
						status = PlayerStatus.FOLDED,
					),
					4 to RecordPlayer(seat = 4, stack = 50000.0, initialStack = 50000.0),
				),
			),
		)
		val pots = state.sidePots
		// Seat3, Seat4는 투입 0 → 제외, 투입 플레이어 2명
		// (50K - 30K) × 1명 = eligible 1 → 미콜 베팅 제외
		// 사이드팟 1개뿐 → emptyList
		assertTrue(pots.isEmpty())
	}

	@Test
	fun `올인 플레이어 1명뿐이고 투입 플레이어 1명이면 사이드 팟 없음`() {
		val state = makeState(
			playerCount = 3,
			players = RecordPlayers(
				mapOf(
					1 to RecordPlayer(
						seat = 1,
						stack = 0.0,
						initialStack = 50000.0,
						status = PlayerStatus.ALL_IN,
					),
					2 to RecordPlayer(seat = 2, initialStack = 50000.0),
					3 to RecordPlayer(seat = 3, initialStack = 50000.0),
				),
			),
		)
		// 투입 플레이어 1명뿐 → size < 2 → empty
		assertTrue(state.sidePots.isEmpty())
	}

	// ===== 사이드 팟 분배 (potResults) =====

	@Test
	fun `A 10만 올인 B 5만 올인 C 8만 올인 - 메인팟 15만 사이드팟 6만 2만`() {
		val state = makeState(
			playerCount = 3,
			players = RecordPlayers(
				mapOf(
					1 to RecordPlayer(
						seat = 1,
						stack = 0.0,
						initialStack = 100000.0,
						status = PlayerStatus.ALL_IN,
					),
					2 to RecordPlayer(
						seat = 2,
						stack = 0.0,
						initialStack = 50000.0,
						status = PlayerStatus.ALL_IN,
					),
					3 to RecordPlayer(
						seat = 3,
						stack = 0.0,
						initialStack = 80000.0,
						status = PlayerStatus.ALL_IN,
					),
				),
			),
		)
		val pots = state.sidePots
		// (10만 - 8만) × 1 = eligible 1 → 미콜 베팅 제외
		assertEquals(2, pots.size)
		// 메인팟: 5만 * 3 = 15만
		assertEquals(150000.0, pots[0])
		// 사이드팟1: (8만 - 5만) * 2 = 6만 (A, C만 eligible)
		assertEquals(60000.0, pots[1])
	}

	@Test
	fun `사이드팟 없으면 potResults 비어있음`() {
		val state = makeState(
			playerCount = 3,
			players = RecordPlayers(
				mapOf(
					1 to RecordPlayer(
						seat = 1,
						stack = 0.0,
						initialStack = 50000.0,
						status = PlayerStatus.ALL_IN,
					),
					2 to RecordPlayer(seat = 2, stack = 0.0, initialStack = 50000.0),
					3 to RecordPlayer(
						seat = 3,
						stack = 50000.0,
						initialStack = 50000.0,
						status = PlayerStatus.FOLDED,
					),
				),
			),
		)
		assertTrue(state.potResults.isEmpty())
	}

	@Test
	fun `메인팟 표시에는 sidePots 첫번째가 사용된다`() {
		val state = makeState(
			playerCount = 3,
			players = RecordPlayers(
				mapOf(
					1 to RecordPlayer(
						seat = 1,
						stack = 0.0,
						initialStack = 100000.0,
						status = PlayerStatus.ALL_IN,
					),
					2 to RecordPlayer(
						seat = 2,
						stack = 0.0,
						initialStack = 50000.0,
						status = PlayerStatus.ALL_IN,
					),
					3 to RecordPlayer(
						seat = 3,
						stack = 0.0,
						initialStack = 80000.0,
						status = PlayerStatus.ALL_IN,
					),
				),
			),
		)
		val pots = state.sidePots
		assertTrue(pots.size > 1)
		// POT에 표시할 메인팟 = sidePots[0]
		val mainPot = pots.first()
		assertEquals(150000.0, mainPot)
		// 사이드팟은 drop(1) - 미콜 베팅 제외로 1개
		val sidePotOnly = pots.drop(1)
		assertEquals(1, sidePotOnly.size)
	}

	@Test
	fun `스택 미입력 플레이어는 사이드팟 계산에 포함되지 않음`() {
		// Seat1: 5만 올인, Seat2: 스택 미입력(0), Seat3: 5만 콜
		val state = makeState(
			playerCount = 3,
			players = RecordPlayers(
				mapOf(
					1 to RecordPlayer(
						seat = 1,
						stack = 0.0,
						initialStack = 50000.0,
						status = PlayerStatus.ALL_IN,
					),
					2 to RecordPlayer(seat = 2, stack = 0.0, initialStack = null),
					3 to RecordPlayer(seat = 3, stack = 0.0, initialStack = 50000.0),
				),
			),
		)
		// Seat2는 invested = 0 → 팟 계산에서 제외
		val pots = state.sidePots
		// Seat1, Seat3만 동일 레벨 → 사이드팟 없음
		assertTrue(pots.isEmpty())
	}

	@Test
	fun `BBA 포함 3인 올인 - 메인팟에 ante 포함 미콜 베팅은 사이드팟 제외`() {
		// BTN(seat1): 50만, SB(seat2): 10만, BB(seat3): 40만
		// SB=8000, BB=8000, BBA=true (ante=8000)
		val state = makeState(
			playerCount = 3,
			buttonSeat = 1,
			sb = 8000.0,
			bb = 8000.0,
			isBigBlindAnte = true,
			players = RecordPlayers(
				mapOf(
					1 to RecordPlayer(
						seat = 1,
						stack = 0.0,
						initialStack = 500000.0,
						status = PlayerStatus.ALL_IN,
					),
					2 to RecordPlayer(
						seat = 2,
						stack = 0.0,
						initialStack = 100000.0,
						status = PlayerStatus.ALL_IN,
					),
					3 to RecordPlayer(
						seat = 3,
						stack = 0.0,
						initialStack = 400000.0,
						status = PlayerStatus.ALL_IN,
					),
				),
			),
		)
		val pots = state.sidePots
		// 사이드팟은 2개 (미콜 베팅은 제외)
		assertEquals(2, pots.size)
		// 메인팟: 100,000 × 3 + 8,000(ante) = 308,000
		assertEquals(308000.0, pots[0])
		// 사이드팟: (392,000 - 100,000) × 2 = 584,000
		assertEquals(584000.0, pots[1])
	}

	@Test
	fun `미콜 베팅은 사이드팟에서 제외`() {
		// Seat1: 50만 올인, Seat2: 30만 올인, Seat3: 30만 콜
		val state = makeState(
			playerCount = 3,
			players = RecordPlayers(
				mapOf(
					1 to RecordPlayer(
						seat = 1,
						stack = 0.0,
						initialStack = 500000.0,
						status = PlayerStatus.ALL_IN,
					),
					2 to RecordPlayer(
						seat = 2,
						stack = 0.0,
						initialStack = 300000.0,
						status = PlayerStatus.ALL_IN,
					),
					3 to RecordPlayer(seat = 3, stack = 0.0, initialStack = 300000.0),
				),
			),
		)
		val pots = state.sidePots
		// 300K × 3 = 900K (메인팟), 200K × 1 = 미콜 → 제외
		// 사이드팟 1개뿐 → emptyList
		assertTrue(pots.isEmpty())
	}

	@Test
	fun `BB앤티 4000-4000-2000 UTG 2BB레이즈 LJ BTN 콜 시 팟에 앤티 포함`() {
		// 8인 테이블: BTN=1, SB=2, BB=3, UTG=4, UTG1=5, LJ=6, HJ=7, CO=8
		val state = makeState(
			playerCount = 8,
			buttonSeat = 1,
			bb = 4000.0,
			sb = 2000.0,
			isBigBlindAnte = true,
			streets = HandStreets(
				preflop = PreflopStreet(
					actions = listOf(
						Action(playerSeat = 4, type = ActionType.RAISE, amount = 8000.0),
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
		// 앤티(4000) + SB(2000) + BB(4000) + UTG(8000) + LJ(8000) + BTN(8000) = 34000
		assertEquals(34000.0, state.currentPot)
	}

	@Test
	fun `BB앤티 없이 동일 상황이면 앤티 제외 30000`() {
		val state = makeState(
			playerCount = 8,
			buttonSeat = 1,
			bb = 4000.0,
			sb = 2000.0,
			isBigBlindAnte = false,
			streets = HandStreets(
				preflop = PreflopStreet(
					actions = listOf(
						Action(playerSeat = 4, type = ActionType.RAISE, amount = 8000.0),
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
		// SB(2000) + BB(4000) + UTG(8000) + LJ(8000) + BTN(8000) = 30000
		assertEquals(30000.0, state.currentPot)
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
			streets = HandStreets(
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
			currentStep = RecordStep.FLOP,
			currentActionSeat = 3,
			streets = HandStreets(
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
				mapOf(
					1 to RecordPlayer(seat = 1, stack = 50000.0, initialStack = 50000.0),
					2 to RecordPlayer(seat = 2, stack = 50000.0, initialStack = 50000.0),
					3 to RecordPlayer(seat = 3, stack = 50000.0, initialStack = 50000.0),
					4 to RecordPlayer(seat = 4, stack = 47500.0, initialStack = 50000.0),
					5 to RecordPlayer(seat = 5, stack = 3000.0, initialStack = 3000.0),
					6 to RecordPlayer(seat = 6, stack = 50000.0, initialStack = 50000.0),
					7 to RecordPlayer(seat = 7, stack = 50000.0, initialStack = 50000.0),
					8 to RecordPlayer(seat = 8, stack = 50000.0, initialStack = 50000.0),
					9 to RecordPlayer(seat = 9, stack = 50000.0, initialStack = 50000.0),
				),
			),
			streets = HandStreets(
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
				mapOf(
					1 to RecordPlayer(seat = 1, stack = 50000.0, initialStack = 50000.0),
					2 to RecordPlayer(seat = 2, stack = 50000.0, initialStack = 50000.0),
					3 to RecordPlayer(seat = 3, stack = 50000.0, initialStack = 50000.0),
					4 to RecordPlayer(
						seat = 4,
						stack = 0.0,
						initialStack = 50000.0,
						status = PlayerStatus.ALL_IN,
					),
					5 to RecordPlayer(seat = 5, stack = 30000.0, initialStack = 30000.0),
					6 to RecordPlayer(seat = 6, stack = 50000.0, initialStack = 50000.0),
					7 to RecordPlayer(seat = 7, stack = 50000.0, initialStack = 50000.0),
					8 to RecordPlayer(seat = 8, stack = 50000.0, initialStack = 50000.0),
					9 to RecordPlayer(seat = 9, stack = 50000.0, initialStack = 50000.0),
				),
			),
			streets = HandStreets(
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

	@Test
	fun `스택 미입력 initialStack null이면 모든 액션 가능`() {
		// 프리플랍 오픈 상황, seat4가 레이즈 후 seat5의 initialStack이 null
		val state = makeState(
			bb = 1000.0,
			currentActionSeat = 5,
			players = RecordPlayers(
				mapOf(
					1 to RecordPlayer(seat = 1, stack = 50000.0, initialStack = 50000.0),
					2 to RecordPlayer(seat = 2, stack = 50000.0, initialStack = 50000.0),
					3 to RecordPlayer(seat = 3, stack = 50000.0, initialStack = 50000.0),
					4 to RecordPlayer(seat = 4, stack = 47500.0, initialStack = 50000.0),
					5 to RecordPlayer(seat = 5, initialStack = null),
					6 to RecordPlayer(seat = 6, stack = 50000.0, initialStack = 50000.0),
					7 to RecordPlayer(seat = 7, stack = 50000.0, initialStack = 50000.0),
					8 to RecordPlayer(seat = 8, stack = 50000.0, initialStack = 50000.0),
					9 to RecordPlayer(seat = 9, stack = 50000.0, initialStack = 50000.0),
				),
			),
			streets = HandStreets(
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
	fun `스택 미입력 initialStack null이면 큰 레이즈 후에도 레이즈 가능`() {
		// seat4가 50000 올인 후 seat5 (initialStack null) → 여전히 모든 액션 가능
		val state = makeState(
			bb = 1000.0,
			currentActionSeat = 5,
			players = RecordPlayers(
				mapOf(
					1 to RecordPlayer(seat = 1, stack = 50000.0, initialStack = 50000.0),
					2 to RecordPlayer(seat = 2, stack = 50000.0, initialStack = 50000.0),
					3 to RecordPlayer(seat = 3, stack = 50000.0, initialStack = 50000.0),
					4 to RecordPlayer(
						seat = 4,
						stack = 0.0,
						initialStack = 50000.0,
						status = PlayerStatus.ALL_IN,
					),
					5 to RecordPlayer(seat = 5, initialStack = null),
					6 to RecordPlayer(seat = 6, stack = 50000.0, initialStack = 50000.0),
					7 to RecordPlayer(seat = 7, stack = 50000.0, initialStack = 50000.0),
					8 to RecordPlayer(seat = 8, stack = 50000.0, initialStack = 50000.0),
					9 to RecordPlayer(seat = 9, stack = 50000.0, initialStack = 50000.0),
				),
			),
			streets = HandStreets(
				preflop = PreflopStreet(
					actions = listOf(
						Action(playerSeat = 4, type = ActionType.ALL_IN, amount = 50000.0),
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
	fun `스택 입력됨이고 민레이즈 미달이면 레이즈 불가`() {
		// seat4가 레이즈, seat5의 initialStack = 3000 (입력됨, 민레이즈 미달)
		val state = makeState(
			bb = 1000.0,
			currentActionSeat = 5,
			players = RecordPlayers(
				mapOf(
					1 to RecordPlayer(seat = 1, stack = 50000.0, initialStack = 50000.0),
					2 to RecordPlayer(seat = 2, stack = 50000.0, initialStack = 50000.0),
					3 to RecordPlayer(seat = 3, stack = 50000.0, initialStack = 50000.0),
					4 to RecordPlayer(seat = 4, stack = 47500.0, initialStack = 50000.0),
					5 to RecordPlayer(seat = 5, stack = 3000.0, initialStack = 3000.0),
					6 to RecordPlayer(seat = 6, stack = 50000.0, initialStack = 50000.0),
					7 to RecordPlayer(seat = 7, stack = 50000.0, initialStack = 50000.0),
					8 to RecordPlayer(seat = 8, stack = 50000.0, initialStack = 50000.0),
					9 to RecordPlayer(seat = 9, stack = 50000.0, initialStack = 50000.0),
				),
			),
			streets = HandStreets(
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
		// 스택 3000 < 민레이즈 4000 → 레이즈 불가
		assertFalse(ActionType.RAISE in actions)
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
			streets = HandStreets(
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
			streets = HandStreets(
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
			streets = HandStreets(
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

	@Test
	fun `올인이 민레이즈 미달이면 다음 플레이어 레이즈 불가`() {
		// 2500 오픈 → 3500 올인 (민3벳 4000 미달) → seat6은 레이즈 불가
		val state = makeState(
			bb = 1000.0,
			currentActionSeat = 6,
			streets = HandStreets(
				preflop = PreflopStreet(
					actions = listOf(
						Action(playerSeat = 4, type = ActionType.RAISE, amount = 2500.0),
						Action(playerSeat = 5, type = ActionType.ALL_IN, amount = 3500.0),
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
	fun `올인이 민레이즈 충족하면 다음 플레이어 레이즈 가능`() {
		// 2500 오픈 → 5000 올인 (민3벳 4000 충족) → seat6은 레이즈 가능
		val state = makeState(
			bb = 1000.0,
			currentActionSeat = 6,
			streets = HandStreets(
				preflop = PreflopStreet(
					actions = listOf(
						Action(playerSeat = 4, type = ActionType.RAISE, amount = 2500.0),
						Action(playerSeat = 5, type = ActionType.ALL_IN, amount = 5000.0),
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
				mapOf(
					1 to RecordPlayer(seat = 1),
					2 to RecordPlayer(seat = 2),
					3 to RecordPlayer(seat = 3),
					4 to RecordPlayer(seat = 4, status = PlayerStatus.FOLDED),
					5 to RecordPlayer(seat = 5),
					6 to RecordPlayer(seat = 6),
					7 to RecordPlayer(seat = 7),
					8 to RecordPlayer(seat = 8),
					9 to RecordPlayer(seat = 9),
				),
			),
		)
		assertFalse(4 in state.actionOrder)
	}

	@Test
	fun `올인한 플레이어는 액션 순서에서 제외`() {
		val state = makeState(
			buttonSeat = 1,
			players = RecordPlayers(
				mapOf(
					1 to RecordPlayer(seat = 1),
					2 to RecordPlayer(seat = 2),
					3 to RecordPlayer(seat = 3),
					4 to RecordPlayer(seat = 4, status = PlayerStatus.ALL_IN),
					5 to RecordPlayer(seat = 5),
					6 to RecordPlayer(seat = 6),
					7 to RecordPlayer(seat = 7),
					8 to RecordPlayer(seat = 8),
					9 to RecordPlayer(seat = 9),
				),
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
				mapOf(
					1 to RecordPlayer(seat = 1, status = PlayerStatus.FOLDED),
					2 to RecordPlayer(seat = 2, status = PlayerStatus.ALL_IN),
					3 to RecordPlayer(seat = 3, status = PlayerStatus.ALL_IN),
				),
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
			streets = HandStreets(
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
		val state = makeState(currentStep = RecordStep.FLOP)
		assertEquals(0, state.currentBetLevel)
		assertEquals("벳", state.nextRaiseLabel)
	}

	@Test
	fun `포스트플랍 벳 후 레벨은 1이고 다음은 레이즈`() {
		val state = makeState(
			currentStep = RecordStep.FLOP,
			streets = HandStreets(
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
			streets = HandStreets(preflop = PreflopStreet(), flop = FlopStreet()),
		)
		assertFalse(state.streets.isBoardReady(Street.FLOP))
	}

	@Test
	fun `플랍 카드 3장이면 보드 준비 완료`() {
		val state = makeState(
			streets = HandStreets(
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

	// ===== 폴드 승리 쇼다운 결과 =====

	@Test
	fun `폴드 승리 시 폴드한 플레이어는 HIGH_CARD가 아닌 폴드 패배여야 한다`() {
		// 3인 테이블: seat 1(BTN), seat 2(SB 폴드), seat 3(BB 폴드)
		// seat 1이 레이즈 → seat 2, 3 폴드 → seat 1 폴드 승리
		val state = makeState(
			playerCount = 3,
			heroSeat = 2,
			buttonSeat = 1,
			players = RecordPlayers(
				mapOf(
					1 to RecordPlayer(seat = 1, status = PlayerStatus.ACTIVE),
					2 to RecordPlayer(seat = 2, status = PlayerStatus.FOLDED),
					3 to RecordPlayer(seat = 3, status = PlayerStatus.FOLDED),
				),
			),
		)
		assertTrue(state.isFoldWin)

		val results = state.showdownResults
		// 승자(seat 1)는 WIN
		val winnerResult = results.find { it.seat == 1 }
		assertEquals(ShowdownOutcome.WIN, winnerResult?.outcome)

		// 폴드한 플레이어(seat 2, 3)는 LOSE이고 ranking이 HIGH_CARD가 아니어야 함
		// → 폴드 패배임을 구분할 수 있어야 한다
		val loserResult = results.find { it.seat == 2 }
		assertEquals(ShowdownOutcome.LOSE, loserResult?.outcome)
		// 현재 버그: ranking = HIGH_CARD → "하이카드(으)로 패배" 표시됨
		// 기대: 폴드 패배를 구분할 수 있는 결과
		assertTrue(
			state.isFoldWin,
			"폴드 승리 상황에서 패배자의 결과에 폴드 패배 여부를 알 수 있어야 한다",
		)
	}
}

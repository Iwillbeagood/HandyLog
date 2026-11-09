package com.hand.log.domain.model.quick

import com.hand.log.domain.model.ActionType
import com.hand.log.domain.model.Blinds
import com.hand.log.domain.model.Card
import com.hand.log.domain.model.HandRanking
import com.hand.log.domain.model.Position
import com.hand.log.domain.model.Rank
import com.hand.log.domain.model.SeatRing
import com.hand.log.domain.model.ShowdownEntry
import com.hand.log.domain.model.ShowdownOutcome
import com.hand.log.domain.model.ShowdownResult
import com.hand.log.domain.model.Street
import com.hand.log.domain.model.Suit
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class QuickHandMachineTest {

	private val machine = QuickHandMachine(
		QuickHandConfig(
			tableId = "t1",
			heroSeat = 4,
			seats = (1..6).toList(),
			blinds = Blinds(sb = 500.0, bb = 1000.0),
			stackInBb = 100.0,
		),
	)

	private val heroWins: (List<Card>, List<ShowdownEntry>) -> List<ShowdownResult> = { _, entries ->
		entries.map {
			ShowdownResult(
				seat = it.seat,
				ranking = HandRanking.FULL_HOUSE,
				outcome = if (it.seat == 4) ShowdownOutcome.WIN else ShowdownOutcome.LOSE,
			)
		}
	}

	private fun seat(p: Position) = QuickInput.Seat(p)
	private fun act(t: ActionType) = QuickInput.Act(t)
	private fun done(t: ActionType) = listOf(QuickInput.Act(t), QuickInput.NextLine)
	private val nl = QuickInput.NextLine
	private fun rank(r: Rank) = QuickInput.CardRank(r)
	private fun card(r: Rank, s: Suit) = listOf(QuickInput.CardRank(r), QuickInput.CardSuit(s))
	private fun amount(text: String) = text.map {
		if (it == '.') QuickInput.Dot else QuickInput.Digit(it.digitToInt())
	}

	private val preflop = amount("3") + done(ActionType.RAISE) +
		seat(Position.BTN) + QuickInput.Hero(Position.BTN) + rank(Rank.FIVE) + rank(Rank.FIVE) + done(ActionType.CALL) +
		done(ActionType.FOLD) + done(ActionType.FOLD)

	private val flop = listOf(rank(Rank.ACE), rank(Rank.KING), rank(Rank.FIVE), nl) +
		QuickInput.PotSize(BetSize.HALF) + done(ActionType.BET) + done(ActionType.CALL)

	private val turn = card(Rank.ACE, Suit.SPADES) + nl + done(ActionType.CHECK) + amount("10") + done(ActionType.BET) + done(ActionType.CALL)

	private val river = card(Rank.TEN, Suit.SPADES) + nl + done(ActionType.CHECK) + done(ActionType.ALL_IN) + done(ActionType.CALL)

	private val fullHand = preflop + flop + turn + river

	@Test
	fun `히어로 지정 없이 첫 액터부터 바로 액션을 입력한다`() {
		val snapshot = machine.replay(emptyList())
		assertEquals(QuickPhase.ACTION, snapshot.phase)
		assertNull(snapshot.heroPosition)
		assertEquals(Position.UTG, snapshot.actorPosition)
		assertTrue(QuickInput.Hero(Position.BB) in snapshot.allowed)
		assertTrue(QuickInput.Digit(3) in snapshot.allowed)
	}

	@Test
	fun `금액을 먼저 입력하고 레이즈를 눌러도 기록된다`() {
		val snapshot = machine.replay(amount("3") + done(ActionType.RAISE))
		val raise = snapshot.record.streets.preflop.actions.single()
		assertEquals(ActionType.RAISE, raise.type)
		assertEquals(3000.0, raise.amount)
		assertEquals(Position.MP, snapshot.actorPosition)
	}

	@Test
	fun `히어로는 무늬 없이 랭크만 입력하고 바로 액션할 수 있다`() {
		val snapshot = machine.replay(preflop.take(7) + done(ActionType.CALL))
		assertEquals(Position.BTN, snapshot.heroPosition)
		assertEquals(4, snapshot.record.buttonSeat)
		val hero = snapshot.record.players.first { it.isHero }
		assertEquals(Card(Rank.FIVE), hero.cards?.card1)
		assertEquals(Card(Rank.FIVE), hero.cards?.card2)
		assertEquals(ActionType.CALL, snapshot.record.streets.preflop.actions.last().type)
	}

	@Test
	fun `히어로 카드 없이 액션해도 된다`() {
		val snapshot = machine.replay(
			amount("3") + done(ActionType.RAISE) + seat(Position.BTN) + QuickInput.Hero(Position.BTN) + done(ActionType.CALL),
		)
		assertEquals(ActionType.CALL, snapshot.record.streets.preflop.actions.last().type)
		assertNull(snapshot.record.players.first { it.isHero }.cards)
	}

	private val raiseAndCall = amount("3") + done(ActionType.RAISE) + seat(Position.CO) + done(ActionType.CALL)

	@Test
	fun `빈 줄에서 스트릿을 닫으면 남은 플레이어를 폴드하고 플랍으로 넘어간다`() {
		val before = machine.replay(raiseAndCall)
		assertTrue(QuickInput.CloseStreet in before.allowed)

		val snapshot = machine.replay(raiseAndCall + QuickInput.CloseStreet)
		assertEquals(QuickPhase.BOARD, snapshot.phase)
		assertEquals(Street.FLOP, snapshot.street)
		val folds = snapshot.record.streets.preflop.actions.filter { it.type == ActionType.FOLD }
		assertEquals(4, folds.size)
	}

	@Test
	fun `스트릿을 닫을 때 베팅을 마주한 히어로 차례에서 멈춘다`() {
		val inputs = raiseAndCall + QuickInput.Hero(Position.BB) + nl + QuickInput.CloseStreet
		val snapshot = machine.replay(inputs)
		assertEquals(QuickPhase.ACTION, snapshot.phase)
		assertEquals(Position.BB, snapshot.actorPosition)
		assertFalse(QuickInput.CloseStreet in snapshot.allowed)
	}

	@Test
	fun `프리플랍에 액션이 없으면 스트릿을 닫을 수 없다`() {
		assertFalse(QuickInput.CloseStreet in machine.replay(emptyList()).allowed)
	}

	@Test
	fun `포스트플랍에서 스트릿을 닫으면 남은 플레이어는 체크하고 다음 스트릿으로 넘어간다`() {
		val flopBoard = listOf(rank(Rank.ACE), rank(Rank.KING), rank(Rank.FIVE), nl)
		val snapshot = machine.replay(preflop + flopBoard + QuickInput.CloseStreet)
		assertEquals(Street.TURN, snapshot.street)
		assertTrue(snapshot.record.streets.flop?.actions.orEmpty().all { it.type == ActionType.CHECK })
	}

	@Test
	fun `라운드가 끝나면 다음 스트릿 보드 입력으로 자동 진행된다`() {
		val snapshot = machine.replay(preflop)
		assertEquals(QuickPhase.BOARD, snapshot.phase)
		assertEquals(Street.FLOP, snapshot.street)
	}

	@Test
	fun `보드를 랭크만 입력하고 줄넘김하면 카드가 확정되고 2웨이 첫 액터가 제시된다`() {
		val boardOnly = machine.replay(preflop + flop.take(3))
		assertEquals(QuickPhase.BOARD, boardOnly.phase)
		assertTrue(QuickInput.NextLine in boardOnly.allowed)
		assertTrue(QuickInput.PotSize(BetSize.HALF) in boardOnly.allowed)

		val afterBet = machine.replay(preflop + flop.take(7))
		assertEquals(
			listOf(Card(Rank.ACE), Card(Rank.KING), Card(Rank.FIVE)),
			afterBet.record.streets.flop?.cards,
		)
		assertEquals(Position.BTN, afterBet.actorPosition)
		val bet = afterBet.record.streets.flop?.actions?.single()
		assertEquals(3800.0, bet?.amount)
	}

	private fun fill(owner: CardOwner, vararg cards: Card) = QuickInput.FillCards(
		owner,
		cards.toList(),
	)

	@Test
	fun `리버 액션이 끝나면 마무리 단계로 넘어가 상대 핸드와 무늬 없는 카드를 한 번에 제공한다`() {
		val review = machine.replay(fullHand)
		assertEquals(QuickPhase.REVIEW, review.phase)
		assertTrue(review.canSave)
		assertNull(review.pending)
		val finish = assertNotNull(review.finish)
		assertEquals(listOf(QuickShowdownHand(1, Position.UTG, emptyList())), finish.showdown)
		assertEquals(listOf(Card(Rank.FIVE), Card(Rank.FIVE)), finish.heroCards)
		assertEquals(Card(Rank.ACE), finish.board.first())
		assertFalse(finish.canSelectHeroPosition)
	}

	@Test
	fun `마무리 단계에서 카드를 지정하면 같은 대상의 이전 입력을 대체하고 저장된다`() {
		val opponent =
			fill(
				CardOwner.Opponent(Position.UTG),
				Card(Rank.QUEEN, Suit.CLUBS),
				Card(Rank.QUEEN, Suit.DIAMONDS),
			)
		val hero = fill(CardOwner.Hero, Card(Rank.FIVE, Suit.HEARTS), Card(Rank.FIVE, Suit.CLUBS))
		val first = machine.appendFinish(
			fullHand + opponent,
			fill(CardOwner.Hero, Card(Rank.TWO, Suit.HEARTS), Card(Rank.TWO, Suit.CLUBS)),
		)
		val inputs = machine.appendFinish(first, hero)
		assertEquals(fullHand + opponent + hero, inputs)

		val record = assertNotNull(machine.build(inputs, heroWins))
		val heroPlayer = record.players.first { it.isHero }
		assertEquals(Card(Rank.FIVE, Suit.HEARTS), heroPlayer.cards?.card1)
		assertEquals(Card(Rank.QUEEN, Suit.CLUBS), record.players.first { it.seat == 1 }.cards?.card1)
		assertNull(record.streets.flop?.cards?.first()?.suit)
		assertEquals(ShowdownOutcome.WIN, heroPlayer.outcome)
		assertEquals(101500.0, record.result)
	}

	@Test
	fun `보드 카드를 지정하면 스트릿별로 나눠 채운다`() {
		val board = listOf(
			Card(Rank.ACE, Suit.HEARTS),
			Card(Rank.KING, Suit.HEARTS),
			Card(Rank.FIVE, Suit.DIAMONDS),
			Card(Rank.ACE, Suit.SPADES),
			Card(Rank.TEN, Suit.SPADES),
		)
		val record = machine.replay(fullHand + QuickInput.FillCards(CardOwner.Board, board)).record
		assertEquals(board.take(3), record.streets.flop?.cards)
		assertEquals(board, record.streets.boardCards)
	}

	@Test
	fun `이미 쓴 카드와 겹치거나 장수가 맞지 않으면 무시된다`() {
		val duplicate =
			fill(CardOwner.Opponent(Position.UTG), Card(Rank.ACE, Suit.SPADES), Card(Rank.QUEEN, Suit.CLUBS))
		assertEquals(emptyList(), machine.replay(fullHand + duplicate).finish?.showdown?.single()?.cards)

		val single = fill(CardOwner.Opponent(Position.UTG), Card(Rank.QUEEN, Suit.CLUBS))
		assertEquals(emptyList(), machine.replay(fullHand + single).finish?.showdown?.single()?.cards)
	}

	@Test
	fun `액션 수정은 마무리 입력과 마지막 액션을 되돌린다`() {
		val inputs = fullHand + fill(CardOwner.Opponent(Position.UTG), Card(Rank.QUEEN, Suit.CLUBS), Card(Rank.QUEEN, Suit.DIAMONDS))
		assertEquals(fullHand.dropLast(1), machine.reopenActions(inputs))
		assertEquals(QuickPhase.ACTION, machine.replay(machine.reopenActions(inputs)).phase)
	}

	@Test
	fun `히어로 없이 핸드가 끝나면 마무리 단계에서 히어로 포지션을 고른다`() {
		val folds = amount("2.5") + done(ActionType.RAISE) + List(5) { done(ActionType.FOLD) }.flatten()
		val review = machine.replay(folds)
		assertTrue(assertNotNull(review.finish).canSelectHeroPosition)
		assertFalse(review.canSave)

		val marked = machine.replay(folds + seat(Position.BB))
		assertTrue(marked.canSave)
		assertEquals(4, marked.record.heroSeat)
		assertEquals(Position.BB, marked.heroPosition)
		assertTrue(assertNotNull(marked.finish).canSelectHeroPosition)

		val changed = machine.appendFinish(folds + seat(Position.BB), seat(Position.SB))
		assertEquals(folds + seat(Position.SB), changed)
		val record = assertNotNull(machine.build(changed, heroWins))
		assertEquals(-500.0, record.result)
	}

	@Test
	fun `마무리 단계의 히어로 포지션은 직접 액션한 포지션만 고를 수 있고 입력한 카드를 히어로 카드로 가져온다`() {
		val inputs = listOf(seat(Position.UTG), rank(Rank.ACE), rank(Rank.KING), act(ActionType.RAISE)) + amount("3") + nl +
			seat(Position.BB) + done(ActionType.FOLD)
		val review = machine.replay(inputs)
		assertEquals(QuickPhase.REVIEW, review.phase)
		assertEquals(listOf(Position.UTG, Position.BB), review.involvedPositions)
		assertEquals(
			setOf(seat(Position.UTG), seat(Position.BB)),
			review.allowed.filterIsInstance<QuickInput.Seat>().toSet(),
		)
		assertNull(machine.replay(inputs + seat(Position.CO)).heroPosition)

		val marked = machine.replay(inputs + seat(Position.UTG))
		assertEquals(Position.UTG, marked.heroPosition)
		assertEquals(listOf(Card(Rank.ACE), Card(Rank.KING)), assertNotNull(marked.finish).heroCards)
		assertTrue(marked.opponentHands.none { it.cards.isNotEmpty() })
	}

	@Test
	fun `차례가 아닌 포지션도 히어로로 지정할 수 있고 액터는 그대로다`() {
		val snapshot = machine.replay(listOf(QuickInput.Hero(Position.CO), rank(Rank.ACE)))
		assertEquals(Position.CO, snapshot.heroPosition)
		assertEquals(Position.UTG, snapshot.actorPosition)
		assertEquals(QuickPhase.HERO_CARDS, snapshot.phase)
		assertFalse(QuickInput.Hero(Position.BTN) in snapshot.allowed)
	}

	@Test
	fun `줄넘김은 입력 중인 줄을 확정한다`() {
		val heroCards = listOf(QuickInput.Hero(Position.UTG), rank(Rank.ACE), QuickInput.NextLine)
		val afterHero = machine.replay(heroCards)
		assertEquals(QuickPhase.ACTION, afterHero.phase)
		assertIs<QuickPending.Actor>(afterHero.pending)

		val board = machine.replay(preflop + rank(Rank.ACE) + rank(Rank.KING) + rank(Rank.FIVE))
		assertTrue(QuickInput.NextLine in board.allowed)
		val committed = machine.replay(
			preflop + rank(Rank.ACE) + rank(Rank.KING) + rank(Rank.FIVE) + QuickInput.NextLine,
		)
		assertEquals(QuickPhase.ACTION, committed.phase)
		assertEquals(Position.UTG, committed.actorPosition)

		val amount = machine.replay(listOf(act(ActionType.RAISE)) + amount("3") + QuickInput.NextLine)
		assertEquals(3000.0, amount.record.streets.preflop.actions.single().amount)
	}

	@Test
	fun `액션을 눌러도 줄넘김 전까지는 확정되지 않고 다른 액션으로 바꿀 수 있다`() {
		val staged = machine.replay(listOf(act(ActionType.CALL)))
		assertTrue(staged.record.streets.preflop.actions.isEmpty())
		assertEquals(Position.UTG, staged.actorPosition)
		assertIs<QuickPending.Staged>(staged.pending)

		val changed = machine.replay(listOf(act(ActionType.CALL), act(ActionType.FOLD), nl))
		assertEquals(ActionType.FOLD, changed.record.streets.preflop.actions.single().type)
		assertEquals(Position.MP, changed.actorPosition)
	}

	@Test
	fun `보드 카드를 다 입력하면 자동으로 액션 단계로 넘어간다`() {
		val board = card(Rank.ACE, Suit.HEARTS) + card(Rank.KING, Suit.HEARTS) + card(Rank.FIVE, Suit.CLUBS)
		val full = machine.replay(preflop + board)
		assertEquals(QuickPhase.ACTION, full.phase)
		assertEquals(board.size / 2, full.record.streets.flop?.cards?.size)
	}

	@Test
	fun `히어로를 지정하면 처음에 추가되고 이미 있으면 같은 자리에서 바뀐다`() {
		val inputs = amount("3") + done(ActionType.RAISE)
		val added = machine.withHero(inputs, Position.BTN)
		assertEquals(listOf(QuickInput.Hero(Position.BTN)) + inputs, added)
		assertEquals(Position.BTN, machine.replay(added).heroPosition)
		assertEquals(3000.0, machine.replay(added).record.streets.preflop.actions.single().amount)

		val changed = machine.withHero(preflop, Position.CO)
		assertEquals(
			preflop.map {
				if (it is QuickInput.Hero) QuickInput.Hero(Position.CO) else it
			},
			changed,
		)
		assertEquals(Position.CO, machine.replay(changed).heroPosition)
	}

	@Test
	fun `뒤 포지션을 누른 직후에는 이전 포지션으로 다시 선택할 수 있다`() {
		val jumped = machine.replay(listOf(seat(Position.CO)))
		assertEquals(Position.CO, jumped.actorPosition)
		assertTrue(seat(Position.UTG) in jumped.allowed)

		val reselected = machine.append(listOf(seat(Position.CO)), seat(Position.UTG))
		assertEquals(listOf(seat(Position.UTG)), reselected)
		val snapshot = machine.replay(reselected)
		assertEquals(Position.UTG, snapshot.actorPosition)
		assertTrue(snapshot.record.streets.preflop.actions.isEmpty())
	}

	@Test
	fun `액션 후에는 이미 행동한 포지션은 다시 선택할 수 없고 남은 포지션으로만 바꾼다`() {
		val inputs = amount("3") + done(ActionType.RAISE) + seat(Position.BTN)
		assertFalse(seat(Position.UTG) in machine.replay(inputs).allowed)
		assertEquals(inputs.dropLast(1) + seat(Position.SB), machine.append(inputs, seat(Position.SB)))
	}

	@Test
	fun `팟 비율 키는 현재 팟 기준 레이즈 금액을 채운다`() {
		val snapshot = machine.replay(preflop + flop.take(4) + QuickInput.PotSize(BetSize.POT))
		assertEquals("7.5", assertIs<QuickPending.Amount>(snapshot.pending).text)
	}

	@Test
	fun `벳이 나온 뒤 팟 비율 키는 벳의 배수로 레이즈 금액을 채운다`() {
		val afterBet = preflop + flop.take(7)
		assertTrue(machine.replay(afterBet).isBetOpen)
		val snapshot = machine.replay(afterBet + QuickInput.PotSize(BetSize.HALF))
		assertEquals("9.5", assertIs<QuickPending.Amount>(snapshot.pending).text)
	}

	@Test
	fun `프리플랍에서 아무도 참여하지 않고 모두 폴드되는 입력은 막는다`() {
		val start = machine.replay(emptyList())
		assertFalse(seat(Position.BB) in start.allowed)
		assertTrue(seat(Position.SB) in start.allowed)

		val toSb = machine.replay(List(4) { done(ActionType.FOLD) }.flatten())
		assertEquals(Position.SB, toSb.actorPosition)
		assertFalse(act(ActionType.FOLD) in toSb.allowed)
		assertTrue(act(ActionType.CALL) in toSb.allowed)
	}

	@Test
	fun `프리플랍에서는 팟 비율 키를 쓸 수 없다`() {
		val snapshot = machine.replay(listOf(QuickInput.PotSize(BetSize.POT)))
		assertNull(snapshot.pending as? QuickPending.Amount)
		assertTrue(machine.replay(emptyList()).allowed.none { it is QuickInput.PotSize })
	}

	@Test
	fun `보드 단계에서 카드 선택으로 보드를 채우면 바로 액션 단계로 넘어간다`() {
		val board =
			listOf(Card(Rank.ACE, Suit.HEARTS), Card(Rank.KING, Suit.HEARTS), Card(Rank.FIVE, Suit.CLUBS))
		assertEquals(3, machine.replay(preflop).boardSlots)
		val inputs = machine.withCards(preflop, QuickInput.FillCards(CardOwner.Board, board))
		val snapshot = machine.replay(inputs)

		assertEquals(QuickPhase.ACTION, snapshot.phase)
		assertEquals(board, snapshot.record.streets.flop?.cards)
		assertEquals(0, snapshot.boardSlots)
	}

	@Test
	fun `플랍부터는 차례 포지션이 미리 지정된 상태로 제시된다`() {
		val preflopActor = assertIs<QuickPending.Actor>(machine.replay(emptyList()).pending)
		assertFalse(preflopActor.isChosen)

		val flopActor = assertIs<QuickPending.Actor>(machine.replay(preflop + flop.take(4)).pending)
		assertTrue(flopActor.isChosen)
	}

	@Test
	fun `액션 입력 도중에도 히어로와 상대 핸드를 지정할 수 있다`() {
		val base = amount("3") + done(ActionType.RAISE) + seat(Position.BTN) + QuickInput.Hero(Position.BTN) + nl
		val hero = QuickInput.FillCards(
			CardOwner.Hero,
			listOf(Card(Rank.ACE, Suit.SPADES), Card(Rank.KING, Suit.SPADES)),
		)
		val utg = QuickInput.FillCards(
			CardOwner.Opponent(Position.UTG),
			listOf(Card(Rank.QUEEN, Suit.CLUBS), Card(Rank.QUEEN, Suit.DIAMONDS)),
		)
		val inputs = machine.withCards(machine.withCards(base, hero), utg)
		val snapshot = machine.replay(inputs)

		assertEquals(QuickPhase.ACTION, snapshot.phase)
		assertEquals(hero.cards, snapshot.heroCards)
		assertEquals(utg.cards, snapshot.opponentHands.first { it.seat == 1 }.cards)
		assertTrue(Card(Rank.ACE, Suit.SPADES) in snapshot.usedCards)

		val replaced = machine.withCards(inputs, QuickInput.FillCards(CardOwner.Hero, emptyList()))
		assertEquals(1, replaced.count { it is QuickInput.FillCards && it.owner == CardOwner.Hero })
		assertTrue(machine.replay(replaced).heroCards.isEmpty())
	}

	@Test
	fun `테이블 없이 BB 단위로 기록하면 tableId가 null인 핸드가 만들어진다`() {
		val tableless = QuickHandMachine(
			QuickHandConfig(
				tableId = null,
				heroSeat = QuickHandConfig.HERO_SEAT_WITHOUT_TABLE,
				seats = (1..QuickHandConfig.DEFAULT_PLAYER_COUNT).toList(),
				blinds = QuickHandConfig.BB_UNIT_BLINDS,
			),
		)
		val inputs = listOf(seat(Position.BTN), QuickInput.Hero(Position.BTN), act(ActionType.RAISE)) + amount("2.5") + nl +
			done(ActionType.FOLD) + done(ActionType.FOLD)
		val record = assertNotNull(tableless.build(inputs, heroWins))

		assertNull(record.tableId)
		assertEquals(Position.BTN, record.heroPosition)
		assertEquals(QuickHandConfig.DEFAULT_PLAYER_COUNT, record.players.size)
		assertEquals(1.5, record.result)
	}

	@Test
	fun `스택보다 큰 레이즈도 올인으로 바뀌지 않고 레이즈로 기록된다`() {
		val snapshot = machine.replay(amount("120") + done(ActionType.RAISE))
		val raise = snapshot.record.streets.preflop.actions.last()

		assertEquals(ActionType.RAISE, raise.type)
		assertEquals(120_000.0, raise.amount)
		assertTrue(act(ActionType.RAISE) in snapshot.allowed)
	}

	@Test
	fun `스택보다 큰 베팅에 콜하면 올인이 아닌 콜로 기록된다`() {
		val record = machine.replay(amount("120") + done(ActionType.RAISE) + done(ActionType.CALL)).record

		assertEquals(ActionType.CALL, record.streets.preflop.actions.last().type)
	}

	@Test
	fun `스택을 모르면 큰 금액도 제한 없이 기록하고 스택은 비워 둔다`() {
		val unknown = QuickHandMachine(
			QuickHandConfig(
				tableId = "t1",
				heroSeat = 4,
				seats = (1..6).toList(),
				blinds = Blinds(sb = 500.0, bb = 1000.0),
			),
		)
		val inputs = listOf(seat(Position.BTN), QuickInput.Hero(Position.BTN), nl) + amount("500") + done(ActionType.RAISE)
		val record = unknown.replay(inputs).record

		assertEquals(500_000.0, record.streets.preflop.actions.last().amount)
		assertNull(record.streets.preflop.actions.last().stackBefore)
		assertNull(record.players.first { it.isHero }.initialStack)
	}

	@Test
	fun `스택을 몰라도 올인은 금액 없이 넘어가고 금액을 함께 기록할 수도 있다`() {
		val unknown = QuickHandMachine(
			QuickHandConfig(
				tableId = "t1",
				heroSeat = 4,
				seats = (1..6).toList(),
				blinds = Blinds(sb = 500.0, bb = 1000.0),
			),
		)
		val staged = unknown.replay(listOf(act(ActionType.ALL_IN)))
		assertIs<QuickPending.Amount>(staged.pending)
		assertTrue(nl in staged.allowed)

		val amountless = unknown.replay(listOf(act(ActionType.ALL_IN), nl))
		assertEquals(ActionType.ALL_IN, amountless.record.streets.preflop.actions.single().type)
		assertEquals(Position.MP, amountless.actorPosition)

		val shove = unknown.replay(
			listOf(act(ActionType.ALL_IN)) + amount("40") + nl,
		).record.streets.preflop.actions.single()
		assertEquals(ActionType.ALL_IN, shove.type)
		assertEquals(40_000.0, shove.amount)
	}

	@Test
	fun `칩 단위로 전환하면 입력 금액을 칩으로 해석한다`() {
		val inputs = listOf(QuickInput.ToggleAmountUnit) + amount("2500") + done(ActionType.RAISE)
		val snapshot = machine.replay(inputs)

		assertTrue(snapshot.amountInChips)
		assertEquals(2500.0, snapshot.record.streets.preflop.actions.last().amount)
	}

	@Test
	fun `블라인드 없이도 단위를 전환할 수 있고 금액은 그대로 해석된다`() {
		val tableless = QuickHandMachine(
			QuickHandConfig(
				tableId = null,
				heroSeat = QuickHandConfig.HERO_SEAT_WITHOUT_TABLE,
				seats = (1..QuickHandConfig.DEFAULT_PLAYER_COUNT).toList(),
				blinds = QuickHandConfig.BB_UNIT_BLINDS,
			),
		)
		assertTrue(QuickInput.ToggleAmountUnit in tableless.replay(emptyList()).allowed)
		val inputs = listOf(QuickInput.ToggleAmountUnit) + amount("3") + done(ActionType.RAISE)
		assertEquals(3.0, tableless.replay(inputs).record.streets.preflop.actions.single().amount)
	}

	@Test
	fun `금액이 현재 최대 베팅 이하면 레이즈가 막힌다`() {
		val low = machine.replay(listOf(act(ActionType.RAISE)) + amount("1"))
		assertFalse(nl in low.allowed)
		val valid = machine.replay(listOf(act(ActionType.RAISE)) + amount("3"))
		assertTrue(nl in valid.allowed)
	}

	@Test
	fun `테이블 없는 단일 핸드는 7인 UTG MP HJ CO 표기만 쓴다`() {
		val tableless = QuickHandMachine(
			QuickHandConfig(
				tableId = null,
				heroSeat = QuickHandConfig.HERO_SEAT_WITHOUT_TABLE,
				seats = (1..QuickHandConfig.DEFAULT_PLAYER_COUNT).toList(),
				blinds = QuickHandConfig.BB_UNIT_BLINDS,
			),
		)
		val snapshot = tableless.replay(emptyList())
		assertEquals(
			setOf(
				Position.UTG,
				Position.MP,
				Position.HJ,
				Position.CO,
				Position.BTN,
				Position.SB,
				Position.BB,
			),
			snapshot.positions.toSet(),
		)
		assertEquals(Position.LJ, SeatRing((1..7).toList(), buttonSeat = 1).positionOf(5))
	}

	@Test
	fun `빈 좌석이 있어도 포지션이 좌석 순서대로 계산된다`() {
		val ring = SeatRing(listOf(1, 2, 4, 5, 7, 8), buttonSeat = 1)
		assertEquals(Position.SB, ring.positionOf(2))
		assertEquals(Position.BB, ring.positionOf(4))
		assertEquals(Position.UTG, ring.positionOf(5))
		assertEquals(Position.MP, ring.positionOf(7))
		assertEquals(Position.CO, ring.positionOf(8))
	}

	private val unknownStack = QuickHandMachine(
		QuickHandConfig(
			tableId = "t1",
			heroSeat = 4,
			seats = (1..6).toList(),
			blinds = Blinds(sb = 500.0, bb = 1000.0),
		),
	)

	private fun allowedActs(snapshot: QuickHandSnapshot) =
		snapshot.allowed.filterIsInstance<QuickInput.Act>().map { it.type }.toSet()

	@Test
	fun `스택을 모를 때 금액 없는 올인은 금액 없이 기록되고 콜도 금액 없이 기록된다`() {
		val snapshot = unknownStack.replay(listOf(act(ActionType.ALL_IN), nl) + done(ActionType.CALL))
		val actions = snapshot.record.streets.preflop.actions
		assertEquals(ActionType.ALL_IN, actions[0].type)
		assertNull(actions[0].amount)
		assertEquals(ActionType.CALL, actions[1].type)
		assertNull(actions[1].amount)
	}

	@Test
	fun `금액 없는 올인 뒤 다음 액터는 폴드와 콜만 할 수 있다`() {
		val snapshot = unknownStack.replay(listOf(act(ActionType.ALL_IN), nl))
		assertEquals(setOf(ActionType.FOLD, ActionType.CALL), allowedActs(snapshot))
		assertFalse(QuickInput.Digit(1) in snapshot.allowed)
	}

	@Test
	fun `스택을 덮는 올인 뒤 다음 액터는 폴드와 콜만 할 수 있다`() {
		val snapshot = machine.replay(done(ActionType.ALL_IN))
		assertEquals(setOf(ActionType.FOLD, ActionType.CALL), allowedActs(snapshot))
	}

	@Test
	fun `최소 레이즈에 못 미치는 올인 뒤에는 레이즈할 수 없다`() {
		val shortStack = QuickHandMachine(
			QuickHandConfig(
				tableId = "t1",
				heroSeat = 4,
				seats = (1..6).toList(),
				blinds = Blinds(sb = 500.0, bb = 1000.0),
			),
		)
		val inputs = amount("10") + done(ActionType.RAISE) + act(ActionType.ALL_IN) + amount("12") + nl
		assertEquals(setOf(ActionType.FOLD, ActionType.CALL), allowedActs(shortStack.replay(inputs)))
	}

	@Test
	fun `팟 비율로 입력한 벳은 비율을 함께 기록하고 숫자를 이어 치면 비율이 해제된다`() {
		val sized = machine.replay(preflop + flop.take(7)).record.streets.flop?.actions?.single()
		assertEquals(BetSize.HALF, sized?.potSize)

		val typed = machine.replay(
			preflop + flop.take(4) + QuickInput.PotSize(BetSize.TWO_THIRDS) + QuickInput.Digit(0) + done(ActionType.BET),
		).record.streets.flop?.actions?.single()
		assertNull(typed?.potSize)
	}

	@Test
	fun `스택과 같은 금액의 레이즈는 추가 스택 없이 올인으로 기록된다`() {
		val snapshot = machine.replay(amount("100") + done(ActionType.RAISE))
		val shove = snapshot.record.streets.preflop.actions.last()
		assertEquals(ActionType.ALL_IN, shove.type)
		assertEquals(100_000.0, shove.amount)
		assertEquals(0.0, shove.stackAfter)
		assertEquals(setOf(ActionType.FOLD, ActionType.CALL), allowedActs(snapshot))
	}

	@Test
	fun `팟 비율 입력 뒤 단위를 바꾸면 같은 금액이 새 단위로 다시 채워진다`() {
		val inputs = preflop + flop.take(4) + QuickInput.PotSize(BetSize.HALF) + QuickInput.ToggleAmountUnit
		assertEquals("3750", assertIs<QuickPending.Amount>(machine.replay(inputs).pending).text)

		val bet = machine.replay(inputs + done(ActionType.BET)).record.streets.flop?.actions?.single()
		assertEquals(3750.0, bet?.amount)
		assertEquals(BetSize.HALF, bet?.potSize)
	}

	@Test
	fun `히어로를 바꿔도 상대 카드는 지정한 포지션에 남는다`() {
		val base = amount("3") + done(ActionType.RAISE) + seat(Position.BTN) + QuickInput.Hero(Position.BTN) + nl
		val utg = listOf(Card(Rank.QUEEN, Suit.CLUBS), Card(Rank.QUEEN, Suit.DIAMONDS))
		val withCards = machine.withCards(
			base,
			QuickInput.FillCards(CardOwner.Opponent(Position.UTG), utg),
		)
		val moved = machine.replay(machine.withHero(withCards, Position.CO))

		assertEquals(utg, moved.opponentHands.single { it.position == Position.UTG }.cards)
		assertTrue(moved.opponentHands.filter { it.position != Position.UTG }.all { it.cards.isEmpty() })
	}

	@Test
	fun `헤즈업은 버튼이 SB로 먼저 액션하고 BB가 폴드하면 버튼이 1BB를 번다`() {
		val headsUp = QuickHandMachine(
			QuickHandConfig(
				tableId = "t1",
				heroSeat = 1,
				seats = listOf(1, 2),
				blinds = Blinds(sb = 500.0, bb = 1000.0),
				stackInBb = 100.0,
			),
		)
		val start = headsUp.replay(emptyList())
		assertEquals(listOf(Position.BTN, Position.BB), start.positions)
		assertEquals(Position.BTN, start.actorPosition)

		val inputs = listOf(seat(Position.BTN), QuickInput.Hero(Position.BTN), act(ActionType.RAISE)) + amount("3") + nl +
			done(ActionType.FOLD)
		val record = assertNotNull(headsUp.build(inputs, heroWins))
		assertEquals(4000.0, record.getPotAtStreet(Street.PREFLOP))
		assertEquals(1000.0, record.result)
	}

	@Test
	fun `프리플랍이 폴드뿐이면 스트릿을 닫을 수 없다`() {
		val snapshot = machine.replay(done(ActionType.FOLD))
		assertFalse(QuickInput.CloseStreet in snapshot.allowed)
		assertEquals(
			QuickPhase.ACTION,
			machine.replay(done(ActionType.FOLD) + QuickInput.CloseStreet).phase,
		)
	}
}

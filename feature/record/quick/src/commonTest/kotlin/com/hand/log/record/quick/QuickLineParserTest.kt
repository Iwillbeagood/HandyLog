package com.hand.log.record.quick

import com.hand.log.domain.model.ActionType
import com.hand.log.domain.model.Blinds
import com.hand.log.domain.model.Card
import com.hand.log.domain.model.Position
import com.hand.log.domain.model.Rank
import com.hand.log.domain.model.Suit
import com.hand.log.domain.model.quick.QuickHandConfig
import com.hand.log.domain.model.quick.QuickHandMachine
import com.hand.log.domain.model.quick.QuickInput
import com.hand.log.domain.model.quick.QuickPhase
import com.hand.log.record.quick.model.HandKey
import com.hand.log.record.quick.model.QuickEditorRenderer
import com.hand.log.record.quick.model.QuickLineParser
import com.hand.log.record.quick.model.draftText
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class QuickLineParserTest {

	private val machine = QuickHandMachine(
		QuickHandConfig(
			tableId = "t1",
			heroSeat = 4,
			seats = (1..6).toList(),
			blinds = Blinds(sb = 500.0, bb = 1000.0),
		),
	)

	private fun commit(inputs: List<QuickInput>, tokens: List<HandKey>): List<QuickInput> =
		inputs + requireNotNull(QuickLineParser.parse(tokens, machine.replay(inputs)))

	@Test
	fun `순서와 상관없이 포지션 카드 액션 금액을 해석한다`() {
		val tokens = listOf(
			HandKey.Num(3),
			HandKey.Face(Rank.ACE),
			HandKey.SuitKey(Suit.SPADES),
			HandKey.Act(ActionType.RAISE),
			HandKey.Pos(Position.CO),
			HandKey.Face(Rank.KING),
		)
		val snapshot = machine.replay(commit(emptyList(), tokens))
		val raise = snapshot.record.streets.preflop.actions.last()

		assertEquals(ActionType.RAISE, raise.type)
		assertEquals(3000.0, raise.amount)
		assertEquals(Position.BTN, snapshot.actorPosition)
		assertEquals(
			listOf(Card(Rank.ACE, Suit.SPADES), Card(Rank.KING)),
			snapshot.opponentHands.first { it.position == Position.CO }.cards,
		)
	}

	@Test
	fun `숫자 뒤에 무늬가 오면 카드 아니면 금액으로 본다`() {
		val tokens = listOf(
			HandKey.Pos(Position.UTG),
			HandKey.Num(9),
			HandKey.SuitKey(Suit.HEARTS),
			HandKey.Act(ActionType.RAISE),
			HandKey.Num(2),
			HandKey.Dot,
			HandKey.Num(5),
		)
		val snapshot = machine.replay(commit(emptyList(), tokens))

		assertEquals(2500.0, snapshot.record.streets.preflop.actions.single().amount)
		assertEquals(
			listOf(Card(Rank.NINE, Suit.HEARTS)),
			snapshot.opponentHands.first {
				it.position == Position.UTG
			}.cards,
		)
	}

	@Test
	fun `액션 없이 금액만 있으면 해석하지 않는다`() {
		assertNull(QuickLineParser.parse(listOf(HandKey.Num(3)), machine.replay(emptyList())))
	}

	@Test
	fun `보드 단계에서 카드 장수를 다 채우면 완료로 본다`() {
		val folds = listOf(HandKey.Num(3), HandKey.Act(ActionType.RAISE))
		var inputs = commit(emptyList(), folds)
		repeat(4) { inputs = commit(inputs, listOf(HandKey.Act(ActionType.FOLD))) }
		inputs = commit(inputs, listOf(HandKey.Act(ActionType.CALL)))
		val board = listOf(
			HandKey.Face(Rank.ACE),
			HandKey.SuitKey(Suit.HEARTS),
			HandKey.Face(Rank.KING),
			HandKey.SuitKey(Suit.HEARTS),
			HandKey.Num(5),
			HandKey.SuitKey(Suit.CLUBS),
		)
		val snapshot = machine.replay(inputs)
		assertEquals(QuickPhase.BOARD, snapshot.phase)
		assertTrue(QuickLineParser.isBoardComplete(board, snapshot))

		val flop = machine.replay(commit(inputs, board))
		assertEquals(QuickPhase.ACTION, flop.phase)
		assertEquals(3, flop.record.streets.flop?.cards?.size)
	}

	@Test
	fun `입력한 키는 누른 순서대로 표시된다`() {
		val tokens = listOf(
			HandKey.Face(Rank.QUEEN),
			HandKey.SuitKey(Suit.CLUBS),
			HandKey.Pos(Position.SB),
			HandKey.Num(1),
			HandKey.Num(2),
			HandKey.Act(ActionType.RAISE),
		)
		assertEquals("Q♣ SB 12 Raise", tokens.draftText())
	}

	@Test
	fun `무늬 없는 랭크 뒤의 무늬는 앞의 카드들에 함께 적용되어 수딧이 된다`() {
		val tokens = listOf(
			HandKey.Pos(Position.UTG),
			HandKey.Face(Rank.ACE),
			HandKey.Face(Rank.KING),
			HandKey.SuitKey(Suit.HEARTS),
			HandKey.Act(ActionType.RAISE),
			HandKey.Num(2),
		)
		val snapshot = machine.replay(commit(emptyList(), tokens))

		assertEquals(
			listOf(Card(Rank.ACE, Suit.HEARTS), Card(Rank.KING, Suit.HEARTS)),
			snapshot.opponentHands.first { it.position == Position.UTG }.cards,
		)
		val line = QuickEditorRenderer.render(snapshot).first { it.text == "UTG" }
		assertEquals(listOf(Card(Rank.ACE, Suit.HEARTS), Card(Rank.KING, Suit.HEARTS)), line.cards)
		assertEquals(" opens to 2BB", line.afterCards)
	}

	@Test
	fun `무늬 없는 랭크 뒤에 무늬를 이어서 두 번 누르면 순서대로 적용되어 오프수딧이 된다`() {
		val tokens = listOf(
			HandKey.Face(Rank.ACE),
			HandKey.Face(Rank.KING),
			HandKey.SuitKey(Suit.SPADES),
			HandKey.SuitKey(Suit.HEARTS),
		)
		assertEquals(
			listOf(Rank.ACE to Suit.SPADES, Rank.KING to Suit.HEARTS),
			QuickLineParser.ParsedLine.of(tokens)?.cards,
		)
	}

	@Test
	fun `두 번째 무늬부터는 일괄 적용된 무늬를 지우고 남은 카드는 무늬 없이 둔다`() {
		val tokens = listOf(
			HandKey.Face(Rank.ACE),
			HandKey.Face(Rank.KING),
			HandKey.Num(6),
			HandKey.SuitKey(Suit.SPADES),
			HandKey.SuitKey(Suit.HEARTS),
		)
		assertEquals(
			listOf(Rank.ACE to Suit.SPADES, Rank.KING to Suit.HEARTS, Rank.SIX to null),
			QuickLineParser.ParsedLine.of(tokens, numbersAreRanks = true)?.cards,
		)
	}

	@Test
	fun `숫자 페어 뒤에 무늬 두 개를 누르면 각 카드에 순서대로 적용된다`() {
		val single = listOf(HandKey.Num(6), HandKey.Num(6), HandKey.SuitKey(Suit.SPADES))
		assertEquals(
			listOf(Rank.SIX to Suit.SPADES, Rank.SIX to null),
			QuickLineParser.ParsedLine.of(single)?.cards,
		)

		val both = single + HandKey.SuitKey(Suit.HEARTS) + HandKey.Act(ActionType.CALL)
		val line = QuickLineParser.ParsedLine.of(both)
		assertEquals(listOf(Rank.SIX to Suit.SPADES, Rank.SIX to Suit.HEARTS), line?.cards)
		assertEquals("", line?.amount)
	}

	@Test
	fun `카드 수보다 많은 무늬는 받지 않는다`() {
		val tokens = listOf(
			HandKey.Face(Rank.ACE),
			HandKey.Face(Rank.KING),
			HandKey.SuitKey(Suit.SPADES),
			HandKey.SuitKey(Suit.HEARTS),
			HandKey.SuitKey(Suit.CLUBS),
		)
		assertEquals(null, QuickLineParser.ParsedLine.of(tokens))
	}

	@Test
	fun `세 자리 이상 숫자 뒤의 무늬는 마지막 숫자에만 적용되고 앞은 금액으로 본다`() {
		val tokens = listOf(HandKey.Num(1), HandKey.Num(2), HandKey.Num(8), HandKey.SuitKey(Suit.SPADES))
		val line = QuickLineParser.ParsedLine.of(tokens)
		assertEquals("12", line?.amount)
		assertEquals(listOf(Rank.EIGHT to Suit.SPADES), line?.cards)
	}

	@Test
	fun `플랍 랭크를 먼저 입력하면 무늬를 카드 수만큼 누를 때까지 완료로 보지 않는다`() {
		var inputs = commit(emptyList(), listOf(HandKey.Num(3), HandKey.Act(ActionType.RAISE)))
		repeat(4) { inputs = commit(inputs, listOf(HandKey.Act(ActionType.FOLD))) }
		inputs = commit(inputs, listOf(HandKey.Act(ActionType.CALL)))
		val snapshot = machine.replay(inputs)
		val ranks = listOf(HandKey.Face(Rank.ACE), HandKey.Face(Rank.KING), HandKey.Face(Rank.QUEEN))

		val monotone = ranks + HandKey.SuitKey(Suit.HEARTS)
		assertFalse(QuickLineParser.isBoardComplete(monotone, snapshot))
		assertFalse(QuickLineParser.isBoardComplete(monotone + HandKey.SuitKey(Suit.SPADES), snapshot))

		val rainbow = monotone + HandKey.SuitKey(Suit.SPADES) + HandKey.SuitKey(Suit.CLUBS)
		assertTrue(QuickLineParser.isBoardComplete(rainbow, snapshot))
		val flop = machine.replay(commit(inputs, rainbow))
		assertEquals(
			listOf(Card(Rank.ACE, Suit.HEARTS), Card(Rank.KING, Suit.SPADES), Card(Rank.QUEEN, Suit.CLUBS)),
			flop.record.streets.flop?.cards,
		)
		val monotoneFlop = machine.replay(commit(inputs, monotone))
		assertEquals(
			listOf(Card(Rank.ACE, Suit.HEARTS), Card(Rank.KING, Suit.HEARTS), Card(Rank.QUEEN, Suit.HEARTS)),
			monotoneFlop.record.streets.flop?.cards,
		)
	}
}

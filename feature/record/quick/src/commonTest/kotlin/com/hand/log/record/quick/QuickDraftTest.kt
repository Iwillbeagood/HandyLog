package com.hand.log.record.quick

import com.hand.log.domain.model.ActionType
import com.hand.log.domain.model.Card
import com.hand.log.domain.model.Position
import com.hand.log.domain.model.Rank
import com.hand.log.domain.model.Suit
import com.hand.log.domain.model.quick.BetSize
import com.hand.log.record.quick.model.DraftContext
import com.hand.log.record.quick.model.HandKey
import com.hand.log.record.quick.model.QuickDraft
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class QuickDraftTest {

	private val context = DraftContext(
		maxCards = 2,
		usedCards = setOf(Card(Rank.ACE, Suit.SPADES)),
		numbersAreRanks = false,
		allInNeedsAmount = false,
	)

	private fun type(vararg keys: HandKey): List<HandKey>? =
		keys.fold<HandKey, List<HandKey>?>(emptyList()) { draft, key ->
			draft?.let {
				QuickDraft.append(it, key, context)
			}
		}

	@Test
	fun `포지션은 하나만 남고 다른 포지션을 누르면 바뀐다`() {
		val draft = requireNotNull(type(HandKey.Pos(Position.UTG), HandKey.Face(Rank.KING)))
		assertNull(QuickDraft.append(draft, HandKey.Pos(Position.UTG), context))
		assertEquals(
			listOf(HandKey.Pos(Position.CO), HandKey.Face(Rank.KING)),
			QuickDraft.append(draft, HandKey.Pos(Position.CO), context),
		)
	}

	@Test
	fun `금액이 없는 액션을 고르면 금액이 지워지고 이후 소수점은 막힌다`() {
		val draft =
			requireNotNull(type(HandKey.Num(3), HandKey.Act(ActionType.RAISE), HandKey.Act(ActionType.CALL)))
		assertEquals(listOf(HandKey.Act(ActionType.CALL)), draft)
		assertNull(QuickDraft.append(draft, HandKey.Sized(BetSize.HALF), context))
	}

	@Test
	fun `카드는 두 장까지만이고 이미 쓴 카드의 무늬는 막힌다`() {
		val draft = requireNotNull(type(HandKey.Face(Rank.ACE)))
		assertNull(QuickDraft.append(draft, HandKey.SuitKey(Suit.SPADES), context))
		val two =
			requireNotNull(
				type(HandKey.Face(Rank.ACE), HandKey.SuitKey(Suit.HEARTS), HandKey.Face(Rank.KING)),
			)
		assertNull(QuickDraft.append(two, HandKey.Face(Rank.QUEEN), context))
	}

	@Test
	fun `수딧 일괄 적용이 쓴 카드와 겹쳐도 무늬를 카드마다 이어서 입력할 수 있다`() {
		val board = context.copy(
			maxCards = 3,
			usedCards = setOf(Card(Rank.SIX, Suit.SPADES)),
			numbersAreRanks = true,
		)
		val ranks = listOf(HandKey.Face(Rank.ACE), HandKey.Face(Rank.KING), HandKey.Num(6))
		val suited = requireNotNull(QuickDraft.append(ranks, HandKey.SuitKey(Suit.SPADES), board))
		assertTrue(QuickDraft.hasCardConflict(suited, board))
		val offsuit = listOf(HandKey.SuitKey(Suit.HEARTS), HandKey.SuitKey(Suit.HEARTS))
			.fold<HandKey, List<HandKey>?>(suited) { draft, key ->
				draft?.let {
					QuickDraft.append(it, key, board)
				}
			}
		assertFalse(QuickDraft.hasCardConflict(requireNotNull(offsuit), board))
		assertNull(
			QuickDraft.append(
				ranks + HandKey.SuitKey(Suit.HEARTS) + HandKey.SuitKey(Suit.HEARTS),
				HandKey.SuitKey(Suit.SPADES),
				board,
			),
		)
	}

	@Test
	fun `소수점은 한 번만 소수 첫째 자리까지만 입력된다`() {
		val draft = requireNotNull(type(HandKey.Num(2), HandKey.Dot, HandKey.Num(5)))
		assertNull(QuickDraft.append(draft, HandKey.Dot, context))
		assertNull(QuickDraft.append(draft, HandKey.Num(1), context))
	}
}

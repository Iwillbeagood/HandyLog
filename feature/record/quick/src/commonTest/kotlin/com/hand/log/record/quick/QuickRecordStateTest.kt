package com.hand.log.record.quick

import com.hand.log.domain.model.ActionType
import com.hand.log.domain.model.Position
import com.hand.log.domain.model.Rank
import com.hand.log.record.quick.component.QuickRecordPreviewData
import com.hand.log.record.quick.model.HandKey
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class QuickRecordStateTest {

	private val preflop = QuickRecordPreviewData.state(emptyList()).copy(
		selectablePositions = setOf(Position.UTG),
		legalActions = setOf(ActionType.RAISE),
	)

	@Test
	fun `프리플랍에서 포지션을 고르기 전에는 포지션 키만 입력할 수 있다`() {
		val state = preflop
		assertTrue(HandKey.Pos(Position.UTG) in state.enabledKeys)
		assertFalse(HandKey.Act(ActionType.RAISE) in state.enabledKeys)
		assertFalse(HandKey.Face(Rank.ACE) in state.enabledKeys)
	}

	@Test
	fun `프리플랍에서 포지션을 고르면 액션과 카드를 입력할 수 있다`() {
		val state = preflop.copy(draft = listOf(HandKey.Pos(Position.UTG)))
		assertTrue(HandKey.Act(ActionType.RAISE) in state.enabledKeys)
		assertTrue(HandKey.Face(Rank.ACE) in state.enabledKeys)
	}
}

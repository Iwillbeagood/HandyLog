package com.hand.log.record.quick

import com.hand.log.domain.model.ActionType
import com.hand.log.domain.model.Card
import com.hand.log.domain.model.Rank
import com.hand.log.domain.model.Position
import com.hand.log.domain.model.quick.QuickHandConfig
import com.hand.log.domain.model.quick.QuickHandMachine
import com.hand.log.domain.model.quick.QuickInput
import com.hand.log.record.quick.component.QuickRecordPreviewData
import com.hand.log.record.quick.model.EditorLineKind
import com.hand.log.record.quick.model.HandKey
import com.hand.log.record.quick.model.QuickEditorRenderer
import com.hand.log.record.quick.model.toInput
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class QuickEditorRendererTest {

	private fun texts(inputs: List<QuickInput>) = QuickRecordPreviewData.state(inputs).lines.map {
		it.text
	}

	@Test
	fun `히어로 지정 전에는 첫 액터 차례가 제시되고 Hero 줄이 없다`() {
		val state = QuickRecordPreviewData.state(emptyList())
		assertTrue(state.canSelectHero)
		assertEquals(null, state.heroLabel)
		assertEquals(null, state.actorKey)
		assertFalse(state.lines.any { it.text.contains("(Hero)") })
	}

	@Test
	fun `히어로를 지정하면 해당 포지션 키와 상단 버튼에 히어로가 표시된다`() {
		val state = QuickRecordPreviewData.state(QuickRecordPreviewData.heroSetup)
		assertEquals(HandKey.Pos(Position.BTN), state.heroKey)
		assertEquals("BTN", state.heroLabel)
		assertTrue(HandKey.NextLine in state.enabledKeys)
	}

	@Test
	fun `액션 중 히어로를 지정하면 맨 위 히어로 줄에 스택 없이 랭크만 입력한 카드가 표시된다`() {
		val lines = QuickRecordPreviewData.state(QuickRecordPreviewData.heroSetup).lines
		val heroLine = lines.single { it.text.startsWith("BTN (Hero)") }
		assertEquals("BTN (Hero)", heroLine.text)
		assertEquals(listOf(Card(Rank.FIVE), Card(Rank.FIVE)), heroLine.cards)
		assertTrue(heroLine.hasCaret)
		assertTrue(lines.any { it.text.startsWith("[Preflop]") })
		assertFalse(lines.any { it.text.startsWith("[Stacks]") || it.text.contains("100BB") })
	}

	@Test
	fun `줄넘김 전 액션은 커서가 있는 줄로 표시된다`() {
		val last = QuickRecordPreviewData.state(QuickRecordPreviewData.stagedInput).lines.last()
		assertEquals("BTN (Hero) calls 3BB", last.text)
		assertTrue(last.hasCaret)
	}

	@Test
	fun `금액 입력 중인 레이즈는 베팅 단계 라벨과 함께 표시된다`() {
		val lines = QuickRecordPreviewData.state(QuickRecordPreviewData.amountInput).lines
		val last = lines.last()
		assertEquals("SB 3-bets to 12", last.text)
		assertEquals("BB", last.caretTrailing)
		val heroLine = lines.single { it.text == "BTN (Hero)" }
		assertEquals(" calls 3BB", heroLine.afterCards)
		assertEquals(listOf(Card(Rank.FIVE), Card(Rank.FIVE)), heroLine.cards)
		assertEquals(EditorLineKind.HERO, heroLine.kind)
		assertEquals("[Preflop]", lines.first().text)
	}

	@Test
	fun `플랍 카드 입력 중에는 Flop 줄에 이어서 표시되고 팟 진행 줄은 보이지 않는다`() {
		val lines = QuickRecordPreviewData.state(QuickRecordPreviewData.boardInput).lines
		assertEquals("Flop", lines.last().text)
		assertEquals(EditorLineKind.BOARD, lines.last().kind)
		assertEquals(listOf(Card(Rank.ACE), Card(Rank.KING)), lines.last().cards)
		assertFalse(lines.any { it.text.startsWith("→") })
	}

	@Test
	fun `핸드가 끝나면 마무리 뷰로 전환되고 상대 핸드 슬롯을 제공한다`() {
		val state = QuickRecordPreviewData.state(QuickRecordPreviewData.reviewInput)
		assertTrue(state.isFinishing)
		assertTrue(state.canSave)
		assertTrue(state.showFinishCards)
		assertEquals(listOf(Position.UTG), state.finish?.showdown?.map { it.position })
		assertTrue(state.heroPositionOptions.isEmpty())
	}

	@Test
	fun `히어로 없이 끝나면 마무리 뷰에서 포지션부터 고른다`() {
		val state = QuickRecordPreviewData.state(QuickRecordPreviewData.heroMissingInput)
		assertTrue(state.isFinishing)
		assertFalse(state.canSave)
		assertFalse(state.showFinishCards)
		assertTrue("BB" in state.heroPositionOptions)
	}

	@Test
	fun `입력 완료 시 Result 줄이 생성된다`() {
		val lines = texts(QuickRecordPreviewData.doneInput)
		assertTrue(lines.any { it.startsWith("[Result]") })
	}

	@Test
	fun `숫자 키는 금액 입력 중이면 숫자이고 카드 입력 중이면 랭크로 해석된다`() {
		val amount = QuickRecordPreviewData.state(QuickRecordPreviewData.amountInput)
		assertEquals(QuickInput.Digit(9), HandKey.Num(9).toInput(amount.snapshot.allowed))

		val board = QuickRecordPreviewData.state(QuickRecordPreviewData.boardInput.dropLast(1))
		assertTrue(HandKey.Num(9).toInput(board.snapshot.allowed) is QuickInput.CardRank)
	}

	@Test
	fun `블라인드 없이 기록하면 금액 뒤에 BB를 붙이지 않는다`() {
		val machine = QuickHandMachine(
			QuickHandConfig(
				tableId = null,
				heroSeat = QuickHandConfig.HERO_SEAT_WITHOUT_TABLE,
				seats = (1..QuickHandConfig.DEFAULT_PLAYER_COUNT).toList(),
				blinds = QuickHandConfig.BB_UNIT_BLINDS,
			),
		)
		val inputs = listOf(QuickInput.Digit(3), QuickInput.Act(ActionType.RAISE), QuickInput.NextLine, QuickInput.Act(ActionType.RAISE)) +
			listOf(QuickInput.Digit(9))
		val lines = QuickEditorRenderer.render(machine.replay(inputs), showBbUnit = false)

		assertTrue(lines.any { it.text.endsWith("opens to 3") })
		assertEquals("", lines.last().caretTrailing)
		assertFalse(lines.any { "BB" in it.text.replace(" BB", "") })
	}

	@Test
	fun `차례 포지션은 자동으로 입력되지 않고 직접 고른 포지션만 표시된다`() {
		val initial = QuickRecordPreviewData.state(emptyList()).lines.last()
		assertEquals("", initial.text)
		assertTrue(initial.hasCaret)

		val chosen = QuickRecordPreviewData.state(listOf(QuickInput.Seat(Position.CO))).lines.last()
		assertEquals("CO", chosen.text)
	}

	@Test
	fun `입력 중인 줄은 키보드로 누른 그대로 표시된다`() {
		val last = QuickRecordPreviewData.draftState.lines.last()
		assertEquals("SB Q♣ 9 Raise", last.draft)
		assertTrue(last.hasCaret)
	}
}

package com.hand.log.preflop.quiz.common

import com.hand.log.domain.model.Position
import com.hand.log.domain.model.preflop.PreflopAction
import com.hand.log.domain.model.preflop.PreflopChart
import com.hand.log.domain.model.preflop.PreflopChartQuery
import com.hand.log.domain.model.preflop.PreflopGrid
import com.hand.log.domain.model.preflop.PreflopScenario
import com.hand.log.domain.model.preflop.PreflopStack
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class QuizQuestionGeneratorTest {

	private val generator = QuizQuestionGenerator()
	private val allHands = PreflopGrid.rows.flatten()

	private val vs3betChart = PreflopChart(
		mapOf(
			"AA" to PreflopAction.FOUR_BET,
			"KK" to PreflopAction.FOUR_BET,
			"QQ" to PreflopAction.CALL,
			"AKs" to PreflopAction.CALL,
			"KQs" to PreflopAction.FOLD,
		),
	)
	private val vs3betQuery =
		PreflopChartQuery(PreflopStack.BB100, PreflopScenario.VS_3BET, Position.CO, Position.BTN)

	@Test
	fun `vs 3벳 문제는 히어로가 오픈한 핸드에서만 출제된다`() {
		val questions = generator.generate(
			mapOf(vs3betQuery to vs3betChart),
			PreflopQuizType.VS_3BET,
			200,
		)

		assertEquals(200, questions.size)
		assertTrue(questions.all { it.hand.notation in vs3betChart.actions.keys })
	}

	@Test
	fun `vs 3벳 경계 핸드는 Not in Range 핸드를 포함하지 않는다`() {
		val candidates = generator.candidateHands(vs3betChart, PreflopScenario.VS_3BET)
		val borderline = generator.borderlineHands(vs3betChart, candidates)

		assertEquals(vs3betChart.actions.keys, candidates.map { it.notation }.toSet())
		assertTrue(borderline.all { it.notation in vs3betChart.actions.keys })
	}

	@Test
	fun `RFI 문제는 전체 169 핸드를 후보로 쓴다`() {
		assertEquals(allHands, generator.candidateHands(vs3betChart, PreflopScenario.RFI))
	}

	@Test
	fun `풀이 충분하면 한 세션에 같은 스팟과 핸드가 중복 출제되지 않는다`() {
		val query = PreflopChartQuery(PreflopStack.BB100, PreflopScenario.RFI, Position.BTN)
		val chart = PreflopChart(allHands.associate { it.notation to PreflopAction.RAISE })

		repeat(20) {
			val questions = generator.generate(mapOf(query to chart), PreflopQuizType.RFI, 10)
			assertEquals(questions.size, questions.map { it.hand }.toSet().size)
		}
	}

	@Test
	fun `온라인 스택 문제는 6맥스 좌석 순서를 쓴다`() {
		val query = PreflopChartQuery(PreflopStack.ONLINE, PreflopScenario.RFI, Position.CO)
		val chart = PreflopChart(mapOf("AA" to PreflopAction.RAISE))

		val question = generator.generate(mapOf(query to chart), PreflopQuizType.RFI, 1).single()

		assertEquals(6, question.seatOrder.size)
	}
}

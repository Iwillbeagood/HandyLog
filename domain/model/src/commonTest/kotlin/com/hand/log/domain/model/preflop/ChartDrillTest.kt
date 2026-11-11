package com.hand.log.domain.model.preflop

import kotlin.test.Test
import kotlin.test.assertEquals

class ChartDrillTest {

	private val answer = mapOf(
		"AA" to PreflopAction.RAISE,
		"AKs" to PreflopAction.RAISE,
		"A5s" to PreflopAction.RAISE_BLUFF,
		"KQo" to PreflopAction.RAISE,
	)

	@Test
	fun `같은 액션으로 칠한 칸은 정답이다`() {
		val result = gradeChartDrill(answer, painted = answer)

		assertEquals(4, result.correct)
		assertEquals(4, result.total)
		assertEquals(100, result.accuracyPct)
	}

	@Test
	fun `다른 액션으로 칠한 칸은 액션 오답이다`() {
		val painted = answer + ("A5s" to PreflopAction.RAISE)

		val result = gradeChartDrill(answer, painted)

		assertEquals(DrillCellResult.WRONG_ACTION, result.cells["A5s"])
		assertEquals(1, result.wrongAction)
		assertEquals(75, result.accuracyPct)
	}

	@Test
	fun `범위에 있는데 칠하지 않은 칸은 누락이다`() {
		val painted = answer - "KQo"

		val result = gradeChartDrill(answer, painted)

		assertEquals(DrillCellResult.MISSED, result.cells["KQo"])
		assertEquals(1, result.missed)
		assertEquals(3, result.correct)
	}

	@Test
	fun `범위 밖인데 칠한 칸은 초과다`() {
		val painted = answer + ("72o" to PreflopAction.RAISE)

		val result = gradeChartDrill(answer, painted)

		assertEquals(DrillCellResult.EXTRA, result.cells["72o"])
		assertEquals(1, result.extra)
		assertEquals(5, result.total)
		assertEquals(80, result.accuracyPct)
	}

	@Test
	fun `양쪽 모두에 없는 핸드는 채점하지 않는다`() {
		val result = gradeChartDrill(answer, painted = emptyMap())

		assertEquals(answer.keys, result.cells.keys)
		assertEquals(0, result.accuracyPct)
	}

	@Test
	fun `정답과 칠한 칸이 모두 비면 100퍼센트다`() {
		val result = gradeChartDrill(emptyMap(), emptyMap())

		assertEquals(0, result.total)
		assertEquals(100, result.accuracyPct)
	}
}

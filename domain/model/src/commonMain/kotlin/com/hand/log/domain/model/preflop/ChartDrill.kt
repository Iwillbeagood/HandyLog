package com.hand.log.domain.model.preflop

enum class DrillCellResult { CORRECT, WRONG_ACTION, MISSED, EXTRA }

data class ChartDrillResult(
	val cells: Map<String, DrillCellResult>,
) {
	val correct: Int get() = count(DrillCellResult.CORRECT)
	val wrongAction: Int get() = count(DrillCellResult.WRONG_ACTION)
	val missed: Int get() = count(DrillCellResult.MISSED)
	val extra: Int get() = count(DrillCellResult.EXTRA)
	val total: Int get() = cells.size
	val accuracyPct: Int get() = if (total == 0) 100 else correct * 100 / total

	private fun count(result: DrillCellResult): Int = cells.values.count { it == result }
}

fun gradeChartDrill(
	answer: Map<String, PreflopAction>,
	painted: Map<String, PreflopAction>,
): ChartDrillResult = ChartDrillResult(
	(answer.keys + painted.keys).associateWith { hand ->
		val expected = answer[hand]
		val actual = painted[hand]
		when {
			expected == null -> DrillCellResult.EXTRA
			actual == null -> DrillCellResult.MISSED
			actual == expected -> DrillCellResult.CORRECT
			else -> DrillCellResult.WRONG_ACTION
		}
	},
)

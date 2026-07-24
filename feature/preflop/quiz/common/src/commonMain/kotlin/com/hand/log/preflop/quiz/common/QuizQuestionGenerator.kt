package com.hand.log.preflop.quiz.common

import com.hand.log.domain.model.preflop.PreflopAction
import com.hand.log.domain.model.preflop.PreflopChart
import com.hand.log.domain.model.preflop.PreflopChartQuery
import com.hand.log.domain.model.preflop.PreflopGrid
import com.hand.log.domain.model.preflop.PreflopHand
import com.hand.log.domain.model.preflop.PreflopScenario
import kotlin.random.Random

/** 로드된 차트 카탈로그에서 랜덤 퀴즈 문제를 생성한다. */
class QuizQuestionGenerator {

	private val allHands: List<PreflopHand> = PreflopGrid.rows.flatten()

	fun generate(
		charts: Map<PreflopChartQuery, PreflopChart>,
		type: PreflopQuizType,
		count: Int,
	): List<PreflopQuizQuestion> {
		val scenario = when (type) {
			PreflopQuizType.RFI -> PreflopScenario.RFI
			PreflopQuizType.VS_OPEN -> PreflopScenario.FACING_RFI
			PreflopQuizType.MIXED -> null
		}
		val pool = charts.entries.filter { (query, chart) ->
			(scenario == null || query.scenario == scenario) && !chart.isEmpty
		}
		if (pool.isEmpty()) return emptyList()

		return (0 until count).map {
			val entry = pool.random()
			val query = entry.key
			val chart = entry.value
			val hand = pickHand(chart)
			PreflopQuizQuestion(
				stack = query.stack,
				scenario = query.scenario,
				hero = query.hero,
				villain = query.villain,
				hand = hand,
				correct = chart.actionFor(hand).toQuizAnswer(),
			)
		}
	}

	/** 대부분 폴드인 그리드 특성상, 65% 확률로 논-폴드 핸드를 뽑아 문제 다양성을 확보. */
	private fun pickHand(chart: PreflopChart): PreflopHand {
		val nonFold = allHands.filter { chart.actionFor(it) != PreflopAction.FOLD }
		return if (nonFold.isNotEmpty() && Random.nextInt(100) < 65) nonFold.random() else allHands.random()
	}
}

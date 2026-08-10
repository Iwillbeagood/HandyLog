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

	/** 각 핸드의 그리드 좌표(행, 열) — 이웃 칸과 액션을 비교해 경계 핸드를 찾는 데 쓴다. */
	private val gridPos: Map<PreflopHand, Pair<Int, Int>> = buildMap {
		PreflopGrid.rows.forEachIndexed { r, row ->
			row.forEachIndexed { c, hand -> put(hand, r to c) }
		}
	}

	fun generate(
		charts: Map<PreflopChartQuery, PreflopChart>,
		type: PreflopQuizType,
		count: Int,
	): List<PreflopQuizQuestion> {
		val scenario = when (type) {
			PreflopQuizType.RFI -> PreflopScenario.RFI
			PreflopQuizType.VS_OPEN -> PreflopScenario.FACING_RFI
			PreflopQuizType.VS_3BET -> PreflopScenario.VS_3BET
			PreflopQuizType.MIXED -> null
		}
		// VS_LIMP(SB 림프 → BB)는 대응하는 퀴즈 유형이 없고 선택지 체계도 달라 출제 대상에서 제외한다.
		// MIXED(scenario == null)도 3개 시나리오만 섞어야 하므로 함께 걸러진다.
		val pool = charts.entries.filter { (query, chart) ->
			query.scenario != PreflopScenario.VS_LIMP &&
				(scenario == null || query.scenario == scenario) &&
				!chart.isEmpty
		}
		if (pool.isEmpty()) return emptyList()

		return (0 until count).map {
			val entry = pool.random()
			val query = entry.key
			val chart = entry.value
			val hand = pickHand(chart)
			val correct = (chart.actionFor(hand) ?: PreflopAction.FOLD).withoutBluff()
			PreflopQuizQuestion(
				stack = query.stack,
				scenario = query.scenario,
				hero = query.hero,
				villain = query.villain,
				hand = hand,
				correct = correct,
				options = primaryOptionsOf(chart.actions.values, correct),
				planOptions = planOptionsOf(chart.actions.values, correct),
			)
		}
	}

	/**
	 * 난이도를 위해 [BORDERLINE_BIAS]% 확률로 폴드↔논폴드 경계의 마진 핸드를 우선 출제한다.
	 * 나머지는 명확한 논폴드(65%)/전체를 섞어 변별력을 확보.
	 */
	private fun pickHand(chart: PreflopChart): PreflopHand {
		val borderline = borderlineHands(chart)
		if (borderline.isNotEmpty() && Random.nextInt(100) < BORDERLINE_BIAS) {
			return borderline.random()
		}
		val nonFold = allHands.filter {
			chart.actionFor(it) != null && chart.actionFor(it) != PreflopAction.FOLD
		}
		return if (nonFold.isNotEmpty() && Random.nextInt(100) < 65) nonFold.random() else allHands.random()
	}

	/**
	 * 정답이 갈리는 경계에 있는 핸드들 = 판단이 어려운 마진 핸드.
	 * 그리드 상하좌우(랭크 한 칸 차이의 유사 강도) 이웃 중 액션 대분류(공격/콜/림프/폴드)가 다른 칸이
	 * 하나라도 있으면 경계로 본다 — 세부 라인(레이즈/폴드 vs 레이즈/콜 등)까지 따지면 대부분이 경계가 되므로
	 * 난이도 판단은 대분류 기준으로 한다. Not in Range(null)·Fold 는 모두 폴드로 취급한다.
	 */
	private fun borderlineHands(chart: PreflopChart): List<PreflopHand> =
		allHands.filter { hand ->
			val bucket = bucketOf(chart, hand)
			val (r, c) = gridPos.getValue(hand)
			neighborsOf(r, c).any { bucketOf(chart, it) != bucket }
		}

	private fun bucketOf(chart: PreflopChart, hand: PreflopHand): ActionBucket =
		when (chart.actionFor(hand)) {
			null, PreflopAction.FOLD -> ActionBucket.FOLD
			PreflopAction.LIMP -> ActionBucket.LIMP
			PreflopAction.CALL, PreflopAction.CHECK -> ActionBucket.CALL
			else -> ActionBucket.AGGRO
		}

	private fun neighborsOf(row: Int, col: Int): List<PreflopHand> =
		listOf(row - 1 to col, row + 1 to col, row to col - 1, row to col + 1)
			.mapNotNull { (r, c) -> PreflopGrid.rows.getOrNull(r)?.getOrNull(c) }

	/** 마진 핸드 판단용 액션 대분류. */
	private enum class ActionBucket { AGGRO, CALL, LIMP, FOLD }

	companion object {
		/** 경계(마진) 핸드를 우선 출제할 확률(%). 높일수록 어려워진다. */
		const val BORDERLINE_BIAS = 80
	}
}

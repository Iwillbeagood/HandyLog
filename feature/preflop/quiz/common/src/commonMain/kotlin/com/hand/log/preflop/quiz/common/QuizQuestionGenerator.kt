package com.hand.log.preflop.quiz.common

import com.hand.log.domain.model.preflop.PreflopAction
import com.hand.log.domain.model.preflop.PreflopChart
import com.hand.log.domain.model.preflop.PreflopChartQuery
import com.hand.log.domain.model.preflop.PreflopGrid
import com.hand.log.domain.model.preflop.PreflopHand
import com.hand.log.domain.model.preflop.PreflopScenario
import com.hand.log.domain.model.preflop.PreflopStack
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
		stack: PreflopStack? = null,
	): List<PreflopQuizQuestion> {
		val pool = poolOf(charts, type, stack)
		if (pool.isEmpty()) return emptyList()

		val asked = mutableSetOf<Pair<PreflopChartQuery, PreflopHand>>()
		return List(count) {
			val (entry, hand) = generateSequence { pick(pool) }
				.take(MAX_PICK_ATTEMPTS)
				.firstOrNull { asked.add(it.first.key to it.second) }
				?: pick(pool)
			val query = entry.key
			val chart = entry.value
			val correct = (chart.actionFor(hand) ?: PreflopAction.FOLD).withoutBluff()
			PreflopQuizQuestion(
				stack = query.stack,
				scenario = query.scenario,
				hero = query.hero,
				villain = query.villain,
				hand = hand,
				correct = correct,
				options = answerOptionsOf(chart.actions.values, correct),
			)
		}
	}

	private fun pick(
		pool: List<Map.Entry<PreflopChartQuery, PreflopChart>>,
	): Pair<Map.Entry<PreflopChartQuery, PreflopChart>, PreflopHand> {
		val entry = pool.random()
		return entry to pickHand(entry.value, entry.key.scenario)
	}

	fun availableTypes(
		charts: Map<PreflopChartQuery, PreflopChart>,
		stack: PreflopStack?,
	): Set<PreflopQuizType> =
		PreflopQuizType.entries.filter { poolOf(charts, it, stack).isNotEmpty() }.toSet()

	private fun poolOf(
		charts: Map<PreflopChartQuery, PreflopChart>,
		type: PreflopQuizType,
		stack: PreflopStack?,
	): List<Map.Entry<PreflopChartQuery, PreflopChart>> {
		val scenario = when (type) {
			PreflopQuizType.RFI -> PreflopScenario.RFI
			PreflopQuizType.VS_OPEN -> PreflopScenario.FACING_RFI
			PreflopQuizType.VS_3BET -> PreflopScenario.VS_3BET
			PreflopQuizType.MIXED -> null
		}
		return charts.entries.filter { (query, chart) ->
			query.scenario != PreflopScenario.VS_LIMP &&
				(scenario == null || query.scenario == scenario) &&
				(stack == null || query.stack == stack) &&
				!chart.isEmpty
		}
	}

	/**
	 * 난이도를 위해 [BORDERLINE_BIAS]% 확률로 폴드↔논폴드 경계의 마진 핸드를 우선 출제한다.
	 * 나머지는 명확한 논폴드(65%)/전체를 섞어 변별력을 확보.
	 */
	private fun pickHand(chart: PreflopChart, scenario: PreflopScenario): PreflopHand {
		val candidates = candidateHands(chart, scenario)
		val borderline = borderlineHands(chart, candidates)
		if (borderline.isNotEmpty() && Random.nextInt(100) < BORDERLINE_BIAS) {
			return borderline.random()
		}
		val nonFold = candidates.filter {
			chart.actionFor(it) != null && chart.actionFor(it) != PreflopAction.FOLD
		}
		return if (nonFold.isNotEmpty() && Random.nextInt(100) < 65) nonFold.random() else candidates.random()
	}

	/** vs 3벳은 히어로가 오픈한 핸드만 출제한다 — Not in Range(null)는 애초에 오픈하지 않은 핸드. */
	internal fun candidateHands(chart: PreflopChart, scenario: PreflopScenario): List<PreflopHand> =
		if (scenario == PreflopScenario.VS_3BET) {
			allHands.filter { chart.actionFor(it) != null }
		} else {
			allHands
		}

	/**
	 * 정답이 갈리는 경계에 있는 핸드들 = 판단이 어려운 마진 핸드.
	 * 그리드 상하좌우(랭크 한 칸 차이의 유사 강도) 이웃 중 액션 대분류(공격/콜/림프/폴드)가 다른 칸이
	 * 하나라도 있으면 경계로 본다 — 세부 라인(레이즈/폴드 vs 레이즈/콜 등)까지 따지면 대부분이 경계가 되므로
	 * 난이도 판단은 대분류 기준으로 한다. Not in Range(null)·Fold 는 모두 폴드로 취급하고,
	 * [candidates] 밖의 이웃은 비교하지 않는다.
	 */
	internal fun borderlineHands(chart: PreflopChart, candidates: List<PreflopHand>): List<PreflopHand> {
		val candidateSet = candidates.toSet()
		return candidates.filter { hand ->
			val bucket = bucketOf(chart, hand)
			val (r, c) = gridPos.getValue(hand)
			neighborsOf(r, c).any { it in candidateSet && bucketOf(chart, it) != bucket }
		}
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

		private const val MAX_PICK_ATTEMPTS = 20
	}
}

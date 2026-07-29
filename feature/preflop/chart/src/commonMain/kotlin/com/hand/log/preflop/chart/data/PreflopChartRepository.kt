package com.hand.log.preflop.chart.data

import com.hand.log.domain.model.Position
import com.hand.log.domain.model.preflop.PreflopAction
import com.hand.log.domain.model.preflop.PreflopChart
import com.hand.log.domain.model.preflop.PreflopChartQuery
import com.hand.log.domain.model.preflop.PreflopPositions
import com.hand.log.domain.model.preflop.PreflopScenario
import com.hand.log.domain.model.preflop.PreflopStack
import handylog.core.res.generated.resources.Res
import kotlinx.serialization.json.Json

/**
 * 로드된 프리플랍 차트 카탈로그.
 * [groupKeys] 는 각 쿼리가 유래한 원본 DTO 그룹 식별자로, 같은 그룹의 상대 포지션은 하나의 칩으로 병합할 때 쓴다.
 */
data class PreflopCatalog(
	val charts: Map<PreflopChartQuery, PreflopChart>,
	val groupKeys: Map<PreflopChartQuery, String>,
)

/**
 * 스택별 preflop_charts.json 5종을 1회 로드·파싱하여 (스택·상황 → 차트) 카탈로그로 캐시한다.
 *
 * 한 차트가 히어로/상대 그룹("UTG+1/+2", "LJ/HJ")이나 범위("UTG+1 - CO"), 미지정("vs All-In")을
 * 커버하면 해당하는 모든 조합으로 전개해 등록한다. 3-시나리오에 맞지 않는 Blind-vs-Blind/림프/잼 라인은 제외한다.
 */
class PreflopChartRepository {

	private val files = listOf(
		"files/15bb_preflop_charts.json",
		"files/25bb_preflop_charts.json",
		"files/40bb_preflop_charts.json",
		"files/75bb_preflop_charts.json",
		"files/100bb_preflop_charts.json",
	)

	private val json = Json { ignoreUnknownKeys = true }
	private var cache: PreflopCatalog? = null

	suspend fun load(): PreflopCatalog {
		cache?.let { return it }

		val charts = mutableMapOf<PreflopChartQuery, PreflopChart>()
		val groupKeys = mutableMapOf<PreflopChartQuery, String>()

		files.forEach { path ->
			val raw = Res.readBytes(path).decodeToString()
			json.decodeFromString<List<PreflopChartDto>>(raw).forEach { dto ->
				val stack = PreflopStack.fromLabel(dto.stackSize) ?: return@forEach
				val scenario = scenarioOf(dto.category) ?: return@forEach
				val chart = PreflopChart(dto.actions.mapValues { actionOf(it.value) })
				// 한 DTO(chart_name)에서 전개된 (hero, villain) 들은 같은 원본 그룹으로 간주한다.
				val groupKey = "${dto.stackSize}|${dto.category}|${dto.chartName}"
				parseTargets(scenario, dto.chartName).forEach { (hero, villain) ->
					val query = PreflopChartQuery(stack, scenario, hero, villain)
					charts[query] = chart
					groupKeys[query] = groupKey
				}
			}
		}
		return PreflopCatalog(charts, groupKeys).also { cache = it }
	}

	private fun scenarioOf(category: String): PreflopScenario? = when {
		category.startsWith("Raise First") -> PreflopScenario.RFI
		category.startsWith("Facing RFI") -> PreflopScenario.FACING_RFI
		category.contains("vs 3bet") || category.startsWith("Facing 3-bet") -> PreflopScenario.VS_3BET
		else -> null // Blind vs Blind 등은 제외
	}

	private fun actionOf(value: String): PreflopAction {
		val bluff = value.contains("Bluff")
		return when {
			value == "Fold" -> PreflopAction.FOLD
			value == "Call" -> PreflopAction.CALL
			value == "Limp" -> PreflopAction.LIMP
			value == "All-in" -> PreflopAction.ALL_IN
			value.startsWith("4-bet") -> if (bluff) PreflopAction.FOUR_BET_BLUFF else PreflopAction.FOUR_BET
			value.startsWith(
				"3-bet",
			) -> if (bluff) PreflopAction.THREE_BET_BLUFF else PreflopAction.THREE_BET
			bluff -> PreflopAction.RAISE_BLUFF
			else -> PreflopAction.RAISE
		}
	}

	/** chart_name → (히어로, 상대) 조합 목록. RFI 는 상대 null. 파싱 불가/림프 라인은 빈 목록. */
	private fun parseTargets(
		scenario: PreflopScenario,
		chartName: String,
	): List<Pair<Position, Position?>> {
		if (scenario == PreflopScenario.RFI) {
			val hero = positionOf(chartName) ?: return emptyList()
			return listOf(hero to null)
		}
		if (chartName.contains("Limp") || chartName.contains("Jam")) return emptyList()

		val cleaned = chartName
			.replace("RFI", "")
			.replace("3bet", "")
			.replace("All-In", "")
			.replace("All-in", "")
			.replace("Raise", "")
		val parts = cleaned.split(" vs ")
		if (parts.size != 2) return emptyList()

		val heroes = expandPositions(parts[0])
		if (heroes.isEmpty()) return emptyList()
		val villainsRaw = expandPositions(parts[1])

		return heroes.flatMap { hero ->
			val villains = villainsRaw.ifEmpty {
				when (scenario) {
					PreflopScenario.FACING_RFI -> PreflopPositions.raisersBefore(hero)
					PreflopScenario.VS_3BET -> PreflopPositions.threeBettorsAfter(hero)
					else -> emptyList()
				}
			}
			villains.map { hero to it }
		}
	}

	/** "LJ/HJ", "UTG+1/+2", "UTG+1 - CO" 등을 포지션 목록으로 전개. 미지정("All-In")은 빈 목록. */
	private fun expandPositions(raw: String): List<Position> {
		val s = raw.trim()
		if (s.isEmpty()) return emptyList()

		if (" - " in s) {
			val ends = s.split(" - ")
			val start = positionOf(ends.getOrElse(0) { "" })
			val end = positionOf(ends.getOrElse(1) { "" })
			if (start != null && end != null) {
				val order = PreflopPositions.order
				val i = order.indexOf(start)
				val j = order.indexOf(end)
				if (i in 0..j) return order.subList(i, j + 1)
			}
			return emptyList()
		}

		return s.split("/").mapNotNull { positionOf(it.trim()) }
	}

	private fun positionOf(token: String): Position? = when (token.trim()) {
		"UTG" -> Position.UTG
		"UTG+1", "+1" -> Position.UTG1
		"UTG+2", "+2" -> Position.UTG2
		"LJ", "Lojack" -> Position.LJ
		"HJ", "Hijack" -> Position.HJ
		"CO", "Cutoff" -> Position.CO
		"BTN", "Button" -> Position.BTN
		"SB", "Small Blind" -> Position.SB
		"BB", "Big Blind" -> Position.BB
		else -> null
	}
}

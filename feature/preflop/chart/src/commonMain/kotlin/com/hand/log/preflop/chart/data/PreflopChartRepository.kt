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
		"files/online_preflop_charts.json",
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
				// 파일마다 "Big Blind"/"BB", 대소문자가 뒤섞여 있어 포지션 표기를 먼저 통일한다.
				val chartName = normalizeChartName(dto.chartName)
				val scenario = scenarioOf(dto.category, chartName) ?: return@forEach
				// "Not in Range" 핸드는 map 에서 제외 → 그리드에서 빈칸으로 표시된다.
				// VS_LIMP 는 BB 가 무료로 들어가 있어 폴드가 없으므로 원본 Fold 를 체크로 해석한다.
				val chart = PreflopChart(
					dto.actions.mapNotNull { (hand, value) ->
						actionOf(
							value,
							scenario,
						)?.let { hand to it }
					}.toMap(),
				)
				// 한 DTO(chart_name)에서 전개된 (hero, villain) 들은 같은 원본 그룹으로 간주한다.
				val groupKey = "${dto.stackSize}|${dto.category}|$chartName"
				parseTargets(scenario, chartName).forEach { (hero, villain) ->
					val query = PreflopChartQuery(stack, scenario, hero, villain)
					charts[query] = chart
					groupKeys[query] = groupKey
				}
			}
		}
		return PreflopCatalog(charts, groupKeys).also { cache = it }
	}

	private fun scenarioOf(category: String, chartName: String): PreflopScenario? = when {
		category.startsWith("Raise First") -> PreflopScenario.RFI
		category.startsWith("Facing RFI") -> PreflopScenario.FACING_RFI
		category.contains("vs 3bet") || category.startsWith("Facing 3-bet") -> PreflopScenario.VS_3BET
		// SB 림프에 대응하는 BB. "…Limp/Jam", "…Limp/All-In" 복합 라인은 제외한다.
		category == "Blind vs Blind" && chartName.endsWith("vs SB Limp", ignoreCase = true) -> PreflopScenario.VS_LIMP
		// SB 오픈(레이즈)에 대응하는 BB = 사실상 FACING_RFI(히어로 BB, 상대 SB).
		category == "Blind vs Blind" && chartName.contains("vs SB Raise", ignoreCase = true) -> PreflopScenario.FACING_RFI
		else -> null // 그 외 Blind vs Blind(SB 시점·올인·잼 라인 등)는 제외
	}

	/**
	 * 원본 액션 문자열을 [PreflopAction] 으로 매핑한다. "3-bet/Fold to 4bet", "Raise/Call 3bet" 처럼
	 * "베이스 액션 + 대응 계획" 형태의 복합 표기는 계획 키워드(Jam/Fold/Call/4bet/Stackoff/Bluff)로 세분한다.
	 */
	private fun actionOf(value: String, scenario: PreflopScenario): PreflopAction? = when {
		value == "Not in Range" -> null
		// VS_LIMP 에서 BB 는 폴드 대신 무료로 체크한다.
		value == "Fold" -> if (scenario == PreflopScenario.VS_LIMP) PreflopAction.CHECK else PreflopAction.FOLD
		// RFI(첫 오픈)에서 "Call" 은 콜이 아니라 블라인드 컴플리트 = 림프다. (SB 전용)
		value == "Call" -> if (scenario == PreflopScenario.RFI) PreflopAction.LIMP else PreflopAction.CALL
		value == "Limp" -> PreflopAction.LIMP
		value == "Check" -> PreflopAction.CHECK
		value == "All-in" -> PreflopAction.ALL_IN
		value.startsWith("4-bet") ->
			if (value.contains("Bluff")) PreflopAction.FOUR_BET_BLUFF else PreflopAction.FOUR_BET
		value.startsWith("3-bet") -> when {
			value.contains("Jam") -> PreflopAction.THREE_BET_JAM
			value.contains("Fold") -> PreflopAction.THREE_BET_FOLD
			value.contains("Call") -> PreflopAction.THREE_BET_CALL
			value.contains("Stackoff") -> PreflopAction.THREE_BET_STACKOFF
			value.contains("Bluff") -> PreflopAction.THREE_BET_BLUFF
			else -> PreflopAction.THREE_BET
		}
		value.startsWith("Raise") -> when {
			value.contains("Jam") || value.contains("shove") -> PreflopAction.RAISE_JAM
			value.contains("4bet") -> PreflopAction.RAISE_4BET
			value.contains("Fold") -> PreflopAction.RAISE_FOLD
			value.contains("Call") -> PreflopAction.RAISE_CALL
			value.contains("Bluff") -> PreflopAction.RAISE_BLUFF
			else -> PreflopAction.RAISE
		}
		else -> PreflopAction.RAISE
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
		// VS_LIMP 는 SB 림프에 대응하는 BB 단일 매치업으로 고정한다.
		if (scenario == PreflopScenario.VS_LIMP) return listOf(Position.BB to Position.SB)
		if (chartName.contains("Limp") || chartName.contains("Jam")) return emptyList()

		val cleaned = chartName
			.replace("RFI", "", ignoreCase = true)
			.replace("3bet", "", ignoreCase = true)
			.replace("All-In", "", ignoreCase = true)
			.replace("Raise", "", ignoreCase = true)
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

	/** 파일마다 다른 포지션 표기("Big Blind"/"Small Blind")를 짧은 표기(BB/SB)로 통일. */
	private fun normalizeChartName(name: String): String = name
		.replace("Big Blind", "BB", ignoreCase = true)
		.replace("Small Blind", "SB", ignoreCase = true)

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

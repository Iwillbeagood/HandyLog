package com.hand.log.domain.model.preflop

import com.hand.log.domain.model.Position

/** 차트 세트. VS_LIMP 는 SB 림프에 대응하는 BB 단일 매치업(포지션 고정)이다. */
enum class PreflopScenario { RFI, FACING_RFI, VS_3BET, VS_LIMP }

/**
 * 특정 상황 하나에 대한 169칸 차트. [actions] 는 핸드 표기(notation) → 액션.
 * Not in Range 핸드는 [actions] 에 포함되지 않으며(=null), 그리드에서 빈칸으로 표시된다.
 */
data class PreflopChart(
	val actions: Map<String, PreflopAction>,
) {
	fun actionFor(hand: PreflopHand): PreflopAction? =
		actions[hand.notation]

	/** 실제 범위 데이터가 아직 채워지지 않은 차트인지. */
	val isEmpty: Boolean get() = actions.isEmpty()

	/** 차트에 실제로 등장하는 액션들(범례 표시용). */
	val presentActions: List<PreflopAction>
		get() = PreflopAction.entries.filter { action -> actions.values.any { it == action } }

	companion object {
		val EMPTY = PreflopChart(emptyMap())
	}
}

/**
 * 차트 조회 키.
 * - RFI: [hero] 만 사용, [villain] = null
 * - FACING_RFI: [villain] = 먼저 레이즈한 상대 포지션
 * - VS_3BET: [villain] = 3벳(또는 올인)한 상대 포지션
 */
data class PreflopChartQuery(
	val stack: PreflopStack,
	val scenario: PreflopScenario,
	val hero: Position,
	val villain: Position? = null,
)

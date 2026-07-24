package com.hand.log.domain.model.preflop

import com.hand.log.domain.model.Position

/**
 * 9-max 프리플랍 액션 순서 및 시나리오별 선택 가능한 포지션.
 * order = 먼저 행동하는 순서(UTG 최선두 → BB 최후미).
 */
object PreflopPositions {

	val order: List<Position> = listOf(
		Position.UTG,
		Position.UTG1,
		Position.UTG2,
		Position.LJ,
		Position.HJ,
		Position.CO,
		Position.BTN,
		Position.SB,
		Position.BB,
	)

	/** RFI(먼저 오픈) 가능한 히어로 — BB 제외(폴드로 넘어오면 BB 승리). */
	val rfiHeroes: List<Position> = order.dropLast(1)

	/** Facing RFI 히어로 — UTG 제외(앞에 레이저가 없음). */
	val facingRfiHeroes: List<Position> = order.drop(1)

	/** vs 3bet 히어로 — 오픈한 뒤 뒤에서 3벳당하는 상황이므로 RFI 히어로와 동일. */
	val vs3betHeroes: List<Position> = rfiHeroes

	/** [hero] 보다 먼저 행동하는(=먼저 레이즈했을 수 있는) 포지션들. */
	fun raisersBefore(hero: Position): List<Position> {
		val idx = order.indexOf(hero)
		return if (idx <= 0) emptyList() else order.subList(0, idx)
	}

	/** [hero] 보다 뒤에 행동하는(=3벳할 수 있는) 포지션들. */
	fun threeBettorsAfter(hero: Position): List<Position> {
		val idx = order.indexOf(hero)
		return if (idx < 0 || idx == order.lastIndex) emptyList() else order.subList(idx + 1, order.size)
	}
}

package com.hand.log.domain.model

/**
 * 포커 핸드 족보
 * 낮은 ordinal = 더 강한 핸드
 */
enum class HandRanking(val label: String) {
	ROYAL_FLUSH("로열 플러시"),
	STRAIGHT_FLUSH("스트레이트 플러시"),
	FOUR_OF_A_KIND("포카드"),
	FULL_HOUSE("풀하우스"),
	FLUSH("플러시"),
	STRAIGHT("스트레이트"),
	THREE_OF_A_KIND("트리플"),
	TWO_PAIR("투페어"),
	ONE_PAIR("원페어"),
	HIGH_CARD("하이카드"),
}

/**
 * 평가된 핸드 결과. [Comparable]로 핸드끼리 비교 가능.
 * ranking이 같으면 kickers로 비교 (내림차순).
 */
data class EvaluatedHand(
	val ranking: HandRanking,
	val kickers: List<Int>,
) : Comparable<EvaluatedHand> {
	override fun compareTo(other: EvaluatedHand): Int {
		val rankCmp = this.ranking.ordinal.compareTo(other.ranking.ordinal)
		if (rankCmp != 0) return rankCmp
		for (i in kickers.indices) {
			if (i >= other.kickers.size) return -1
			val cmp = other.kickers[i].compareTo(this.kickers[i])
			if (cmp != 0) return cmp
		}
		return 0
	}
}

data class ShowdownResult(
	val seat: Int,
	val ranking: HandRanking,
	val bestCards: List<Card> = emptyList(),
	val isWinner: Boolean,
)

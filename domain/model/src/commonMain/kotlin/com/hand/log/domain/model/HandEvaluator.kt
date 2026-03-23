package com.hand.log.domain.model

import kotlinx.serialization.Serializable

/**
 * 포커 핸드 족보
 * 낮은 ordinal = 더 강한 핸드
 */
enum class HandRanking {
	ROYAL_FLUSH,
	STRAIGHT_FLUSH,
	FOUR_OF_A_KIND,
	FULL_HOUSE,
	FLUSH,
	STRAIGHT,
	THREE_OF_A_KIND,
	TWO_PAIR,
	ONE_PAIR,
	HIGH_CARD,
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

@Serializable
data class ShowdownResult(
	val seat: Int,
	val ranking: HandRanking,
	val bestCards: List<Card> = emptyList(),
	val isWinner: Boolean,
)

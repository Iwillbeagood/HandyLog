package com.hand.log.domain.model.preflop

import com.hand.log.domain.model.Rank

/** 169개 프리플랍 핸드의 형태. */
enum class HandShape { PAIR, SUITED, OFFSUIT }

/**
 * 프리플랍 차트의 한 칸(=169개 스타팅 핸드 중 하나).
 * [high] 는 항상 [low] 이상(랭크 ordinal 이 더 작거나 같음)이다. 페어는 high == low.
 */
data class PreflopHand(
	val high: Rank,
	val low: Rank,
	val shape: HandShape,
) {
	/** "AA", "AKs", "AKo", "T9s" 같은 표준 표기. 10 은 그리드 표기상 "T" 로 축약한다. */
	val notation: String
		get() = when (shape) {
			HandShape.PAIR -> "${high.chartChar}${high.chartChar}"
			HandShape.SUITED -> "${high.chartChar}${low.chartChar}s"
			HandShape.OFFSUIT -> "${high.chartChar}${low.chartChar}o"
		}
}

/** 그리드/표기용 랭크 문자. TEN 만 "10" → "T" 로 축약, 나머지는 기존 심볼 사용. */
val Rank.chartChar: String
	get() = if (this == Rank.TEN) "T" else symbol

/**
 * 13x13 프리플랍 그리드. 행/열 모두 A→2 순서.
 * 대각선=페어, 대각선 위(행<열)=수딧, 대각선 아래(행>열)=오프수딧 → AKs 는 우상단.
 */
object PreflopGrid {
	val ranks: List<Rank> = Rank.entries

	val rows: List<List<PreflopHand>> = ranks.mapIndexed { r, rowRank ->
		ranks.mapIndexed { c, colRank ->
			when {
				r == c -> PreflopHand(rowRank, rowRank, HandShape.PAIR)
				r < c -> PreflopHand(rowRank, colRank, HandShape.SUITED)
				else -> PreflopHand(colRank, rowRank, HandShape.OFFSUIT)
			}
		}
	}
}

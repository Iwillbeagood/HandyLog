package com.hand.log.ui.poker

/**
 * 칩/금액을 간략하게 포맷 (1K, 1.5M 등)
 */
fun formatChips(amount: Double): String {
	return when {
		amount >= 1_000_000 -> "${formatDecimal(amount / 1_000_000)}M"
		amount >= 1_000 -> "${formatDecimal(amount / 1_000)}K"
		amount % 1.0 == 0.0 -> amount.toLong().toString()
		else -> amount.toLong().toString()
	}
}

/**
 * BB 단위 또는 칩 단위 문자열로 변환
 */
fun formatAmountOrBb(amount: Double, useBbUnit: Boolean = false, bb: Double = 0.0): String {
	return if (useBbUnit && bb > 0) {
		val bbCount = (amount * 10 / bb).toLong() / 10.0
		if (bbCount == bbCount.toLong().toDouble()) {
			"${bbCount.toLong()}BB"
		} else {
			"${bbCount}BB"
		}
	} else {
		formatChips(amount)
	}
}

private fun formatDecimal(value: Double): String {
	return if (value % 1.0 == 0.0) {
		value.toLong().toString()
	} else {
		((value * 10).toLong() / 10.0).toString()
	}
}

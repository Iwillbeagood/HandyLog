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
 * BB 단위 또는 칩 약식(K/M) 문자열로 변환
 */
fun formatAmountOrBb(amount: Double, useBbUnit: Boolean = false, bb: Double = 0.0): String {
	return if (useBbUnit && bb > 0) {
		formatBbCount(amount / bb)
	} else {
		formatChips(amount)
	}
}

/**
 * BB 단위 또는 콤마 포맷 문자열로 변환
 */
fun formatAmountFull(amount: Double, useBbUnit: Boolean = false, bb: Double = 0.0): String {
	return if (useBbUnit && bb > 0) {
		formatBbCount(amount / bb)
	} else {
		formatWithComma(amount.toLong())
	}
}

/**
 * BB 수를 문자열로 변환 (예: "2BB", "3.5BB")
 */
fun formatBbCount(bbCount: Double): String {
	val rounded2 = (bbCount * 100).toLong() / 100.0
	return when {
		rounded2 == rounded2.toLong().toDouble() -> "${rounded2.toLong()}BB"
		rounded2 * 10 == (rounded2 * 10).toLong().toDouble() -> "${(rounded2 * 10).toLong() / 10.0}BB"
		else -> "${rounded2}BB"
	}
}

fun formatWithComma(value: Long): String {
	val isNegative = value < 0
	val absValue = if (isNegative) -value else value
	val str = absValue.toString()
	val result = buildString {
		str.forEachIndexed { index, c ->
			if (index > 0 && (str.length - index) % 3 == 0) append(',')
			append(c)
		}
	}
	return if (isNegative) "-$result" else result
}

private fun formatDecimal(value: Double): String {
	return if (value % 1.0 == 0.0) {
		value.toLong().toString()
	} else {
		((value * 10).toLong() / 10.0).toString()
	}
}

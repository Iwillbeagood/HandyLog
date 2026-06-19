package com.hand.log.ui.poker

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
	val rounded1 = (bbCount * 10).toLong() / 10.0
	return when {
		rounded1 == rounded1.toLong().toDouble() -> "${rounded1.toLong()}BB"
		else -> "${rounded1}BB"
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

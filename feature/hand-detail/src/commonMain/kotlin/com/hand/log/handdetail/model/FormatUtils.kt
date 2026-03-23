package com.hand.log.handdetail.model

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

fun formatAmount(amount: Double, useBbUnit: Boolean, bb: Double): String {
	return if (useBbUnit && bb > 0) {
		val bbCount = (amount * 10 / bb).toLong() / 10.0
		if (bbCount == bbCount.toLong().toDouble()) {
			"${bbCount.toLong()}BB"
		} else {
			"${bbCount}BB"
		}
	} else {
		formatWithComma(amount.toLong())
	}
}

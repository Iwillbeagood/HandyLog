package com.hand.log.utils.currency

object CurrencyFormatter {

	private const val CURRENCY_UNIT = "원"

	fun formatAmountWon(amount: Int?): String {
		if (amount == null) return "0$CURRENCY_UNIT"
		return "${formatWithCommas(amount.toLong())}$CURRENCY_UNIT"
	}

	fun formatAmountWon(amount: Long?): String {
		if (amount == null) return "0$CURRENCY_UNIT"
		return "${formatWithCommas(amount)}$CURRENCY_UNIT"
	}

	fun formatAmountWon(amount: Double?): String {
		if (amount == null) return "0$CURRENCY_UNIT"
		return "${formatWithCommas(amount.toLong())}$CURRENCY_UNIT"
	}

	fun formatAmount(amount: Int?): String {
		if (amount == null) return "0"
		return formatWithCommas(amount.toLong())
	}

	fun deFormatAmountInt(amount: String): Int {
		return amount.replace(CURRENCY_UNIT, "").replace(",", "").toIntOrNull() ?: 0
	}

	fun deFormatAmountLong(amount: String): Long {
		return amount.replace(CURRENCY_UNIT, "").replace(",", "").toLongOrNull() ?: 0
	}

	fun deFormatAmountDouble(amount: String): Double {
		return amount.replace(CURRENCY_UNIT, "").replace(",", "").toDoubleOrNull() ?: 0.0
	}

	private fun formatWithCommas(value: Long): String {
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
}

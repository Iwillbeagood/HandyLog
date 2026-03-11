package com.hand.log.utils.currency

import java.text.DecimalFormat

object CurrencyFormatter {

	private const val DECIMAL_PATTERN = "#,###"
	private const val CURRENCY_UNIT = "원"

	fun formatAmountWon(amount: Int?): String {
		if (amount == null) return "0$CURRENCY_UNIT"

		val formatter = DecimalFormat(DECIMAL_PATTERN)
		return "${formatter.format(amount)}$CURRENCY_UNIT"
	}

	fun formatAmountWon(amount: Long?): String {
		if (amount == null) return "0$CURRENCY_UNIT"

		val formatter = DecimalFormat(DECIMAL_PATTERN)
		return "${formatter.format(amount)}$CURRENCY_UNIT"
	}

	fun formatAmountWon(amount: Double?): String {
		if (amount == null) return "0$CURRENCY_UNIT"

		val formatter = DecimalFormat(DECIMAL_PATTERN)
		return "${formatter.format(amount)}$CURRENCY_UNIT"
	}

	fun formatAmount(amount: Int?): String {
		if (amount == null) return "0"

		val formatter = DecimalFormat(DECIMAL_PATTERN)
		return formatter.format(amount)
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
}
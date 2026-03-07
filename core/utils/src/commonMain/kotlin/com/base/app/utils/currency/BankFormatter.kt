package com.hand.log.utils.currency

object BankFormatter {

	fun formatAccountNumber(accountNumber: String): String {
		return if (accountNumber.length > 6) {
			"${accountNumber.substring(
				0,
				3,
			)}-${accountNumber.substring(3, 6)}-${accountNumber.substring(6)}"
		} else {
			accountNumber
		}
	}
}
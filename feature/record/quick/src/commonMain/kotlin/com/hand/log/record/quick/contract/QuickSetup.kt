package com.hand.log.record.quick.contract

import androidx.compose.runtime.Immutable
import com.hand.log.domain.model.Blinds
import com.hand.log.domain.model.quick.QuickHandConfig

@Immutable
internal data class QuickSetup(
	val sbText: String = "",
	val bbText: String = "",
	val stackText: String = "",
) {
	val hasBlinds: Boolean
		get() = sbText.isNotBlank() || bbText.isNotBlank()

	val blinds: Blinds?
		get() {
			if (!hasBlinds) return QuickHandConfig.BB_UNIT_BLINDS
			val bb = bbText.toPositiveDouble() ?: return null
			if (sbText.isBlank()) return Blinds(sb = bb / 2, bb = bb)
			val sb = sbText.toPositiveDouble()?.takeIf { it <= bb } ?: return null
			return Blinds(sb = sb, bb = bb)
		}

	val stackInBb: Double?
		get() = stackText.toPositiveDouble()

	val isValid: Boolean
		get() = blinds != null && (stackText.isBlank() || stackInBb != null)

	private fun String.toPositiveDouble(): Double? = toDoubleOrNull()?.takeIf { it > 0 }
}

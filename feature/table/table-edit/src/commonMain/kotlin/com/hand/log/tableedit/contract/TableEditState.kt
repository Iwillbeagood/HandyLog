package com.hand.log.tableedit.contract

import androidx.compose.runtime.Immutable
import com.hand.log.domain.model.GameType

@Immutable
internal data class TableEditState(
	val date: String = "",
	val location: String = "",
	val isCash: Boolean = false,
	val sbText: String = "",
	val bbText: String = "",
	val straddleEnabled: Boolean = false,
	val straddleText: String = "",
	val bigBlindAnteEnabled: Boolean = true,
	val maxPlayers: Int = 9,
	val playerCount: Int = 9,
	val heroSeat: Int = 1,
	val isEditMode: Boolean = false,
) {
	val isSubmitEnabled: Boolean
		get() = date.isNotBlank()

	fun buildGameType(): GameType = if (isCash) {
		GameType.Cash(
			sb = sbText.toDoubleOrNull() ?: 0.0,
			bb = bbText.toDoubleOrNull() ?: 0.0,
			straddle = if (straddleEnabled) straddleText.toDoubleOrNull() else null,
		)
	} else {
		GameType.Tournament(
			isBigBlindAnte = bigBlindAnteEnabled,
		)
	}
}

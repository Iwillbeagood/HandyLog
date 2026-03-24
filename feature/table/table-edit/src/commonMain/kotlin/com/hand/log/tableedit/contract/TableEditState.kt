package com.hand.log.tableedit.contract

import androidx.compose.runtime.Immutable
import com.hand.log.domain.model.Blinds
import com.hand.log.domain.model.GameType

@Immutable
internal data class TableEditState(
	val date: String = "",
	val location: String = "",
	val gameType: GameType = GameType.TOURNAMENT,
	val startingStack: String = "",
	val sbText: String = "",
	val bbText: String = "",
	val straddleEnabled: Boolean = false,
	val straddleText: String = "",
	val bigBlindAnteEnabled: Boolean = true,
	val playerCount: Int = 9,
	val heroSeat: Int = 1,
	val isEditMode: Boolean = false,
) {
	val isSubmitEnabled: Boolean
		get() = date.isNotBlank() && startingStack.isNotBlank()

	// title, buttonText는 UI에서 isEditMode 기반으로 stringResource 사용

	fun buildBlinds(): Blinds = when (gameType) {
		GameType.CASH -> Blinds(
			sb = sbText.toDoubleOrNull() ?: 0.0,
			bb = bbText.toDoubleOrNull() ?: 0.0,
			straddle = if (straddleEnabled) straddleText.toDoubleOrNull() else null,
		)
		GameType.TOURNAMENT -> Blinds(
			sb = 0.0,
			bb = 0.0,
			isBigBlindAnte = bigBlindAnteEnabled,
		)
	}
}

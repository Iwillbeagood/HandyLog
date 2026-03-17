package com.hand.log.ui.table

import androidx.compose.runtime.Immutable
import com.hand.log.domain.model.GameType

@Immutable
data class TableFormState(
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

	val title: String
		get() = if (isEditMode) "테이블 수정" else "새 테이블 생성"

	val buttonText: String
		get() = if (isEditMode) "수정 완료" else "테이블 생성"
}

package com.hand.log.handdetail.contract

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import com.hand.log.domain.model.HandRecord
import com.hand.log.handdetail.model.HandDetailUiModel

@Stable
internal sealed interface HandDetailState {

	@Immutable
	data object Loading : HandDetailState

	@Immutable
	data class Success(
		val hand: HandRecord,
		val useBbUnit: Boolean = false,
		val uiModel: HandDetailUiModel,
	) : HandDetailState

	@Immutable
	data object Error : HandDetailState
}

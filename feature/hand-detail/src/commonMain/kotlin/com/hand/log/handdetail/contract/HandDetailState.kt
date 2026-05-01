package com.hand.log.handdetail.contract

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import com.hand.log.domain.model.HandRecord

@Stable
internal sealed interface HandDetailState {

	@Immutable
	data object Loading : HandDetailState

	@Immutable
	data class Detail(
		val hand: HandRecord,
		val useBbUnit: Boolean = false,
	) : HandDetailState

	@Immutable
	data object Error : HandDetailState
}

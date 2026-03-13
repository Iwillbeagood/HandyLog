package com.hand.log.table.contract

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import com.hand.log.domain.model.HandRecord
import com.hand.log.domain.model.PokerTable

@Stable
internal sealed interface TableDetailState {

	@Immutable
	data object Loading : TableDetailState

	@Immutable
	data class TableData(
		val table: PokerTable,
		val hands: List<HandRecord> = emptyList(),
	) : TableDetailState
}

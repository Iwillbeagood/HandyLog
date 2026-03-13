package com.hand.log.home.contract

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import com.hand.log.domain.model.PokerTable

@Stable
internal sealed interface HomeState {

	@Immutable
	data object Loading : HomeState

	@Immutable
	data class HomeData(
		val tables: List<TableListItem> = emptyList(),
	) : HomeState
}

@Immutable
data class TableListItem(
	val table: PokerTable,
	val handCount: Int,
)

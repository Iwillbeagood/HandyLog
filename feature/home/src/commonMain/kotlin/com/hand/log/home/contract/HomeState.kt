package com.hand.log.home.contract

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import com.hand.log.domain.model.HandWithTable
import com.hand.log.domain.model.TableListItem

@Stable
internal sealed interface HomeState {

	@Immutable
	data object Loading : HomeState

	@Immutable
	data class HomeData(
		val tables: List<TableListItem> = emptyList(),
		val hands: List<HandWithTable> = emptyList(),
	) : HomeState
}

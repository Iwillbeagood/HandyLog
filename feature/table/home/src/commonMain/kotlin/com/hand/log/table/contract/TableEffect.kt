package com.hand.log.table.contract

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import org.jetbrains.compose.resources.StringResource

@Stable
internal sealed interface TableEffect {

	@Immutable
	data class ShowToast(val message: StringResource) : TableEffect

	@Immutable
	data class ShowToastAndPop(val message: StringResource) : TableEffect

	@Immutable
	data class NavigateToRecordHand(val tableId: String) : TableEffect
}

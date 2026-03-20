package com.hand.log.tableedit.contract

import androidx.compose.runtime.Immutable
import com.hand.log.domain.model.PokerTable

@Immutable
internal sealed interface TableEditEffect {
	data class SaveComplete(val table: PokerTable) : TableEditEffect
}

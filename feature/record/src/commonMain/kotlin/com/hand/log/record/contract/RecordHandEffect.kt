package com.hand.log.record.contract

import androidx.compose.runtime.Immutable

@Immutable
internal sealed interface RecordHandModalEffect {
	data object Idle : RecordHandModalEffect
}

@Immutable
internal sealed interface RecordHandEffect {
	data object SaveSuccess : RecordHandEffect
	data class SaveError(val message: String) : RecordHandEffect
}

package com.hand.log.domain.model.etc

sealed interface ConnectionState {
	data object Available : ConnectionState
	data object Unavailable : ConnectionState
}

package com.hand.log.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Blinds(
	val sb: Double,
	val bb: Double,
	val straddle: Double? = null,
	val isBigBlindAnte: Boolean = false,
)

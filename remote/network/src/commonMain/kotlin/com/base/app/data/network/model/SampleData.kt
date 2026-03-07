package com.hand.log.data.network.model

import kotlinx.serialization.Serializable

@Serializable
internal data class SampleData(
	val id: Long,
	val name: String,
)

package com.hand.log.navigation.argument

import kotlinx.serialization.Serializable

@Serializable
sealed interface AddType {

	@Serializable
	data object New : AddType

	@Serializable
	data class Edit(val id: Long) : AddType
}
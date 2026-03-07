package com.hand.log.local.datastore

import kotlinx.serialization.Serializable

@Serializable
data class Fruittie(
    val id: Long = 0,
    val name: String,
    val fullName: String,
    val calories: String,
)

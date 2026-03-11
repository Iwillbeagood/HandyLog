package com.hand.log.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "hands")
data class HandEntity(
	@PrimaryKey val id: String,
	val date: Long,
	val tableSize: Int,
	val heroPosition: String? = null,
	val potSize: Double? = null,
	val notes: String? = null,
	val createdAt: Long
)

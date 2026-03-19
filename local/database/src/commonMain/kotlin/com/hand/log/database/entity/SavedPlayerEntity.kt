package com.hand.log.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saved_players")
data class SavedPlayerEntity(
	@PrimaryKey val id: String,
	val name: String,
	val tendency: String? = null,
	val memo: String? = null,
	val createdAt: Long = 0L,
)

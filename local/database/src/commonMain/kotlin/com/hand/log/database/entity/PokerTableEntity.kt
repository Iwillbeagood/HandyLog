package com.hand.log.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.datetime.LocalDate

@Entity(tableName = "poker_tables")
data class PokerTableEntity(
	@PrimaryKey val id: String,
	val date: LocalDate,
	val location: String? = null,
	val gameType: String,
	val maxPlayers: Int = 0,
	val heroSeat: Int,
	val createdAt: Long,
	val hasShownPositionSetup: Boolean = false,
)

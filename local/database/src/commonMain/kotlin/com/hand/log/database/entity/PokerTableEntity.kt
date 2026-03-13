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
	val startingStack: Double,
	val blindsSb: Double? = null,
	val blindsBb: Double? = null,
	val blindsStraddle: Double? = null,
	val playerCount: Int,
	val heroSeat: Int,
	val createdAt: Long,
)

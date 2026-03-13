package com.hand.log.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
	tableName = "table_players",
	foreignKeys = [
		ForeignKey(
			entity = PokerTableEntity::class,
			parentColumns = ["id"],
			childColumns = ["tableId"],
			onDelete = ForeignKey.CASCADE,
		),
	],
)
data class TablePlayerEntity(
	@PrimaryKey val id: String,
	val tableId: String,
	val seat: Int,
	val stack: Double,
	val tendency: String? = null,
	val memo: String? = null,
	val name: String? = null,
)

package com.hand.log.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
	tableName = "table_players",
	indices = [Index("tableId")],
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
	val tendency: String? = null,
	val memo: String? = null,
	val name: String? = null,
	val savedPlayerId: String? = null,
)

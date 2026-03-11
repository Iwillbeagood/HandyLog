package com.hand.log.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
	tableName = "hand_players",
	foreignKeys = [
		ForeignKey(
			entity = HandEntity::class,
			parentColumns = ["id"],
			childColumns = ["handId"],
			onDelete = ForeignKey.CASCADE
		)
	]
)
data class HandPlayerEntity(
	@PrimaryKey val id: String,
	val handId: String,
	val position: String,
	val card1Rank: String? = null,
	val card1Suit: String? = null,
	val card2Rank: String? = null,
	val card2Suit: String? = null,
	val result: String? = null
)

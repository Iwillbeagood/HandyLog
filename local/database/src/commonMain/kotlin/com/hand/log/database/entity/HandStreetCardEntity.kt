package com.hand.log.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
	tableName = "hand_street_cards",
	foreignKeys = [
		ForeignKey(
			entity = HandRecordEntity::class,
			parentColumns = ["id"],
			childColumns = ["handId"],
			onDelete = ForeignKey.CASCADE,
		),
	],
)
data class HandStreetCardEntity(
	@PrimaryKey val id: String,
	val handId: String,
	val street: String,
	val cardIndex: Int,
	val rank: String,
	val suit: String,
)

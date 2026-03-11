package com.hand.log.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
	tableName = "hand_community_cards",
	foreignKeys = [
		ForeignKey(
			entity = HandEntity::class,
			parentColumns = ["id"],
			childColumns = ["handId"],
			onDelete = ForeignKey.CASCADE
		)
	]
)
data class HandCommunityCardEntity(
	@PrimaryKey val id: String,
	val handId: String,
	val cardIndex: Int,
	val rank: String,
	val suit: String
)

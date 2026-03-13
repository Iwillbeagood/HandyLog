package com.hand.log.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
	tableName = "hand_street_actions",
	foreignKeys = [
		ForeignKey(
			entity = HandRecordEntity::class,
			parentColumns = ["id"],
			childColumns = ["handId"],
			onDelete = ForeignKey.CASCADE,
		),
	],
)
data class HandStreetActionEntity(
	@PrimaryKey val id: String,
	val handId: String,
	val street: String,
	val playerSeat: Int,
	val actionType: String,
	val amount: Double? = null,
	val actionOrder: Int,
)

package com.hand.log.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
	tableName = "hand_actions",
	foreignKeys = [
		ForeignKey(
			entity = HandEntity::class,
			parentColumns = ["id"],
			childColumns = ["handId"],
			onDelete = ForeignKey.CASCADE
		)
	]
)
data class HandActionEntity(
	@PrimaryKey val id: String,
	val handId: String,
	val street: String,
	val position: String,
	val actionType: String,
	val amount: Double? = null,
	val actionOrder: Int
)

package com.hand.log.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.hand.log.domain.model.Blinds
import com.hand.log.domain.model.HandStreets
import com.hand.log.domain.model.HeroHand

@Entity(
	tableName = "hand_records",
	foreignKeys = [
		ForeignKey(
			entity = PokerTableEntity::class,
			parentColumns = ["id"],
			childColumns = ["tableId"],
			onDelete = ForeignKey.CASCADE,
		),
	],
)
data class HandRecordEntity(
	@PrimaryKey val id: String,
	val tableId: String,
	val createdAt: Long,
	val blinds: Blinds? = null,
	val heroHand: HeroHand? = null,
	val heroStack: Double = 0.0,
	val buttonSeat: Int = 1,
	val streets: HandStreets = HandStreets(),
	val result: Double? = null,
	val memo: String? = null,
)

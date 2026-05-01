package com.hand.log.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.hand.log.domain.model.Blinds
import com.hand.log.domain.model.HandPlayer
import com.hand.log.domain.model.HandStreets

@Entity(
	tableName = "hand_records",
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
data class HandRecordEntity(
	@PrimaryKey val id: String,
	val tableId: String,
	val createdAt: Long,
	val blinds: Blinds? = null,
	val heroSeat: Int = 0,
	val buttonSeat: Int = 1,
	val streets: HandStreets = HandStreets(),
	val players: List<HandPlayer> = emptyList(),
	val result: Double? = null,
	val memo: String? = null,
)

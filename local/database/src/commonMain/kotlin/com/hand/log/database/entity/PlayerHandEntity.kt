package com.hand.log.database.entity

import androidx.room.Entity
import com.hand.log.domain.model.Position
import kotlinx.serialization.Serializable

@Serializable
@Entity(
	tableName = "player_hand",
	primaryKeys = ["handHistoryId", "position"]
)
data class PlayerHandEntity(
	val handHistoryId: String,
	val position: Position,
	val cardsJson: String?, // JSON string of [Card, Card] | null
	val result: String? = null // 'win' | 'lose' | 'split'
)


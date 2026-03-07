package com.hand.log.database.entity

import androidx.room.Entity
import com.hand.log.domain.model.ActionType
import com.hand.log.domain.model.Position
import com.hand.log.domain.model.Street
import kotlinx.serialization.Serializable

@Serializable
@Entity(
	tableName = "hand_action",
	primaryKeys = ["handHistoryId", "actionIndex"]
)
data class ActionEntity(
	val handHistoryId: String,
	val actionIndex: Int, // To maintain order
	val position: Position,
	val type: ActionType,
	val amount: Int? = null,
	val street: Street
)


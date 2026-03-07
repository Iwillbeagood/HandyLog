/*
 * Copyright 2024 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hand.log.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.hand.log.domain.model.ActionType
import com.hand.log.domain.model.Position
import com.hand.log.domain.model.Rank
import com.hand.log.domain.model.Street
import com.hand.log.domain.model.Suit
import kotlinx.serialization.Serializable

// Card data class
@Serializable
data class Card(
	val rank: Rank,
	val suit: Suit
)

@Serializable
@Entity(tableName = "hand_history")
data class HandHistory(
	@PrimaryKey val id: String,
	val date: Long, // Store as timestamp
	val tableSize: Int,
	val heroPosition: Position? = null,
	val communityCardsJson: String, // JSON string of Card[]
	val potSize: Int? = null,
	val notes: String? = null
)

// PlayerHand Entity
@Serializable
@Entity(
    tableName = "player_hand",
    primaryKeys = ["handHistoryId", "position"]
)
data class PlayerHand(
    val handHistoryId: String,
    val position: Position,
    val cardsJson: String?, // JSON string of [Card, Card] | null
    val result: String? = null // 'win' | 'lose' | 'split'
)

// Action Entity
@Serializable
@Entity(
    tableName = "action",
    primaryKeys = ["handHistoryId", "actionIndex"]
)
data class Action(
	val handHistoryId: String,
	val actionIndex: Int, // To maintain order
	val position: Position,
	val type: ActionType,
	val amount: Int? = null,
	val street: Street
)


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
package com.hand.log.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.hand.log.database.entity.ActionEntity
import com.hand.log.database.entity.HandHistoryEntity
import com.hand.log.database.entity.PlayerHandEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HandHistoryDao {
    // HandHistory operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHandHistory(handHistory: HandHistoryEntity)

    @Update
    suspend fun updateHandHistory(handHistory: HandHistoryEntity)

    @Delete
    suspend fun deleteHandHistory(handHistory: HandHistoryEntity)

    @Query("SELECT * FROM hand_history ORDER BY date DESC")
    fun getAllHandHistories(): Flow<List<HandHistoryEntity>>

    @Query("SELECT * FROM hand_history WHERE id = :handId")
    suspend fun getHandHistoryById(handId: String): HandHistoryEntity?

    @Query("SELECT * FROM hand_history WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getHandHistoriesByDateRange(startDate: Long, endDate: Long): Flow<List<HandHistoryEntity>>

    // PlayerHand operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlayerHand(playerHand: PlayerHandEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlayerHands(playerHands: List<PlayerHandEntity>)

    @Query("SELECT * FROM player_hand WHERE handHistoryId = :handId")
    suspend fun getPlayerHandsForHistory(handId: String): List<PlayerHandEntity>

    @Query("DELETE FROM player_hand WHERE handHistoryId = :handId")
    suspend fun deletePlayerHandsForHistory(handId: String)

    // Action operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAction(action: ActionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActions(actions: List<ActionEntity>)

    @Query("SELECT * FROM hand_action WHERE handHistoryId = :handId ORDER BY actionIndex ASC")
    suspend fun getActionsForHistory(handId: String): List<ActionEntity>

    @Query("DELETE FROM hand_action WHERE handHistoryId = :handId")
    suspend fun deleteActionsForHistory(handId: String)

    // Transaction to insert complete hand history with related data
    @Transaction
    suspend fun insertCompleteHandHistory(
        handHistory: HandHistoryEntity,
        playerHands: List<PlayerHandEntity>,
        actions: List<ActionEntity>
    ) {
        insertHandHistory(handHistory)
        insertPlayerHands(playerHands)
        insertActions(actions)
    }

    // Transaction to delete complete hand history with related data
    @Transaction
    suspend fun deleteCompleteHandHistory(handId: String) {
        deletePlayerHandsForHistory(handId)
        deleteActionsForHistory(handId)
        // Delete the hand history itself
        val handHistory = getHandHistoryById(handId)
        handHistory?.let { deleteHandHistory(it) }
    }

    @Query("DELETE FROM hand_history")
    suspend fun deleteAllHandHistories()

    @Query("SELECT COUNT(*) FROM hand_history")
    suspend fun getHandHistoryCount(): Int
}


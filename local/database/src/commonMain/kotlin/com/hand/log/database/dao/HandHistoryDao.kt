package com.hand.log.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.hand.log.database.entity.HandActionEntity
import com.hand.log.database.entity.HandCommunityCardEntity
import com.hand.log.database.entity.HandEntity
import com.hand.log.database.entity.HandPlayerEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HandHistoryDao {

	@Insert(onConflict = OnConflictStrategy.REPLACE)
	suspend fun insertHand(hand: HandEntity)

	@Update
	suspend fun updateHand(hand: HandEntity)

	@Delete
	suspend fun deleteHand(hand: HandEntity)

	@Query("SELECT * FROM hands ORDER BY date DESC")
	fun getAllHands(): Flow<List<HandEntity>>

	@Query("SELECT * FROM hands WHERE id = :handId")
	suspend fun getHandById(handId: String): HandEntity?

	@Query("SELECT * FROM hands WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
	fun getHandsByDateRange(startDate: Long, endDate: Long): Flow<List<HandEntity>>

	// HandPlayer operations
	@Insert(onConflict = OnConflictStrategy.REPLACE)
	suspend fun insertPlayers(players: List<HandPlayerEntity>)

	@Query("SELECT * FROM hand_players WHERE handId = :handId")
	suspend fun getPlayersForHand(handId: String): List<HandPlayerEntity>

	@Query("DELETE FROM hand_players WHERE handId = :handId")
	suspend fun deletePlayersForHand(handId: String)

	// HandAction operations
	@Insert(onConflict = OnConflictStrategy.REPLACE)
	suspend fun insertActions(actions: List<HandActionEntity>)

	@Query("SELECT * FROM hand_actions WHERE handId = :handId ORDER BY actionOrder ASC")
	suspend fun getActionsForHand(handId: String): List<HandActionEntity>

	@Query("DELETE FROM hand_actions WHERE handId = :handId")
	suspend fun deleteActionsForHand(handId: String)

	// HandCommunityCard operations
	@Insert(onConflict = OnConflictStrategy.REPLACE)
	suspend fun insertCommunityCards(cards: List<HandCommunityCardEntity>)

	@Query("SELECT * FROM hand_community_cards WHERE handId = :handId ORDER BY cardIndex ASC")
	suspend fun getCommunityCardsForHand(handId: String): List<HandCommunityCardEntity>

	@Query("DELETE FROM hand_community_cards WHERE handId = :handId")
	suspend fun deleteCommunityCardsForHand(handId: String)

	@Transaction
	suspend fun insertCompleteHand(
		hand: HandEntity,
		players: List<HandPlayerEntity>,
		actions: List<HandActionEntity>,
		communityCards: List<HandCommunityCardEntity>
	) {
		insertHand(hand)
		insertPlayers(players)
		insertActions(actions)
		insertCommunityCards(communityCards)
	}

	@Transaction
	suspend fun deleteCompleteHand(handId: String) {
		val hand = getHandById(handId)
		hand?.let { deleteHand(it) }
	}

	@Query("DELETE FROM hands")
	suspend fun deleteAllHands()

	@Query("SELECT COUNT(*) FROM hands")
	suspend fun getHandCount(): Int
}

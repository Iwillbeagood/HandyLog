package com.hand.log.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.hand.log.database.entity.HandRecordEntity
import com.hand.log.database.entity.HandStreetActionEntity
import com.hand.log.database.entity.HandStreetCardEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HandRecordDao {

	@Insert(onConflict = OnConflictStrategy.REPLACE)
	suspend fun insertHand(hand: HandRecordEntity)

	@Delete
	suspend fun deleteHand(hand: HandRecordEntity)

	@Query("SELECT * FROM hand_records WHERE tableId = :tableId ORDER BY createdAt DESC")
	fun observeHandsByTableId(tableId: String): Flow<List<HandRecordEntity>>

	@Query("SELECT * FROM hand_records WHERE id = :handId")
	suspend fun getHandById(handId: String): HandRecordEntity?

	@Query("SELECT COUNT(*) FROM hand_records WHERE tableId = :tableId")
	suspend fun getHandCountByTableId(tableId: String): Int

	@Insert(onConflict = OnConflictStrategy.REPLACE)
	suspend fun insertStreetCards(cards: List<HandStreetCardEntity>)

	@Query("SELECT * FROM hand_street_cards WHERE handId = :handId ORDER BY street, cardIndex ASC")
	suspend fun getStreetCardsForHand(handId: String): List<HandStreetCardEntity>

	@Insert(onConflict = OnConflictStrategy.REPLACE)
	suspend fun insertStreetActions(actions: List<HandStreetActionEntity>)

	@Query("SELECT * FROM hand_street_actions WHERE handId = :handId ORDER BY street, actionOrder ASC")
	suspend fun getStreetActionsForHand(handId: String): List<HandStreetActionEntity>

	@Transaction
	suspend fun insertCompleteHand(
		hand: HandRecordEntity,
		streetCards: List<HandStreetCardEntity>,
		streetActions: List<HandStreetActionEntity>,
	) {
		insertHand(hand)
		insertStreetCards(streetCards)
		insertStreetActions(streetActions)
	}

	@Transaction
	suspend fun deleteCompleteHand(handId: String) {
		val hand = getHandById(handId)
		hand?.let { deleteHand(it) }
	}
}

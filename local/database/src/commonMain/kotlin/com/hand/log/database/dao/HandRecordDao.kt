package com.hand.log.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.hand.log.database.entity.HandRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HandRecordDao {

	@Insert(onConflict = OnConflictStrategy.REPLACE)
	suspend fun insertHand(hand: HandRecordEntity)

	@Query("SELECT * FROM hand_records WHERE tableId = :tableId ORDER BY createdAt DESC")
	fun observeHandsByTableId(tableId: String): Flow<List<HandRecordEntity>>

	@Query("SELECT * FROM hand_records ORDER BY createdAt DESC")
	fun observeAllHands(): Flow<List<HandRecordEntity>>

	@Query("SELECT * FROM hand_records WHERE id = :handId")
	fun observeHandById(handId: String): Flow<HandRecordEntity?>

	@Query("SELECT * FROM hand_records WHERE id = :handId")
	suspend fun getHandById(handId: String): HandRecordEntity?

	@Query("SELECT COUNT(*) FROM hand_records WHERE tableId = :tableId")
	suspend fun getHandCountByTableId(tableId: String): Int

	@Query("DELETE FROM hand_records WHERE id = :handId")
	suspend fun deleteHandById(handId: String)
}

package com.hand.log.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.hand.log.database.entity.QuizRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface QuizRecordDao {

	@Query("SELECT * FROM quiz_records ORDER BY playedAt DESC LIMIT :limit")
	fun observeRecent(limit: Int): Flow<List<QuizRecordEntity>>

	@Insert(onConflict = OnConflictStrategy.REPLACE)
	suspend fun insert(record: QuizRecordEntity)
}

package com.hand.log.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.hand.log.database.entity.SavedPlayerEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedPlayerDao {

	@Query("SELECT * FROM saved_players ORDER BY name ASC")
	fun observeAllPlayers(): Flow<List<SavedPlayerEntity>>

	@Query("SELECT * FROM saved_players WHERE id = :id")
	suspend fun getPlayerById(id: String): SavedPlayerEntity?

	@Query("SELECT * FROM saved_players WHERE name = :name LIMIT 1")
	suspend fun getPlayerByName(name: String): SavedPlayerEntity?

	@Insert(onConflict = OnConflictStrategy.REPLACE)
	suspend fun insertPlayer(player: SavedPlayerEntity)

	@Query(
		"UPDATE saved_players SET name = :name, tendency = :tendency, memo = :memo WHERE id = :id",
	)
	suspend fun updatePlayerInfo(id: String, name: String, tendency: String?, memo: String?)

	@Query("DELETE FROM saved_players WHERE id = :id")
	suspend fun deletePlayer(id: String)
}

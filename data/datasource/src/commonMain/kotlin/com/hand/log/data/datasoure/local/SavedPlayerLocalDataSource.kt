package com.hand.log.data.datasoure.local

import com.hand.log.domain.model.SavedPlayer
import kotlinx.coroutines.flow.Flow

interface SavedPlayerLocalDataSource {
	fun observeAllPlayers(): Flow<List<SavedPlayer>>
	suspend fun getPlayerById(id: String): SavedPlayer?
	suspend fun getPlayerByName(name: String): SavedPlayer?
	suspend fun savePlayer(player: SavedPlayer)
	suspend fun deletePlayer(id: String)
}

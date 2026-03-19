package com.hand.log.domain.repository

import com.hand.log.domain.model.SavedPlayer
import kotlinx.coroutines.flow.Flow

interface SavedPlayerRepository {
	fun observeAllPlayers(): Flow<List<SavedPlayer>>
	suspend fun getPlayerById(id: String): SavedPlayer?
	suspend fun getPlayerByName(name: String): SavedPlayer?
	suspend fun savePlayer(player: SavedPlayer)
	suspend fun deletePlayer(id: String)
}

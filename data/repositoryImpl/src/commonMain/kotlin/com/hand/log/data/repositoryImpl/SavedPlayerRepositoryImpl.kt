package com.hand.log.data.repositoryImpl

import com.hand.log.data.datasoure.local.SavedPlayerLocalDataSource
import com.hand.log.domain.model.SavedPlayer
import com.hand.log.domain.repository.SavedPlayerRepository
import kotlinx.coroutines.flow.Flow

internal class SavedPlayerRepositoryImpl(
	private val localDataSource: SavedPlayerLocalDataSource,
) : SavedPlayerRepository {

	override fun observeAllPlayers(): Flow<List<SavedPlayer>> {
		return localDataSource.observeAllPlayers()
	}

	override suspend fun getPlayerById(id: String): SavedPlayer? {
		return localDataSource.getPlayerById(id)
	}

	override suspend fun getPlayerByName(name: String): SavedPlayer? {
		return localDataSource.getPlayerByName(name)
	}

	override suspend fun savePlayer(player: SavedPlayer) {
		localDataSource.savePlayer(player)
	}

	override suspend fun deletePlayer(id: String) {
		localDataSource.deletePlayer(id)
	}
}

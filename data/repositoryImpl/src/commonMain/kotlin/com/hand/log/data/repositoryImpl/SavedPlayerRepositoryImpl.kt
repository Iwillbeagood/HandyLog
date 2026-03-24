package com.hand.log.data.repositoryImpl

import com.hand.log.data.datasoure.local.SavedPlayerLocalDataSource
import com.hand.log.domain.model.SavedPlayer
import com.hand.log.domain.repository.SavedPlayerRepository
import com.hand.log.utils.etc.Logger
import kotlinx.coroutines.flow.Flow

internal class SavedPlayerRepositoryImpl(
	private val localDataSource: SavedPlayerLocalDataSource,
) : SavedPlayerRepository {

	override fun observeAllPlayers(): Flow<List<SavedPlayer>> =
		localDataSource.observeAllPlayers()

	override suspend fun getPlayerById(id: String): SavedPlayer? =
		localDataSource.getPlayerById(id)

	override suspend fun getPlayerByName(name: String): SavedPlayer? =
		localDataSource.getPlayerByName(name)

	override suspend fun savePlayer(player: SavedPlayer) {
		try {
			localDataSource.savePlayer(player)
			Logger.d("SavedPlayerRepo: savePlayer success (${player.name})")
		} catch (e: Exception) {
			Logger.e("SavedPlayerRepo: savePlayer failed - ${e.message}")
		}
	}

	override suspend fun updatePlayer(player: SavedPlayer) {
		try {
			localDataSource.updatePlayer(player)
			Logger.d("SavedPlayerRepo: updatePlayer success (${player.name})")
		} catch (e: Exception) {
			Logger.e("SavedPlayerRepo: updatePlayer failed - ${e.message}")
		}
	}

	override suspend fun deletePlayer(id: String) {
		try {
			localDataSource.deletePlayer(id)
			Logger.d("SavedPlayerRepo: deletePlayer success ($id)")
		} catch (e: Exception) {
			Logger.e("SavedPlayerRepo: deletePlayer failed - ${e.message}")
		}
	}
}

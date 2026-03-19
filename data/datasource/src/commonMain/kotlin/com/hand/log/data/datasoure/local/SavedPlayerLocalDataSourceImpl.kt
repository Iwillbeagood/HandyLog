package com.hand.log.data.datasoure.local

import com.hand.log.database.dao.SavedPlayerDao
import com.hand.log.database.entity.SavedPlayerEntity
import com.hand.log.domain.model.PlayerTendency
import com.hand.log.domain.model.SavedPlayer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class SavedPlayerLocalDataSourceImpl(
	private val savedPlayerDao: SavedPlayerDao,
) : SavedPlayerLocalDataSource {

	override fun observeAllPlayers(): Flow<List<SavedPlayer>> {
		return savedPlayerDao.observeAllPlayers().map { entities ->
			entities.map { it.toDomain() }
		}
	}

	override suspend fun getPlayerById(id: String): SavedPlayer? {
		return savedPlayerDao.getPlayerById(id)?.toDomain()
	}

	override suspend fun getPlayerByName(name: String): SavedPlayer? {
		return savedPlayerDao.getPlayerByName(name)?.toDomain()
	}

	override suspend fun savePlayer(player: SavedPlayer) {
		savedPlayerDao.insertPlayer(player.toEntity())
	}

	override suspend fun deletePlayer(id: String) {
		savedPlayerDao.deletePlayer(id)
	}

	private fun SavedPlayerEntity.toDomain(): SavedPlayer = SavedPlayer(
		id = id,
		name = name,
		tendency = tendency?.let { PlayerTendency.valueOf(it) },
		memo = memo,
		createdAt = createdAt,
	)

	private fun SavedPlayer.toEntity(): SavedPlayerEntity = SavedPlayerEntity(
		id = id,
		name = name,
		tendency = tendency?.name,
		memo = memo,
		createdAt = createdAt,
	)
}

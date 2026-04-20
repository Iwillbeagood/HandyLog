package com.hand.log.database.impl

import com.hand.log.data.datasoure.local.SavedPlayerLocalDataSource
import com.hand.log.database.dao.SavedPlayerDao
import com.hand.log.database.entity.SavedPlayerEntity
import com.hand.log.domain.model.PlayerTendency
import com.hand.log.domain.model.SavedPlayer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

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

	@OptIn(ExperimentalUuidApi::class, ExperimentalTime::class)
	override suspend fun savePlayer(player: SavedPlayer) {
		val toSave = if (player.id.isBlank()) {
			player.copy(
				id = Uuid.random().toString(),
				createdAt = Clock.System.now().toEpochMilliseconds(),
			)
		} else {
			player
		}
		savedPlayerDao.insertPlayer(toSave.toEntity())
	}

	override suspend fun updatePlayer(player: SavedPlayer) {
		savedPlayerDao.updatePlayerInfo(
			id = player.id,
			name = player.name,
			tendency = player.tendency?.name,
			memo = player.memo,
		)
	}

	override suspend fun deletePlayer(id: String) {
		savedPlayerDao.deletePlayer(id)
	}

	private fun SavedPlayerEntity.toDomain(): SavedPlayer = SavedPlayer(
		id = id,
		name = name,
		tendency = tendency?.let { PlayerTendency.valueOf(it.trim()) },
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

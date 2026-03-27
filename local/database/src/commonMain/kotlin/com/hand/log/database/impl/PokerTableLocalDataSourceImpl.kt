package com.hand.log.database.impl

import com.hand.log.data.datasoure.local.PokerTableLocalDataSource
import com.hand.log.database.dao.PokerTableDao
import com.hand.log.database.entity.TablePlayerEntity
import com.hand.log.database.mapper.toDomain
import com.hand.log.database.mapper.toEntity
import com.hand.log.domain.model.GameType
import com.hand.log.domain.model.Player
import com.hand.log.domain.model.PokerTable
import kotlinx.serialization.json.Json
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

private val json = Json { ignoreUnknownKeys = true }

internal class PokerTableLocalDataSourceImpl(
	private val pokerTableDao: PokerTableDao,
) : PokerTableLocalDataSource {

	override fun observeAllTables(): Flow<List<PokerTable>> {
		return pokerTableDao.observeAllTables().map { tables ->
			tables.map { entity ->
				entity.toDomain(pokerTableDao.getPlayersForTable(entity.id))
			}
		}
	}

	override fun observeTableById(tableId: String): Flow<PokerTable?> {
		return combine(
			pokerTableDao.observeTableById(tableId),
			pokerTableDao.observePlayersForTable(tableId),
		) { entity, playerEntities ->
			entity?.toDomain(playerEntities)
		}
	}

	override suspend fun getTableById(tableId: String): PokerTable? {
		val entity = pokerTableDao.getTableById(tableId) ?: return null
		return entity.toDomain(pokerTableDao.getPlayersForTable(entity.id))
	}

	@OptIn(ExperimentalUuidApi::class, ExperimentalTime::class)
	override suspend fun saveTable(table: PokerTable): PokerTable {
		val now = Clock.System.now().toEpochMilliseconds()
		val generatedId = table.id.ifBlank { Uuid.random().toString() }
		val createdAt = if (table.createdAt == 0L) now else table.createdAt

		val entity = table.copy(id = generatedId).toEntity(createdAt)
		val playerEntities = table.players.map { player ->
			player.toEntity(generatedId, Uuid.random().toString())
		}

		pokerTableDao.updateCompleteTable(entity, playerEntities)
		return table.copy(id = generatedId, createdAt = createdAt)
	}

	override suspend fun updateTableInfo(table: PokerTable) {
		pokerTableDao.updateTableInfo(
			id = table.id,
			date = table.date,
			location = table.location,
			gameType = json.encodeToString(GameType.serializer(), table.gameType),
			maxPlayers = table.maxPlayers,
			playerCount = table.playerCount,
			heroSeat = table.heroSeat,
		)
		val maxSeat = table.maxPlayers.takeIf { it > 0 } ?: table.playerCount
		pokerTableDao.deletePlayersOverSeat(table.id, maxSeat)
	}

	@OptIn(ExperimentalUuidApi::class)
	override suspend fun upsertPlayer(tableId: String, player: Player) {
		if (player.id.isNotBlank()) {
			pokerTableDao.updatePlayer(
				id = player.id,
				seat = player.seat,
				tendency = player.tendency?.name,
				memo = player.memo,
				name = player.name,
			)
		} else {
			pokerTableDao.insertPlayer(
				player.toEntity(tableId, Uuid.random().toString()),
			)
		}
	}

	override suspend fun deletePlayer(tableId: String, seat: Int) {
		pokerTableDao.deletePlayerBySeat(tableId, seat)
	}

	override suspend fun deleteTable(tableId: String) {
		pokerTableDao.deleteCompleteTable(tableId)
	}
}

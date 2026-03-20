package com.hand.log.data.datasoure.local

import com.hand.log.database.dao.PokerTableDao
import com.hand.log.database.entity.PokerTableEntity
import com.hand.log.database.entity.TablePlayerEntity
import com.hand.log.domain.model.Blinds
import com.hand.log.domain.model.GameType
import com.hand.log.domain.model.Player
import com.hand.log.domain.model.PlayerTendency
import com.hand.log.domain.model.PokerTable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

internal class PokerTableLocalDataSourceImpl(
	private val pokerTableDao: PokerTableDao,
) : PokerTableLocalDataSource {

	override fun observeAllTables(): Flow<List<PokerTable>> {
		return pokerTableDao.observeAllTables().map { tables ->
			tables.map { table -> assembleTable(table) }
		}
	}

	override suspend fun getTableById(tableId: String): PokerTable? {
		val table = pokerTableDao.getTableById(tableId) ?: return null
		return assembleTable(table)
	}

	@OptIn(ExperimentalUuidApi::class)
	override suspend fun saveTable(table: PokerTable) {
		val entity = PokerTableEntity(
			id = table.id,
			date = table.date,
			location = table.location,
			gameType = table.gameType.name,
			startingStack = table.startingStack,
			blindsSb = table.blinds?.sb,
			blindsBb = table.blinds?.bb,
			blindsStraddle = table.blinds?.straddle,
			isBigBlindAnte = table.blinds?.isBigBlindAnte ?: false,
			playerCount = table.playerCount,
			heroSeat = table.heroSeat,
			createdAt = table.createdAt,
		)

		val playerEntities = table.players.map { player ->
			TablePlayerEntity(
				id = Uuid.random().toString(),
				tableId = table.id,
				seat = player.seat,
				stack = player.stack,
				tendency = player.tendency?.name,
				memo = player.memo,
				name = player.name,
			)
		}

		pokerTableDao.updateCompleteTable(entity, playerEntities)
	}

	override suspend fun updateTableInfo(table: PokerTable) {
		pokerTableDao.updateTableInfo(
			id = table.id,
			date = table.date,
			location = table.location,
			gameType = table.gameType.name,
			startingStack = table.startingStack,
			blindsSb = table.blinds?.sb,
			blindsBb = table.blinds?.bb,
			blindsStraddle = table.blinds?.straddle,
			isBigBlindAnte = table.blinds?.isBigBlindAnte ?: false,
			playerCount = table.playerCount,
			heroSeat = table.heroSeat,
		)
	}

	override suspend fun deleteTable(tableId: String) {
		pokerTableDao.deleteCompleteTable(tableId)
	}

	private suspend fun assembleTable(entity: PokerTableEntity): PokerTable {
		val players = pokerTableDao.getPlayersForTable(entity.id).map { p ->
			Player(
				seat = p.seat,
				stack = p.stack,
				tendency = p.tendency?.let { PlayerTendency.valueOf(it) },
				memo = p.memo,
				name = p.name,
			)
		}

		val sb = entity.blindsSb
		val bb = entity.blindsBb
		val blinds = if (sb != null && bb != null) {
			Blinds(
				sb = sb,
				bb = bb,
				straddle = entity.blindsStraddle,
				isBigBlindAnte = entity.isBigBlindAnte,
			)
		} else {
			null
		}

		return PokerTable(
			id = entity.id,
			date = entity.date,
			location = entity.location,
			gameType = GameType.valueOf(entity.gameType),
			startingStack = entity.startingStack,
			blinds = blinds,
			playerCount = entity.playerCount,
			heroSeat = entity.heroSeat,
			players = players,
			createdAt = entity.createdAt,
		)
	}
}

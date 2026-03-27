package com.hand.log.database.mapper

import com.hand.log.database.entity.PokerTableEntity
import com.hand.log.database.entity.TablePlayerEntity
import com.hand.log.domain.model.GameType
import com.hand.log.domain.model.Player
import com.hand.log.domain.model.PlayerTendency
import com.hand.log.domain.model.PokerTable
import kotlinx.serialization.json.Json

private val json = Json { ignoreUnknownKeys = true }

fun PokerTableEntity.toDomain(playerEntities: List<TablePlayerEntity>): PokerTable {
	val players = playerEntities.map { it.toDomain() }

	val gameTypeValue = try {
		json.decodeFromString(GameType.serializer(), gameType)
	} catch (_: Exception) {
		when (gameType) {
			"CASH" -> GameType.Cash(sb = 0.0, bb = 0.0)
			else -> GameType.Tournament()
		}
	}

	return PokerTable(
		id = id,
		date = date,
		location = location,
		gameType = gameTypeValue,
		maxPlayers = maxPlayers,
		playerCount = playerCount,
		heroSeat = heroSeat,
		players = players,
		createdAt = createdAt,
	)
}

fun TablePlayerEntity.toDomain(): Player = Player(
	id = id,
	seat = seat,
	tendency = tendency?.let { PlayerTendency.valueOf(it) },
	memo = memo,
	name = name,
)

fun Player.toEntity(tableId: String, generatedId: String): TablePlayerEntity = TablePlayerEntity(
	id = generatedId,
	tableId = tableId,
	seat = seat,
	tendency = tendency?.name,
	memo = memo,
	name = name,
)

fun PokerTable.toEntity(createdAt: Long): PokerTableEntity = PokerTableEntity(
	id = id,
	date = date,
	location = location,
	gameType = json.encodeToString(GameType.serializer(), gameType),
	maxPlayers = maxPlayers,
	playerCount = playerCount,
	heroSeat = heroSeat,
	createdAt = createdAt,
)

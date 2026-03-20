package com.hand.log.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.hand.log.database.entity.PokerTableEntity
import com.hand.log.database.entity.TablePlayerEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

@Dao
interface PokerTableDao {

	@Insert(onConflict = OnConflictStrategy.REPLACE)
	suspend fun insertTable(table: PokerTableEntity)

	@Query(
		"""UPDATE poker_tables SET
		date = :date, location = :location, gameType = :gameType,
		startingStack = :startingStack, blindsSb = :blindsSb, blindsBb = :blindsBb,
		blindsStraddle = :blindsStraddle, isBigBlindAnte = :isBigBlindAnte,
		playerCount = :playerCount, heroSeat = :heroSeat
		WHERE id = :id""",
	)
	suspend fun updateTableInfo(
		id: String,
		date: LocalDate,
		location: String?,
		gameType: String,
		startingStack: Double,
		blindsSb: Double?,
		blindsBb: Double?,
		blindsStraddle: Double?,
		isBigBlindAnte: Boolean,
		playerCount: Int,
		heroSeat: Int,
	)

	@Delete
	suspend fun deleteTable(table: PokerTableEntity)

	@Query("SELECT * FROM poker_tables ORDER BY createdAt DESC")
	fun observeAllTables(): Flow<List<PokerTableEntity>>

	@Query("SELECT * FROM poker_tables WHERE id = :tableId")
	suspend fun getTableById(tableId: String): PokerTableEntity?

	@Insert(onConflict = OnConflictStrategy.REPLACE)
	suspend fun insertPlayers(players: List<TablePlayerEntity>)

	@Query("SELECT * FROM table_players WHERE tableId = :tableId ORDER BY seat ASC")
	suspend fun getPlayersForTable(tableId: String): List<TablePlayerEntity>

	@Query("DELETE FROM table_players WHERE tableId = :tableId")
	suspend fun deletePlayersForTable(tableId: String)

	@Transaction
	suspend fun insertCompleteTable(
		table: PokerTableEntity,
		players: List<TablePlayerEntity>,
	) {
		insertTable(table)
		insertPlayers(players)
	}

	@Transaction
	suspend fun updateCompleteTable(
		table: PokerTableEntity,
		players: List<TablePlayerEntity>,
	) {
		insertTable(table)
		deletePlayersForTable(table.id)
		insertPlayers(players)
	}

	@Transaction
	suspend fun deleteCompleteTable(tableId: String) {
		val table = getTableById(tableId)
		table?.let { deleteTable(it) }
	}
}

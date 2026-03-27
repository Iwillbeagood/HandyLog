package com.hand.log.data.datasoure.local

import com.hand.log.domain.model.Player
import com.hand.log.domain.model.PokerTable
import kotlinx.coroutines.flow.Flow

interface PokerTableLocalDataSource {
	fun observeAllTables(): Flow<List<PokerTable>>
	fun observeTableById(tableId: String): Flow<PokerTable?>
	suspend fun getTableById(tableId: String): PokerTable?
	suspend fun saveTable(table: PokerTable): PokerTable
	suspend fun updateTableInfo(table: PokerTable)
	suspend fun upsertPlayer(tableId: String, player: Player)
	suspend fun deletePlayer(tableId: String, seat: Int)
	suspend fun deleteTable(tableId: String)
}

package com.hand.log.data.datasoure.local

import com.hand.log.domain.model.PokerTable
import kotlinx.coroutines.flow.Flow

interface PokerTableLocalDataSource {
	fun observeAllTables(): Flow<List<PokerTable>>
	suspend fun getTableById(tableId: String): PokerTable?
	suspend fun saveTable(table: PokerTable): PokerTable
	suspend fun updateTableInfo(table: PokerTable)
	suspend fun deleteTable(tableId: String)
}

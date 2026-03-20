package com.hand.log.domain.repository

import com.hand.log.domain.model.PokerTable
import kotlinx.coroutines.flow.Flow

interface PokerTableRepository {
	fun observeAllTables(): Flow<List<PokerTable>>
	suspend fun getTableById(tableId: String): PokerTable?
	suspend fun saveTable(table: PokerTable, onSuccess: () -> Unit = {})
	suspend fun updateTableInfo(table: PokerTable, onSuccess: () -> Unit = {})
	suspend fun deleteTable(tableId: String, onSuccess: () -> Unit = {})
}

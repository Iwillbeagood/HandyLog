package com.hand.log.data.repositoryImpl

import com.hand.log.data.datasoure.local.PokerTableLocalDataSource
import com.hand.log.domain.model.PokerTable
import com.hand.log.domain.repository.PokerTableRepository
import com.hand.log.utils.etc.Logger
import kotlinx.coroutines.flow.Flow

internal class PokerTableRepositoryImpl(
	private val localDataSource: PokerTableLocalDataSource,
) : PokerTableRepository {

	override fun observeAllTables(): Flow<List<PokerTable>> {
		return localDataSource.observeAllTables()
	}

	override suspend fun getTableById(tableId: String): PokerTable? {
		return localDataSource.getTableById(tableId)
	}

	override suspend fun saveTable(table: PokerTable, onSuccess: () -> Unit) {
		try {
			localDataSource.saveTable(table)
			onSuccess()
		} catch (e: Exception) {
			Logger.e("saveTable error: $e")
		}
	}

	override suspend fun updateTableInfo(table: PokerTable, onSuccess: () -> Unit) {
		try {
			localDataSource.updateTableInfo(table)
			onSuccess()
		} catch (e: Exception) {
			Logger.e("updateTableInfo error: $e")
		}
	}

	override suspend fun deleteTable(tableId: String, onSuccess: () -> Unit) {
		try {
			localDataSource.deleteTable(tableId)
			onSuccess()
		} catch (e: Exception) {
			Logger.e("deleteTable error: $e")
		}
	}
}

package com.hand.log.data.repositoryImpl

import com.hand.log.data.datasoure.local.PokerTableLocalDataSource
import com.hand.log.domain.model.Player
import com.hand.log.domain.model.PokerTable
import com.hand.log.domain.repository.PokerTableRepository
import com.hand.log.platform.etc.Logger
import kotlinx.coroutines.flow.Flow

internal class PokerTableRepositoryImpl(
	private val localDataSource: PokerTableLocalDataSource,
) : PokerTableRepository {

	override fun observeAllTables(): Flow<List<PokerTable>> =
		localDataSource.observeAllTables()

	override fun observeTableById(tableId: String): Flow<PokerTable?> =
		localDataSource.observeTableById(tableId)

	override suspend fun getTableById(tableId: String): PokerTable? =
		localDataSource.getTableById(tableId)

	override suspend fun saveTable(table: PokerTable): PokerTable {
		try {
			val saved = localDataSource.saveTable(table)
			Logger.d("TableRepo: saveTable success (id=${saved.id})")
			return saved
		} catch (e: Exception) {
			Logger.e("TableRepo: saveTable failed - ${e.message}")
			throw e
		}
	}

	override suspend fun updateTableInfo(table: PokerTable, onSuccess: () -> Unit) {
		try {
			localDataSource.updateTableInfo(table)
			Logger.d("TableRepo: updateTableInfo success (id=${table.id})")
			onSuccess()
		} catch (e: Exception) {
			Logger.e("TableRepo: updateTableInfo failed - ${e.message}")
		}
	}

	override suspend fun upsertPlayer(tableId: String, player: Player) {
		localDataSource.upsertPlayer(tableId, player)
	}

	override suspend fun deletePlayer(tableId: String, seat: Int) {
		localDataSource.deletePlayer(tableId, seat)
	}

	override suspend fun deleteTable(tableId: String, onSuccess: () -> Unit) {
		try {
			localDataSource.deleteTable(tableId)
			Logger.d("TableRepo: deleteTable success ($tableId)")
			onSuccess()
		} catch (e: Exception) {
			Logger.e("TableRepo: deleteTable failed - ${e.message}")
		}
	}
}

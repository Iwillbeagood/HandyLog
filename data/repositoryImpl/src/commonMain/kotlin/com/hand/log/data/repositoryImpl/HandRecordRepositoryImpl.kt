package com.hand.log.data.repositoryImpl

import com.hand.log.data.datasoure.local.HandRecordLocalDataSource
import com.hand.log.domain.model.HandRecord
import com.hand.log.domain.repository.HandRecordRepository
import com.hand.log.utils.etc.Logger
import kotlinx.coroutines.flow.Flow

internal class HandRecordRepositoryImpl(
	private val localDataSource: HandRecordLocalDataSource,
) : HandRecordRepository {

	override fun observeHandsByTableId(tableId: String): Flow<List<HandRecord>> {
		return localDataSource.observeHandsByTableId(tableId)
	}

	override fun observeHandById(handId: String): Flow<HandRecord?> {
		return localDataSource.observeHandById(handId)
	}

	override suspend fun getHandById(handId: String): HandRecord? {
		return localDataSource.getHandById(handId)
	}

	override suspend fun getHandCountByTableId(tableId: String): Int {
		return localDataSource.getHandCountByTableId(tableId)
	}

	override suspend fun saveHand(hand: HandRecord, onSuccess: () -> Unit) {
		try {
			localDataSource.saveHand(hand)
			onSuccess()
		} catch (e: Exception) {
			Logger.e("saveHand error: $e")
		}
	}

	override suspend fun deleteHand(handId: String, onSuccess: () -> Unit) {
		try {
			localDataSource.deleteHand(handId)
			onSuccess()
		} catch (e: Exception) {
			Logger.e("deleteHand error: $e")
		}
	}
}

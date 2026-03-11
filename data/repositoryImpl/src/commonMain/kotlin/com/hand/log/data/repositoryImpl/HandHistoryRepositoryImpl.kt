package com.hand.log.data.repositoryImpl

import com.hand.log.data.datasoure.local.HandHistoryLocalDataSource
import com.hand.log.domain.model.HandHistory
import com.hand.log.domain.repository.HandHistoryRepository
import com.hand.log.utils.etc.Logger
import kotlinx.coroutines.flow.Flow

internal class HandHistoryRepositoryImpl(
	private val handHistoryLocalDataSource: HandHistoryLocalDataSource,
) : HandHistoryRepository {

	override fun observeAllHands(): Flow<List<HandHistory>> {
		return handHistoryLocalDataSource.observeAllHands()
	}

	override fun observeHandsByDateRange(startDate: Long, endDate: Long): Flow<List<HandHistory>> {
		return handHistoryLocalDataSource.observeHandsByDateRange(startDate, endDate)
	}

	override suspend fun getHandById(handId: String): HandHistory? {
		return handHistoryLocalDataSource.getHandById(handId)
	}

	override suspend fun saveHandHistory(
		handHistory: HandHistory,
		onSuccess: () -> Unit,
	) {
		try {
			handHistoryLocalDataSource.saveHandHistory(handHistory)
			onSuccess()
		} catch (e: Exception) {
			Logger.e("saveHandHistory error: $e")
		}
	}

	override suspend fun deleteHandHistory(
		handId: String,
		onSuccess: () -> Unit,
	) {
		try {
			handHistoryLocalDataSource.deleteHandHistory(handId)
			onSuccess()
		} catch (e: Exception) {
			Logger.e("deleteHandHistory error: $e")
		}
	}

	override suspend fun getHandCount(): Int {
		return handHistoryLocalDataSource.getHandCount()
	}
}

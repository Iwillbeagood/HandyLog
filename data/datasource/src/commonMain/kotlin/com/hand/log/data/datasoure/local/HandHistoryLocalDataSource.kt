package com.hand.log.data.datasoure.local

import com.hand.log.domain.model.HandHistory
import kotlinx.coroutines.flow.Flow

interface HandHistoryLocalDataSource {
	fun observeAllHands(): Flow<List<HandHistory>>
	fun observeHandsByDateRange(startDate: Long, endDate: Long): Flow<List<HandHistory>>
	suspend fun getHandById(handId: String): HandHistory?
	suspend fun saveHandHistory(handHistory: HandHistory)
	suspend fun deleteHandHistory(handId: String)
	suspend fun getHandCount(): Int
}

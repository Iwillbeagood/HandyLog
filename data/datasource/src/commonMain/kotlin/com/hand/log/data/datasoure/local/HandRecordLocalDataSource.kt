package com.hand.log.data.datasoure.local

import com.hand.log.domain.model.HandRecord
import kotlinx.coroutines.flow.Flow

interface HandRecordLocalDataSource {
	fun observeHandsByTableId(tableId: String): Flow<List<HandRecord>>
	fun observeHandById(handId: String): Flow<HandRecord?>
	suspend fun getHandById(handId: String): HandRecord?
	suspend fun getHandCountByTableId(tableId: String): Int
	suspend fun saveHand(hand: HandRecord)
	suspend fun deleteHand(handId: String)
}

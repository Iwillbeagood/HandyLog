package com.hand.log.domain.repository

import com.hand.log.domain.model.HandRecord
import kotlinx.coroutines.flow.Flow

interface HandRecordRepository {
	fun observeHandsByTableId(tableId: String): Flow<List<HandRecord>>
	fun observeHandById(handId: String): Flow<HandRecord?>
	suspend fun getHandById(handId: String): HandRecord?
	suspend fun getHandCountByTableId(tableId: String): Int
	suspend fun saveHand(hand: HandRecord, onSuccess: () -> Unit = {})
	suspend fun deleteHand(handId: String, onSuccess: () -> Unit = {})
}

package com.hand.log.data.datasoure.local

import com.hand.log.database.dao.HandRecordDao
import com.hand.log.database.entity.HandRecordEntity
import com.hand.log.domain.model.HandRecord
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class HandRecordLocalDataSourceImpl(
	private val handRecordDao: HandRecordDao,
) : HandRecordLocalDataSource {

	override fun observeHandsByTableId(tableId: String): Flow<List<HandRecord>> {
		return handRecordDao.observeHandsByTableId(tableId).map { hands ->
			hands.map { it.toDomain() }
		}
	}

	override suspend fun getHandById(handId: String): HandRecord? {
		return handRecordDao.getHandById(handId)?.toDomain()
	}

	override suspend fun getHandCountByTableId(tableId: String): Int {
		return handRecordDao.getHandCountByTableId(tableId)
	}

	override suspend fun saveHand(hand: HandRecord) {
		handRecordDao.insertHand(hand.toEntity())
	}

	override suspend fun deleteHand(handId: String) {
		handRecordDao.deleteHandById(handId)
	}

	private fun HandRecord.toEntity(): HandRecordEntity = HandRecordEntity(
		id = id,
		tableId = tableId,
		createdAt = createdAt,
		blinds = blinds,
		heroHand = heroHand,
		heroStack = heroStack,
		buttonSeat = buttonSeat,
		streets = streets,
		result = result,
		memo = memo,
	)

	private fun HandRecordEntity.toDomain(): HandRecord = HandRecord(
		id = id,
		tableId = tableId,
		createdAt = createdAt,
		blinds = blinds,
		heroHand = heroHand,
		heroStack = heroStack,
		buttonSeat = buttonSeat,
		streets = streets,
		result = result,
		memo = memo,
	)
}

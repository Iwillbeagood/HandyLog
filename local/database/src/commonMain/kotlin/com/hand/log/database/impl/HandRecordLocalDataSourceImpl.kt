package com.hand.log.database.impl

import com.hand.log.data.datasoure.local.HandRecordLocalDataSource
import com.hand.log.database.dao.HandRecordDao
import com.hand.log.database.entity.HandRecordEntity
import com.hand.log.domain.model.HandRecord
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

internal class HandRecordLocalDataSourceImpl(
	private val handRecordDao: HandRecordDao,
) : HandRecordLocalDataSource {

	override fun observeHandsByTableId(tableId: String): Flow<List<HandRecord>> {
		return handRecordDao.observeHandsByTableId(tableId).map { hands ->
			hands.map { it.toDomain() }
		}
	}

	override fun observeAllHands(): Flow<List<HandRecord>> {
		return handRecordDao.observeAllHands().map { hands ->
			hands.map { it.toDomain() }
		}
	}

	override fun observeHandById(handId: String): Flow<HandRecord?> {
		return handRecordDao.observeHandById(handId).map { it?.toDomain() }
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

	@OptIn(ExperimentalTime::class)
	private fun HandRecord.toEntity(): HandRecordEntity = HandRecordEntity(
		id = id.ifBlank { generateId() },
		tableId = tableId,
		createdAt = if (createdAt == 0L) Clock.System.now().toEpochMilliseconds() else createdAt,
		blinds = blinds,
		heroSeat = heroSeat,
		buttonSeat = buttonSeat,
		streets = streets,
		players = players,
		result = result,
		resultLabel = resultLabel,
		memo = memo,
	)

	private fun generateId(): String {
		val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
		return (1..20).map { chars.random() }.joinToString("")
	}

	private fun HandRecordEntity.toDomain(): HandRecord = HandRecord(
		id = id,
		tableId = tableId,
		createdAt = createdAt,
		blinds = blinds,
		heroSeat = heroSeat,
		buttonSeat = buttonSeat,
		streets = streets,
		players = players,
		result = result,
		resultLabel = resultLabel,
		memo = memo,
	)
}

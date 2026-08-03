package com.hand.log.database.impl

import com.hand.log.data.datasoure.local.QuizRecordLocalDataSource
import com.hand.log.database.dao.QuizRecordDao
import com.hand.log.database.entity.QuizRecordEntity
import com.hand.log.domain.model.preflop.QuizRecord
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class QuizRecordLocalDataSourceImpl(
	private val quizRecordDao: QuizRecordDao,
) : QuizRecordLocalDataSource {

	override fun observeRecent(limit: Int): Flow<List<QuizRecord>> =
		quizRecordDao.observeRecent(limit).map { list -> list.map { it.toDomain() } }

	override suspend fun findById(id: String): QuizRecord? =
		quizRecordDao.findById(id)?.toDomain()

	override suspend fun save(record: QuizRecord) {
		quizRecordDao.insert(record.toEntity())
	}

	private fun QuizRecordEntity.toDomain() = QuizRecord(
		id = id,
		quizType = quizType,
		score = score,
		total = total,
		avgResponseMs = avgResponseMs,
		bestStreak = bestStreak,
		playedAt = playedAt,
		questions = questions,
	)

	private fun QuizRecord.toEntity() = QuizRecordEntity(
		id = id,
		quizType = quizType,
		score = score,
		total = total,
		avgResponseMs = avgResponseMs,
		bestStreak = bestStreak,
		playedAt = playedAt,
		questions = questions,
	)
}

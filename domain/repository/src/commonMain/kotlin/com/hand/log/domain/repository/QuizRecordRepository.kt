package com.hand.log.domain.repository

import com.hand.log.domain.model.preflop.QuizRecord
import kotlinx.coroutines.flow.Flow

interface QuizRecordRepository {
	fun observeRecent(limit: Int): Flow<List<QuizRecord>>
	suspend fun findById(id: String): QuizRecord?
	suspend fun save(record: QuizRecord)
}

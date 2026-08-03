package com.hand.log.data.datasoure.local

import com.hand.log.domain.model.preflop.QuizRecord
import kotlinx.coroutines.flow.Flow

interface QuizRecordLocalDataSource {
	fun observeRecent(limit: Int): Flow<List<QuizRecord>>
	suspend fun findById(id: String): QuizRecord?
	suspend fun save(record: QuizRecord)
}

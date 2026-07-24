package com.hand.log.data.repositoryImpl

import com.hand.log.data.datasoure.local.QuizRecordLocalDataSource
import com.hand.log.domain.model.preflop.QuizRecord
import com.hand.log.domain.repository.QuizRecordRepository
import kotlinx.coroutines.flow.Flow

internal class QuizRecordRepositoryImpl(
	private val localDataSource: QuizRecordLocalDataSource,
) : QuizRecordRepository {

	override fun observeRecent(limit: Int): Flow<List<QuizRecord>> =
		localDataSource.observeRecent(limit)

	override suspend fun save(record: QuizRecord) =
		localDataSource.save(record)
}

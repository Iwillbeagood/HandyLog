package com.hand.log.data.repositoryImpl

import com.hand.log.data.datasoure.local.HandRecordLocalDataSource
import com.hand.log.domain.model.HandRecord
import com.hand.log.domain.repository.HandRecordRepository
import com.hand.log.utils.etc.Logger
import com.hand.log.utils.poker.HandEvaluator
import kotlinx.coroutines.flow.Flow
import kotlin.math.abs

internal class HandRecordRepositoryImpl(
	private val localDataSource: HandRecordLocalDataSource,
) : HandRecordRepository {

	override fun observeHandsByTableId(tableId: String): Flow<List<HandRecord>> =
		localDataSource.observeHandsByTableId(tableId)

	override fun observeAllHands(): Flow<List<HandRecord>> =
		localDataSource.observeAllHands()

	override fun observeHandById(handId: String): Flow<HandRecord?> =
		localDataSource.observeHandById(handId)

	override suspend fun getHandById(handId: String): HandRecord? =
		localDataSource.getHandById(handId)

	override suspend fun getHandCountByTableId(tableId: String): Int =
		localDataSource.getHandCountByTableId(tableId)

	override suspend fun saveHand(hand: HandRecord, onSuccess: () -> Unit) {
		try {
			// 파생 결과(팟·최종 스택·승자·투입액)를 저장 시점에 한 번 계산해 materialize한다.
			// 입력의 단일 진실은 Action 로그이며 이 값은 그로부터의 캐시다 (ADR-0002).
			val withResults = hand.copy(results = hand.computeResults(HandEvaluator::calculateShowdown))
			verifyStackConservation(withResults)
			localDataSource.saveHand(withResults)
			Logger.d("HandRecordRepo: saveHand success (tableId=${hand.tableId})")
			onSuccess()
		} catch (e: Exception) {
			Logger.e("HandRecordRepo: saveHand failed - ${e.message}")
		}
	}

	/** 파생 결과 정합성 자가검증: 최종 스택 합 ≈ 초기 스택 합(팟 보존). 어긋나면 로깅만 하고 저장은 막지 않는다. */
	private fun verifyStackConservation(hand: HandRecord) {
		val results = hand.results ?: return
		val initialSum = hand.allSeats.sumOf { hand.getInitialStack(it) ?: 0.0 }
		val finalSum = results.finalStacks.values.sum()
		if (abs(initialSum - finalSum) > 0.01) {
			Logger.e(
				"HandRecordRepo: 스택 보존 불변식 위반 (handId=${hand.id}) " +
					"Σinitial=$initialSum Σfinal=$finalSum",
			)
		}
	}

	override suspend fun deleteHand(handId: String, onSuccess: () -> Unit) {
		try {
			localDataSource.deleteHand(handId)
			Logger.d("HandRecordRepo: deleteHand success ($handId)")
			onSuccess()
		} catch (e: Exception) {
			Logger.e("HandRecordRepo: deleteHand failed - ${e.message}")
		}
	}
}

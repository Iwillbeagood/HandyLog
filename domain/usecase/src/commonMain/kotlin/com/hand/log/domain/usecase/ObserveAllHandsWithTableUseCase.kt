package com.hand.log.domain.usecase

import com.hand.log.domain.model.HandWithTable
import com.hand.log.domain.repository.HandRecordRepository
import com.hand.log.domain.repository.PokerTableRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class ObserveAllHandsWithTableUseCase(
	private val handRecordRepository: HandRecordRepository,
	private val pokerTableRepository: PokerTableRepository,
) {

	operator fun invoke(): Flow<List<HandWithTable>> =
		combine(
			handRecordRepository.observeAllHands(),
			pokerTableRepository.observeAllTables(),
		) { hands, tables ->
			val tableMap = tables.associateBy { it.id }
			hands
				.sortedByDescending { it.createdAt }
				.mapNotNull { hand ->
					tableMap[hand.tableId]?.let { table ->
						HandWithTable(hand = hand, table = table)
					}
				}
		}
}

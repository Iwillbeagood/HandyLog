package com.hand.log.domain.usecase

import com.hand.log.domain.model.TableListItem
import com.hand.log.domain.repository.HandRecordRepository
import com.hand.log.domain.repository.PokerTableRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ObserveTableListItemsUseCase(
	private val pokerTableRepository: PokerTableRepository,
	private val handRecordRepository: HandRecordRepository,
) {

	operator fun invoke(): Flow<List<TableListItem>> =
		pokerTableRepository.observeAllTables().map { tables ->
			tables.map { table ->
				TableListItem(
					table = table,
					handCount = handRecordRepository.getHandCountByTableId(table.id),
				)
			}
		}
}

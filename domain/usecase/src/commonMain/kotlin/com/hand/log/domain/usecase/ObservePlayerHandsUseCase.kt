package com.hand.log.domain.usecase

import com.hand.log.domain.model.HandRecord
import com.hand.log.domain.repository.HandRecordRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ObservePlayerHandsUseCase(
	private val handRecordRepository: HandRecordRepository,
) {

	operator fun invoke(savedPlayerId: String): Flow<List<HandRecord>> =
		handRecordRepository.observeAllHands().map { allHands ->
			allHands.filter { it.containsPlayer(savedPlayerId) }
		}
}

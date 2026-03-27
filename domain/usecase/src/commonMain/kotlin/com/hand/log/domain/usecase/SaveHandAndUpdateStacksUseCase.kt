package com.hand.log.domain.usecase

import com.hand.log.domain.model.HandRecord
import com.hand.log.domain.repository.HandRecordRepository

class SaveHandAndUpdateStacksUseCase(
	private val handRecordRepository: HandRecordRepository,
) {

	suspend operator fun invoke(handRecord: HandRecord) {
		handRecordRepository.saveHand(handRecord)
	}
}

package com.hand.log.domain.usecase

import com.hand.log.domain.model.PokerTable
import com.hand.log.domain.repository.PokerTableRepository

class MarkPositionSetupShownUseCase(
	private val pokerTableRepository: PokerTableRepository,
) {

	suspend operator fun invoke(table: PokerTable) {
		if (!table.hasShownPositionSetup) {
			pokerTableRepository.updateTableInfo(table.copy(hasShownPositionSetup = true))
		}
	}
}

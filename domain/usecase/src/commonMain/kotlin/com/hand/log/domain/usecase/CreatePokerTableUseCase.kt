package com.hand.log.domain.usecase

import com.hand.log.domain.model.Player
import com.hand.log.domain.model.PokerTable
import com.hand.log.domain.repository.PokerTableRepository

class CreatePokerTableUseCase(
	private val pokerTableRepository: PokerTableRepository,
) {

	suspend operator fun invoke(table: PokerTable): PokerTable {
		val players = (1..table.playerCount).map { seat ->
			Player(seat = seat)
		}
		return pokerTableRepository.saveTable(table.copy(players = players))
	}
}

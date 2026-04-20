package com.hand.log.domain.usecase

import com.hand.log.domain.model.Player
import com.hand.log.domain.model.PokerTable
import com.hand.log.domain.repository.PokerTableRepository

class ApplyTableBalanceUseCase(
	private val pokerTableRepository: PokerTableRepository,
) {

	suspend operator fun invoke(table: PokerTable, heroSeat: Int, otherSeats: Set<Int>) {
		val currentSeats = table.players.map { it.seat }.toSet()
		currentSeats.forEach { seat ->
			pokerTableRepository.deletePlayer(table.id, seat)
		}
		pokerTableRepository.updateTableInfo(table.copy(heroSeat = heroSeat))
		otherSeats.forEach { seat ->
			pokerTableRepository.upsertPlayer(table.id, Player(seat = seat))
		}
	}
}

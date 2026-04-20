package com.hand.log.domain.usecase

import com.hand.log.domain.model.Player
import com.hand.log.domain.model.PokerTable
import com.hand.log.domain.repository.PokerTableRepository

class SavePlayerPositionsUseCase(
	private val pokerTableRepository: PokerTableRepository,
) {

	suspend operator fun invoke(table: PokerTable, selectedSeats: Set<Int>) {
		val currentSeats = table.players.map { it.seat }.toSet()
		val seatsToAdd = selectedSeats - currentSeats
		val seatsToRemove = currentSeats - selectedSeats - setOf(table.heroSeat)

		if (!table.hasShownPositionSetup) {
			pokerTableRepository.updateTableInfo(table.copy(hasShownPositionSetup = true))
		}
		seatsToRemove.forEach { seat ->
			pokerTableRepository.deletePlayer(table.id, seat)
		}
		seatsToAdd.forEach { seat ->
			pokerTableRepository.upsertPlayer(table.id, Player(seat = seat))
		}
	}
}

package com.hand.log.domain.usecase

import com.hand.log.domain.model.Player
import com.hand.log.domain.model.PokerTable
import com.hand.log.domain.repository.PokerTableRepository

class SavePlayerPositionsUseCase(
	private val pokerTableRepository: PokerTableRepository,
) {

	suspend operator fun invoke(table: PokerTable, selectedSeats: Set<Int>) {
		// 히어로 좌석을 반드시 포함
		val allSeats = selectedSeats + table.heroSeat
		val currentSeats = table.players.map { it.seat }.toSet()
		val seatsToAdd = allSeats - currentSeats
		val seatsToRemove = currentSeats - allSeats

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

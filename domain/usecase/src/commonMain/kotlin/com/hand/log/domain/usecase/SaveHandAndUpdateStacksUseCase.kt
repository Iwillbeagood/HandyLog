package com.hand.log.domain.usecase

import com.hand.log.domain.model.HandRecord
import com.hand.log.domain.model.Street
import com.hand.log.domain.repository.HandRecordRepository
import com.hand.log.domain.repository.PokerTableRepository

class SaveHandAndUpdateStacksUseCase(
	private val handRecordRepository: HandRecordRepository,
	private val pokerTableRepository: PokerTableRepository,
) {

	suspend operator fun invoke(handRecord: HandRecord) {
		handRecordRepository.saveHand(handRecord)

		val table = pokerTableRepository.getTableById(handRecord.tableId) ?: return

		val seatStacks = calculateFinalStacks(handRecord)

		val updatedPlayers = table.players.map { player ->
			val newStack = seatStacks[player.seat]
			if (newStack != null) player.copy(stack = newStack) else player
		}
		pokerTableRepository.updateTableInfo(table.copy(players = updatedPlayers))
	}

	private fun calculateFinalStacks(hand: HandRecord): Map<Int, Double> {
		val allActions = listOf(Street.PREFLOP, Street.FLOP, Street.TURN, Street.RIVER)
			.flatMap { hand.streets.getActions(it) }

		return allActions
			.groupBy { it.playerSeat }
			.mapValues { (_, actions) -> actions.last().stackAfter ?: actions.last().stackBefore ?: 0.0 }
	}
}

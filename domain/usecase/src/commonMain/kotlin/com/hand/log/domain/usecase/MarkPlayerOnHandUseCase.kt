package com.hand.log.domain.usecase

import com.hand.log.domain.model.Player
import com.hand.log.domain.model.SavedPlayer
import com.hand.log.domain.repository.HandRecordRepository
import com.hand.log.domain.repository.PokerTableRepository
import com.hand.log.domain.repository.SavedPlayerRepository

class MarkPlayerOnHandUseCase(
	private val savedPlayerRepository: SavedPlayerRepository,
	private val handRecordRepository: HandRecordRepository,
	private val pokerTableRepository: PokerTableRepository,
) {

	suspend operator fun invoke(player: SavedPlayer, handId: String, seat: Int) {
		savedPlayerRepository.savePlayer(player)
		val saved = savedPlayerRepository.getPlayerByName(player.name) ?: return
		val hand = handRecordRepository.getHandById(handId) ?: return

		// players 리스트 업데이트
		val updatedPlayers = hand.players.map { p ->
			if (p.seat == seat) p.copy(savedPlayerId = saved.id, playerName = saved.name) else p
		}

		// Action의 playerName/savedPlayerId도 업데이트 (하위 호환)
		val updatedPreflop = hand.streets.preflop.copy(
			actions = hand.streets.preflop.actions.map { action ->
				if (action.playerSeat == seat) {
					action.copy(savedPlayerId = saved.id, playerName = saved.name)
				} else {
					action
				}
			},
		)

		val updatedHand = hand.copy(
			players = updatedPlayers,
			streets = hand.streets.copy(preflop = updatedPreflop),
		)
		handRecordRepository.saveHand(updatedHand)
		pokerTableRepository.upsertPlayer(
			tableId = hand.tableId,
			player = Player(
				seat = seat,
				name = saved.name,
				savedPlayerId = saved.id,
				tendency = saved.tendency,
				memo = saved.memo,
			),
		)
	}
}

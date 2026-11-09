package com.hand.log.domain.usecase

import com.hand.log.domain.model.Player
import com.hand.log.domain.model.SavedPlayer
import com.hand.log.domain.repository.HandRecordRepository
import com.hand.log.domain.repository.PokerTableRepository

class MarkPlayerOnHandUseCase(
	private val getOrCreateSavedPlayer: GetOrCreateSavedPlayerUseCase,
	private val handRecordRepository: HandRecordRepository,
	private val pokerTableRepository: PokerTableRepository,
) {

	suspend operator fun invoke(player: SavedPlayer, handId: String, seat: Int): Result {
		val hand = handRecordRepository.getHandById(handId) ?: return Result.NotFound
		val saved = when (val result = getOrCreateSavedPlayer(player)) {
			is GetOrCreateSavedPlayerUseCase.Result.Found -> result.player
			GetOrCreateSavedPlayerUseCase.Result.LimitReached -> return Result.PlayerLimitReached
			GetOrCreateSavedPlayerUseCase.Result.Failed -> return Result.NotFound
		}

		val updatedPlayers = hand.players.map { p ->
			if (p.seat == seat) p.copy(savedPlayerId = saved.id, playerName = saved.name) else p
		}

		val updatedPreflop = hand.streets.preflop.copy(
			actions = hand.streets.preflop.actions.map { action ->
				if (action.playerSeat == seat) {
					action.copy(savedPlayerId = saved.id, playerName = saved.name)
				} else {
					action
				}
			},
		)

		handRecordRepository.saveHand(
			hand.copy(
				players = updatedPlayers,
				streets = hand.streets.copy(preflop = updatedPreflop),
			),
		)

		val tableId = hand.tableId ?: return Result.Marked
		val existingSeat = pokerTableRepository.getTableById(tableId)?.players?.find { it.seat == seat }
		pokerTableRepository.upsertPlayer(
			tableId = tableId,
			player = (existingSeat ?: Player(seat = seat)).copy(
				name = saved.name,
				savedPlayerId = saved.id,
				tendency = saved.tendency,
				memo = saved.memo,
			),
		)
		return Result.Marked
	}

	enum class Result { Marked, PlayerLimitReached, NotFound }
}

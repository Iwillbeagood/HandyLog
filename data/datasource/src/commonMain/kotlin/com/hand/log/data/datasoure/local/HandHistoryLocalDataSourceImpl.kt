package com.hand.log.data.datasoure.local

import com.hand.log.database.dao.HandHistoryDao
import com.hand.log.database.entity.HandActionEntity
import com.hand.log.database.entity.HandCommunityCardEntity
import com.hand.log.database.entity.HandEntity
import com.hand.log.database.entity.HandPlayerEntity
import com.hand.log.domain.model.Action
import com.hand.log.domain.model.ActionType
import com.hand.log.domain.model.Card
import com.hand.log.domain.model.HandHistory
import com.hand.log.domain.model.PlayerHand
import com.hand.log.domain.model.Position
import com.hand.log.domain.model.Rank
import com.hand.log.domain.model.Street
import com.hand.log.domain.model.Suit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

internal class HandHistoryLocalDataSourceImpl(
	private val handHistoryDao: HandHistoryDao,
) : HandHistoryLocalDataSource {

	override fun observeAllHands(): Flow<List<HandHistory>> {
		return handHistoryDao.getAllHands().map { hands ->
			hands.map { hand -> assembleHandHistory(hand) }
		}
	}

	override fun observeHandsByDateRange(startDate: Long, endDate: Long): Flow<List<HandHistory>> {
		return handHistoryDao.getHandsByDateRange(startDate, endDate).map { hands ->
			hands.map { hand -> assembleHandHistory(hand) }
		}
	}

	override suspend fun getHandById(handId: String): HandHistory? {
		val hand = handHistoryDao.getHandById(handId) ?: return null
		return assembleHandHistory(hand)
	}

	@OptIn(ExperimentalUuidApi::class)
	override suspend fun saveHandHistory(handHistory: HandHistory) {
		val handEntity = HandEntity(
			id = handHistory.id,
			date = handHistory.date,
			tableSize = handHistory.tableSize,
			heroPosition = handHistory.heroPosition?.name,
			potSize = handHistory.potSize,
			notes = handHistory.notes,
			createdAt = handHistory.createdAt,
		)

		val playerEntities = handHistory.players.map { player ->
			HandPlayerEntity(
				id = Uuid.random().toString(),
				handId = handHistory.id,
				position = player.position.name,
				card1Rank = player.card1?.rank?.name,
				card1Suit = player.card1?.suit?.name,
				card2Rank = player.card2?.rank?.name,
				card2Suit = player.card2?.suit?.name,
				result = player.result,
			)
		}

		val actionEntities = handHistory.actions.map { action ->
			HandActionEntity(
				id = Uuid.random().toString(),
				handId = handHistory.id,
				street = action.street.name,
				position = action.position.name,
				actionType = action.actionType.name,
				amount = action.amount,
				actionOrder = action.actionOrder,
			)
		}

		val communityCardEntities = handHistory.communityCards.mapIndexed { index, card ->
			HandCommunityCardEntity(
				id = Uuid.random().toString(),
				handId = handHistory.id,
				cardIndex = index,
				rank = card.rank.name,
				suit = card.suit.name,
			)
		}

		handHistoryDao.insertCompleteHand(
			hand = handEntity,
			players = playerEntities,
			actions = actionEntities,
			communityCards = communityCardEntities,
		)
	}

	override suspend fun deleteHandHistory(handId: String) {
		handHistoryDao.deleteCompleteHand(handId)
	}

	override suspend fun getHandCount(): Int {
		return handHistoryDao.getHandCount()
	}

	private suspend fun assembleHandHistory(hand: HandEntity): HandHistory {
		val players = handHistoryDao.getPlayersForHand(hand.id).map { entity ->
			PlayerHand(
				position = Position.valueOf(entity.position),
				card1 = buildCard(entity.card1Rank, entity.card1Suit),
				card2 = buildCard(entity.card2Rank, entity.card2Suit),
				result = entity.result,
			)
		}

		val actions = handHistoryDao.getActionsForHand(hand.id).map { entity ->
			Action(
				street = Street.valueOf(entity.street),
				position = Position.valueOf(entity.position),
				actionType = ActionType.valueOf(entity.actionType),
				amount = entity.amount,
				actionOrder = entity.actionOrder,
			)
		}

		val communityCards = handHistoryDao.getCommunityCardsForHand(hand.id).map { entity ->
			Card(
				rank = Rank.valueOf(entity.rank),
				suit = Suit.valueOf(entity.suit),
			)
		}

		return HandHistory(
			id = hand.id,
			date = hand.date,
			tableSize = hand.tableSize,
			heroPosition = hand.heroPosition?.let { Position.valueOf(it) },
			players = players,
			communityCards = communityCards,
			actions = actions,
			potSize = hand.potSize,
			notes = hand.notes,
			createdAt = hand.createdAt,
		)
	}

	private fun buildCard(rank: String?, suit: String?): Card? {
		if (rank == null || suit == null) return null
		return Card(
			rank = Rank.valueOf(rank),
			suit = Suit.valueOf(suit),
		)
	}
}

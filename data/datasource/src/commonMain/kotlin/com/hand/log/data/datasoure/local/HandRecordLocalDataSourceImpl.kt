package com.hand.log.data.datasoure.local

import com.hand.log.database.dao.HandRecordDao
import com.hand.log.database.entity.HandRecordEntity
import com.hand.log.database.entity.HandStreetActionEntity
import com.hand.log.database.entity.HandStreetCardEntity
import com.hand.log.domain.model.Action
import com.hand.log.domain.model.ActionType
import com.hand.log.domain.model.Blinds
import com.hand.log.domain.model.Card
import com.hand.log.domain.model.HandRecord
import com.hand.log.domain.model.Rank
import com.hand.log.domain.model.Street
import com.hand.log.domain.model.StreetData
import com.hand.log.domain.model.Suit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

internal class HandRecordLocalDataSourceImpl(
	private val handRecordDao: HandRecordDao,
) : HandRecordLocalDataSource {

	override fun observeHandsByTableId(tableId: String): Flow<List<HandRecord>> {
		return handRecordDao.observeHandsByTableId(tableId).map { hands ->
			hands.map { hand -> assembleHandRecord(hand) }
		}
	}

	override suspend fun getHandById(handId: String): HandRecord? {
		val hand = handRecordDao.getHandById(handId) ?: return null
		return assembleHandRecord(hand)
	}

	override suspend fun getHandCountByTableId(tableId: String): Int {
		return handRecordDao.getHandCountByTableId(tableId)
	}

	@OptIn(ExperimentalUuidApi::class)
	override suspend fun saveHand(hand: HandRecord) {
		val entity = HandRecordEntity(
			id = hand.id,
			tableId = hand.tableId,
			createdAt = hand.createdAt,
			blindsSb = hand.blinds?.sb,
			blindsBb = hand.blinds?.bb,
			heroCard1Rank = hand.heroCards.getOrNull(0)?.rank?.name,
			heroCard1Suit = hand.heroCards.getOrNull(0)?.suit?.name,
			heroCard2Rank = hand.heroCards.getOrNull(1)?.rank?.name,
			heroCard2Suit = hand.heroCards.getOrNull(1)?.suit?.name,
			heroStack = hand.heroStack,
			buttonSeat = hand.buttonSeat,
			result = hand.result,
			memo = hand.memo,
		)

		val streetCards = mutableListOf<HandStreetCardEntity>()
		val streetActions = mutableListOf<HandStreetActionEntity>()

		hand.streets.forEach { (street, data) ->
			data.cards.forEachIndexed { index, card ->
				streetCards.add(
					HandStreetCardEntity(
						id = Uuid.random().toString(),
						handId = hand.id,
						street = street.name,
						cardIndex = index,
						rank = card.rank.name,
						suit = card.suit.name,
					),
				)
			}
			data.actions.forEachIndexed { index, action ->
				streetActions.add(
					HandStreetActionEntity(
						id = Uuid.random().toString(),
						handId = hand.id,
						street = street.name,
						playerSeat = action.playerSeat,
						actionType = action.type.name,
						amount = action.amount,
						actionOrder = index,
					),
				)
			}
		}

		handRecordDao.insertCompleteHand(entity, streetCards, streetActions)
	}

	override suspend fun deleteHand(handId: String) {
		handRecordDao.deleteCompleteHand(handId)
	}

	private suspend fun assembleHandRecord(entity: HandRecordEntity): HandRecord {
		val streetCardEntities = handRecordDao.getStreetCardsForHand(entity.id)
		val streetActionEntities = handRecordDao.getStreetActionsForHand(entity.id)

		val streets = mutableMapOf<Street, StreetData>()

		val cardsByStreet = streetCardEntities.groupBy { it.street }
		val actionsByStreet = streetActionEntities.groupBy { it.street }

		val allStreets = (cardsByStreet.keys + actionsByStreet.keys).distinct()
		for (streetName in allStreets) {
			val street = Street.valueOf(streetName)
			val cards = cardsByStreet[streetName]?.sortedBy { it.cardIndex }?.map { e ->
				Card(rank = Rank.valueOf(e.rank), suit = Suit.valueOf(e.suit))
			} ?: emptyList()
			val actions = actionsByStreet[streetName]?.sortedBy { it.actionOrder }?.map { e ->
				Action(
					playerSeat = e.playerSeat,
					type = ActionType.valueOf(e.actionType),
					amount = e.amount,
				)
			} ?: emptyList()
			streets[street] = StreetData(cards = cards, actions = actions)
		}

		val heroCards = listOfNotNull(
			buildCard(entity.heroCard1Rank, entity.heroCard1Suit),
			buildCard(entity.heroCard2Rank, entity.heroCard2Suit),
		)

		val sb = entity.blindsSb
		val bb = entity.blindsBb
		val blinds = if (sb != null && bb != null) {
			Blinds(sb = sb, bb = bb)
		} else {
			null
		}

		return HandRecord(
			id = entity.id,
			tableId = entity.tableId,
			createdAt = entity.createdAt,
			blinds = blinds,
			heroCards = heroCards,
			heroStack = entity.heroStack,
			buttonSeat = entity.buttonSeat,
			streets = streets,
			result = entity.result,
			memo = entity.memo,
		)
	}

	private fun buildCard(rank: String?, suit: String?): Card? {
		if (rank == null || suit == null) return null
		return Card(rank = Rank.valueOf(rank), suit = Suit.valueOf(suit))
	}
}

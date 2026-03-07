package com.hand.log.database.model

import com.hand.log.database.entity.ActionEntity
import com.hand.log.database.entity.CardEntity
import com.hand.log.database.entity.HandHistoryEntity
import com.hand.log.database.entity.PlayerHandEntity
import com.hand.log.domain.model.ActionType
import com.hand.log.domain.model.Position
import com.hand.log.domain.model.Rank
import com.hand.log.domain.model.Street
import com.hand.log.domain.model.Suit
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Complete hand history with all related data
 * This matches the TypeScript interface structure
 */
@Serializable
data class CompleteHandHistory(
	val id: String,
	val date: Long, // timestamp
	val tableSize: Int,
	val heroPosition: Position? = null,
	val playerHands: List<PlayerHandData>,
	val communityCards: List<CardData>,
	val actions: List<ActionData>,
	val potSize: Int? = null,
	val notes: String? = null
)

@Serializable
data class PlayerHandData(
    val position: Position,
    val cards: List<CardData>?, // null or 2 cards
    val result: String? = null // 'win' | 'lose' | 'split'
)

@Serializable
data class ActionData(
	val position: Position,
	val type: ActionType,
	val amount: Int? = null,
	val street: Street
)

@Serializable
data class CardData(
	val rank: Rank,
	val suit: Suit
)

/**
 * Extension functions to convert between database entities and domain models
 */
fun CompleteHandHistory.toEntities(): Triple<HandHistoryEntity, List<PlayerHandEntity>, List<ActionEntity>> {
    val handHistory = HandHistoryEntity(
        id = id,
        date = date,
        tableSize = tableSize,
        heroPosition = heroPosition,
        communityCardsJson = Json.encodeToString(communityCards.map { it.toEntity() }),
        potSize = potSize,
        notes = notes
    )

    val playerHandEntities = playerHands.map { playerHandData ->
        PlayerHandEntity(
            handHistoryId = id,
            position = playerHandData.position,
            cardsJson = playerHandData.cards?.let { Json.encodeToString(it.map { card -> card.toEntity() }) },
            result = playerHandData.result
        )
    }

    val actionEntities = actions.mapIndexed { index, actionData ->
        ActionEntity(
            handHistoryId = id,
            actionIndex = index,
            position = actionData.position,
            type = actionData.type,
            amount = actionData.amount,
            street = actionData.street
        )
    }

    return Triple(handHistory, playerHandEntities, actionEntities)
}

fun HandHistoryEntity.toCompleteHandHistory(
    playerHands: List<PlayerHandEntity>,
    actions: List<ActionEntity>
): CompleteHandHistory {
    return CompleteHandHistory(
        id = id,
        date = date,
        tableSize = tableSize,
        heroPosition = heroPosition,
        playerHands = playerHands.map { it.toPlayerHandData() },
        communityCards = Json.decodeFromString<List<CardEntity>>(communityCardsJson).map { it.toData() },
        actions = actions.map { it.toActionData() },
        potSize = potSize,
        notes = notes
    )
}

fun PlayerHandEntity.toPlayerHandData(): PlayerHandData {
    return PlayerHandData(
        position = position,
        cards = cardsJson?.let { Json.decodeFromString<List<CardEntity>>(it).map { card -> card.toData() } },
        result = result
    )
}

fun ActionEntity.toActionData(): ActionData {
    return ActionData(
        position = position,
        type = type,
        amount = amount,
        street = street
    )
}

fun CardEntity.toData(): CardData {
    return CardData(
        rank = rank,
        suit = suit
    )
}

fun CardData.toEntity(): CardEntity {
    return CardEntity(
        rank = rank,
        suit = suit
    )
}

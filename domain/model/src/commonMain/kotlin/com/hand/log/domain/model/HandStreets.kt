package com.hand.log.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class PreflopStreet(
	val actions: List<Action> = emptyList(),
)

@Serializable
data class FlopStreet(
	val card1: Card? = null,
	val card2: Card? = null,
	val card3: Card? = null,
	val actions: List<Action> = emptyList(),
) {
	val cards: List<Card> get() = listOfNotNull(card1, card2, card3)
}

@Serializable
data class TurnStreet(
	val card: Card? = null,
	val actions: List<Action> = emptyList(),
) {
	val cards: List<Card> get() = listOfNotNull(card)
}

@Serializable
data class RiverStreet(
	val card: Card? = null,
	val actions: List<Action> = emptyList(),
) {
	val cards: List<Card> get() = listOfNotNull(card)
}

@Serializable
data class HandStreets(
	val preflop: PreflopStreet = PreflopStreet(),
	val flop: FlopStreet? = null,
	val turn: TurnStreet? = null,
	val river: RiverStreet? = null,
) {
	val boardCards: List<Card>
		get() = listOfNotNull(
			flop?.card1,
			flop?.card2,
			flop?.card3,
			turn?.card,
			river?.card,
		)

	fun getActions(street: Street): List<Action> = when (street) {
		Street.PREFLOP -> preflop.actions
		Street.FLOP -> flop?.actions ?: emptyList()
		Street.TURN -> turn?.actions ?: emptyList()
		Street.RIVER -> river?.actions ?: emptyList()
	}

	fun getCards(street: Street): List<Card> = when (street) {
		Street.PREFLOP -> emptyList()
		Street.FLOP -> flop?.cards ?: emptyList()
		Street.TURN -> turn?.cards ?: emptyList()
		Street.RIVER -> river?.cards ?: emptyList()
	}

	fun isBoardReady(street: Street): Boolean = when (street) {
		Street.PREFLOP -> true
		Street.FLOP -> flop?.cards?.size == 3
		Street.TURN -> turn?.card != null
		Street.RIVER -> river?.card != null
	}

	fun ensureStreet(street: Street): HandStreets = when (street) {
		Street.PREFLOP -> this
		Street.FLOP -> if (flop != null) this else copy(flop = FlopStreet())
		Street.TURN -> if (turn != null) this else copy(turn = TurnStreet())
		Street.RIVER -> if (river != null) this else copy(river = RiverStreet())
	}

	fun addAction(street: Street, action: Action): HandStreets = when (street) {
		Street.PREFLOP -> copy(preflop = preflop.copy(actions = preflop.actions + action))
		Street.FLOP -> copy(flop = (flop ?: FlopStreet()).let { it.copy(actions = it.actions + action) })
		Street.TURN -> copy(turn = (turn ?: TurnStreet()).let { it.copy(actions = it.actions + action) })
		Street.RIVER -> copy(
			river = (river ?: RiverStreet()).let {
				it.copy(actions = it.actions + action)
			},
		)
	}

	fun removeLastAction(street: Street): HandStreets = when (street) {
		Street.PREFLOP -> copy(preflop = preflop.copy(actions = preflop.actions.dropLast(1)))
		Street.FLOP -> copy(flop = flop?.copy(actions = flop.actions.dropLast(1)))
		Street.TURN -> copy(turn = turn?.copy(actions = turn.actions.dropLast(1)))
		Street.RIVER -> copy(river = river?.copy(actions = river.actions.dropLast(1)))
	}

	fun setCards(street: Street, cards: List<Card>): HandStreets = when (street) {
		Street.PREFLOP -> this
		Street.FLOP -> copy(
			flop = (flop ?: FlopStreet()).copy(
				card1 = cards.getOrNull(0),
				card2 = cards.getOrNull(1),
				card3 = cards.getOrNull(2),
			),
		)
		Street.TURN -> copy(turn = (turn ?: TurnStreet()).copy(card = cards.firstOrNull()))
		Street.RIVER -> copy(river = (river ?: RiverStreet()).copy(card = cards.firstOrNull()))
	}

	/** 모든 스트릿의 액션만 제거하고 보드 카드는 보존 */
	fun clearActions(): HandStreets = copy(
		preflop = preflop.copy(actions = emptyList()),
		flop = flop?.copy(actions = emptyList()),
		turn = turn?.copy(actions = emptyList()),
		river = river?.copy(actions = emptyList()),
	)

	/** 해당 스트릿 이후의 모든 스트릿 데이터 제거 */
	fun clearAfter(street: Street): HandStreets = when (street) {
		Street.PREFLOP -> copy(flop = null, turn = null, river = null)
		Street.FLOP -> copy(turn = null, river = null)
		Street.TURN -> copy(river = null)
		Street.RIVER -> this
	}

	/** 해당 스트릿 이후 스트릿의 액션만 제거하고 보드 카드는 보존 */
	fun clearActionsAfter(street: Street): HandStreets = when (street) {
		Street.PREFLOP -> copy(
			flop = flop?.copy(actions = emptyList()),
			turn = turn?.copy(actions = emptyList()),
			river = river?.copy(actions = emptyList()),
		)
		Street.FLOP -> copy(
			turn = turn?.copy(actions = emptyList()),
			river = river?.copy(actions = emptyList()),
		)
		Street.TURN -> copy(river = river?.copy(actions = emptyList()))
		Street.RIVER -> this
	}

	fun totalActionAmount(): Double {
		fun streetTotal(actions: List<Action>): Double {
			return actions
				.groupBy { it.playerSeat }
				.values
				.sumOf { seatActions -> seatActions.last().amount ?: 0.0 }
		}
		return streetTotal(preflop.actions) +
			(flop?.actions?.let { streetTotal(it) } ?: 0.0) +
			(turn?.actions?.let { streetTotal(it) } ?: 0.0) +
			(river?.actions?.let { streetTotal(it) } ?: 0.0)
	}
}

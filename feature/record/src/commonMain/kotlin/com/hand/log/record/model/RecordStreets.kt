package com.hand.log.record.model

import androidx.compose.runtime.Immutable
import com.hand.log.domain.model.Action
import com.hand.log.domain.model.Card
import com.hand.log.domain.model.FlopStreet
import com.hand.log.domain.model.HandStreets
import com.hand.log.domain.model.PreflopStreet
import com.hand.log.domain.model.RiverStreet
import com.hand.log.domain.model.Street
import com.hand.log.domain.model.TurnStreet

@Immutable
data class RecordStreets(
	val preflop: PreflopStreet = PreflopStreet(),
	val flop: FlopStreet? = null,
	val turn: TurnStreet? = null,
	val river: RiverStreet? = null,
) {
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

	fun ensureStreet(street: Street): RecordStreets = when (street) {
		Street.PREFLOP -> this
		Street.FLOP -> if (flop != null) this else copy(flop = FlopStreet())
		Street.TURN -> if (turn != null) this else copy(turn = TurnStreet())
		Street.RIVER -> if (river != null) this else copy(river = RiverStreet())
	}

	fun addAction(street: Street, action: Action): RecordStreets = when (street) {
		Street.PREFLOP -> copy(preflop = preflop.copy(actions = preflop.actions + action))
		Street.FLOP -> copy(flop = (flop ?: FlopStreet()).let { it.copy(actions = it.actions + action) })
		Street.TURN -> copy(turn = (turn ?: TurnStreet()).let { it.copy(actions = it.actions + action) })
		Street.RIVER -> copy(
			river = (river ?: RiverStreet()).let {
				it.copy(actions = it.actions + action)
			},
		)
	}

	fun removeLastAction(street: Street): RecordStreets = when (street) {
		Street.PREFLOP -> copy(preflop = preflop.copy(actions = preflop.actions.dropLast(1)))
		Street.FLOP -> copy(flop = flop?.copy(actions = flop.actions.dropLast(1)))
		Street.TURN -> copy(turn = turn?.copy(actions = turn.actions.dropLast(1)))
		Street.RIVER -> copy(river = river?.copy(actions = river.actions.dropLast(1)))
	}

	fun setCards(street: Street, cards: List<Card>): RecordStreets = when (street) {
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

	fun totalActionAmount(): Double {
		return preflop.actions.sumOf { it.amount ?: 0.0 } +
			(flop?.actions?.sumOf { it.amount ?: 0.0 } ?: 0.0) +
			(turn?.actions?.sumOf { it.amount ?: 0.0 } ?: 0.0) +
			(river?.actions?.sumOf { it.amount ?: 0.0 } ?: 0.0)
	}

	fun toHandStreets(): HandStreets = HandStreets(
		preflop = preflop,
		flop = flop,
		turn = turn,
		river = river,
	)
}

package com.hand.log.handdetail.model

import com.hand.log.domain.model.ActionType
import com.hand.log.domain.model.Card
import com.hand.log.domain.model.HandRecord
import com.hand.log.domain.model.Position
import com.hand.log.domain.model.Street
import com.hand.log.domain.model.Suit

object HandHistoryFormatter {

	fun format(hand: HandRecord): String = buildString {
		val bb = hand.blinds?.bb ?: 1.0
		val heroSeat = hand.heroSeat
		val playerCount = hand.streets.preflop.actions
			.map { it.playerSeat }.distinct().size.coerceAtLeast(2)
		val buttonSeat = hand.buttonSeat

		// --- Stacks ---
		appendLine("[Stacks]")
		val allSeats = hand.streets.preflop.actions.map { it.playerSeat }.distinct().sorted()
		allSeats.forEach { seat ->
			val posName = getPositionName(seat, buttonSeat, playerCount)
			val isHero = seat == heroSeat
			val stack = hand.streets.preflop.actions.firstOrNull { it.playerSeat == seat }?.stackBefore
			if (stack != null) {
				val bbStack = formatBb(stack, bb)
				if (isHero) {
					appendLine("Hero ($posName) - $bbStack")
				} else {
					appendLine("$posName - $bbStack")
				}
			}
		}
		appendLine()

		// --- Preflop ---
		appendLine("[Preflop]")
		val preflopActions = hand.streets.preflop.actions
		var foldCount = 0
		val foldPositions = mutableListOf<String>()

		preflopActions.forEach { action ->
			val posName = getPositionName(action.playerSeat, buttonSeat, playerCount)
			val isHero = action.playerSeat == heroSeat
			val heroCards = if (isHero) {
				hand.heroHand?.let {
					" with ${formatCard(it.card1)}${formatCard(it.card2)}"
				} ?: ""
			} else {
				""
			}
			val prefix = if (isHero) "Hero ($posName)" else posName

			when (action.type) {
				ActionType.FOLD -> {
					foldCount++
					foldPositions.add(posName)
				}
				ActionType.CALL -> appendLine("$prefix calls${formatAmountBb(action.amount, bb)}")
				ActionType.RAISE -> appendLine(
					"$prefix opens to ${formatBb(action.amount ?: 0.0, bb)}$heroCards",
				)
				ActionType.BET -> appendLine("$prefix bets ${formatBb(action.amount ?: 0.0, bb)}$heroCards")
				ActionType.ALL_IN -> appendLine(
					"$prefix goes all-in ${formatBb(action.amount ?: 0.0, bb)}$heroCards",
				)
				ActionType.CHECK -> appendLine("$prefix checks")
			}
		}
		if (foldCount > 0) {
			appendLine("${foldPositions.joinToString(", ")} fold${if (foldCount > 1) "" else "s"}")
		}

		val activePlayers = preflopActions.map { it.playerSeat }.distinct().count() - foldCount
		val preflopPot = calculatePot(hand, Street.PREFLOP, bb)
		appendLine()
		appendLine(
			"→ Pot: ${formatBb(
				preflopPot,
				bb,
			)} (${if (activePlayers > 2) "$activePlayers-way" else "Heads-up"})",
		)
		appendLine()

		// --- Flop ---
		hand.streets.flop?.let { flop ->
			appendLine("[Flop] ${flop.cards.joinToString(" ") { formatCard(it) }}")
			formatStreetActions(flop.actions, heroSeat, buttonSeat, playerCount, bb, this)
			val flopPot = calculatePot(hand, Street.FLOP, bb)
			val flopActive = countActive(hand, Street.FLOP)
			appendLine(
				"→ Pot: ${formatBb(flopPot, bb)} (${if (flopActive > 2) "$flopActive-way" else "Heads-up"})",
			)
			appendLine()
		}

		// --- Turn ---
		hand.streets.turn?.let { turn ->
			appendLine("[Turn] ${turn.cards.joinToString(" ") { formatCard(it) }}")
			formatStreetActions(turn.actions, heroSeat, buttonSeat, playerCount, bb, this)
			val turnPot = calculatePot(hand, Street.TURN, bb)
			appendLine("→ Pot: ${formatBb(turnPot, bb)}")
			appendLine()
		}

		// --- River ---
		hand.streets.river?.let { river ->
			if (river.actions.isNotEmpty()) {
				appendLine("[River] ${river.cards.joinToString(" ") { formatCard(it) }}")
				formatStreetActions(river.actions, heroSeat, buttonSeat, playerCount, bb, this)
				appendLine()
			}
		}

		// --- Result ---
		hand.result?.let { result ->
			val isPositive = result >= 0
			appendLine("[Result] ${if (isPositive) "+" else ""}${formatBb(result, bb)}")
		}
		hand.memo?.let { memo ->
			appendLine(memo)
		}
	}

	private fun formatStreetActions(
		actions: List<com.hand.log.domain.model.Action>,
		heroSeat: Int,
		buttonSeat: Int,
		playerCount: Int,
		bb: Double,
		sb: StringBuilder,
	) {
		val foldPositions = mutableListOf<String>()

		actions.forEach { action ->
			val posName = getPositionName(action.playerSeat, buttonSeat, playerCount)
			val isHero = action.playerSeat == heroSeat
			val prefix = if (isHero) "Hero" else posName

			when (action.type) {
				ActionType.FOLD -> foldPositions.add(prefix)
				ActionType.CHECK -> sb.appendLine("$prefix checks")
				ActionType.CALL -> sb.appendLine("$prefix calls")
				ActionType.BET -> sb.appendLine("$prefix bets ${formatBb(action.amount ?: 0.0, bb)}")
				ActionType.RAISE -> sb.appendLine("$prefix raises to ${formatBb(action.amount ?: 0.0, bb)}")
				ActionType.ALL_IN -> sb.appendLine(
					"$prefix goes all-in (${formatBb(action.amount ?: 0.0, bb)})",
				)
			}
		}
		if (foldPositions.isNotEmpty()) {
			sb.appendLine(
				"${foldPositions.joinToString(", ")} fold${if (foldPositions.size > 1) "" else "s"}",
			)
		}
	}

	private fun formatBb(amount: Double, bb: Double): String {
		if (bb <= 0) return "${amount.toLong()}"
		val bbCount = (amount * 10 / bb).toLong() / 10.0
		return if (bbCount == bbCount.toLong().toDouble()) {
			"${bbCount.toLong()}BB"
		} else {
			"${bbCount}BB"
		}
	}

	private fun formatAmountBb(amount: Double?, bb: Double): String {
		if (amount == null) return ""
		return " ${formatBb(amount, bb)}"
	}

	private fun formatCard(card: Card): String {
		val suit = when (card.suit) {
			Suit.SPADES -> "♠"
			Suit.HEARTS -> "♥"
			Suit.DIAMONDS -> "♦"
			Suit.CLUBS -> "♣"
		}
		return "${card.rank.symbol}$suit"
	}

	private fun calculatePot(hand: HandRecord, upToStreet: Street, bb: Double): Double {
		val blinds = hand.blinds
		val blindsPot = (blinds?.sb ?: 0.0) + (blinds?.bb ?: 0.0)
		val antePot = if (blinds?.isBigBlindAnte == true) blinds.bb else 0.0
		var pot = blindsPot + antePot

		val streets = listOf(Street.PREFLOP, Street.FLOP, Street.TURN, Street.RIVER)
		for (s in streets) {
			pot += hand.streets.getActions(s).sumOf { it.amount ?: 0.0 }
			if (s == upToStreet) break
		}
		return pot
	}

	private fun countActive(hand: HandRecord, upToStreet: Street): Int {
		val allSeats = hand.streets.preflop.actions.map { it.playerSeat }.distinct().toMutableSet()
		val streets = listOf(Street.PREFLOP, Street.FLOP, Street.TURN, Street.RIVER)
		for (s in streets) {
			hand.streets.getActions(s).forEach { action ->
				if (action.type == ActionType.FOLD) allSeats.remove(action.playerSeat)
			}
			if (s == upToStreet) break
		}
		return allSeats.size
	}

	private fun getPositionName(seat: Int, buttonSeat: Int, count: Int): String {
		val btn = buttonSeat
		val sbSeat = (btn % count) + 1
		val bbSeat = ((btn + 1) % count) + 1

		if (seat == btn) return Position.BTN.label
		if (seat == sbSeat) return Position.SB.label
		if (seat == bbSeat) return Position.BB.label

		val preflopOrder = (1..count).map { offset -> ((btn + 2 + offset - 1) % count) + 1 }
		val utgOrder = preflopOrder.filter { it != btn && it != sbSeat && it != bbSeat }
		val idx = utgOrder.indexOf(seat)

		return when {
			idx == 0 -> Position.UTG.label
			idx == utgOrder.lastIndex -> Position.CO.label
			count <= 6 -> Position.MP.label
			idx == utgOrder.lastIndex - 1 -> Position.HJ.label
			idx == utgOrder.lastIndex - 2 -> Position.LJ.label
			idx == 1 -> Position.UTG1.label
			else -> Position.MP.label
		}
	}
}

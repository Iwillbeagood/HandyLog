package com.hand.log.handdetail.model

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hand.log.designsystem.etc.ThemePreview
import com.hand.log.designsystem.etc.ThemePreviews
import com.hand.log.designsystem.theme.HandyTheme
import com.hand.log.domain.model.Action
import com.hand.log.domain.model.ActionType
import com.hand.log.domain.model.Blinds
import com.hand.log.domain.model.Card
import com.hand.log.domain.model.FlopStreet
import com.hand.log.domain.model.HandRecord
import com.hand.log.domain.model.HandStreets
import com.hand.log.domain.model.PocketCards
import com.hand.log.domain.model.Position
import com.hand.log.domain.model.PreflopStreet
import com.hand.log.domain.model.Rank
import com.hand.log.domain.model.RiverStreet
import com.hand.log.domain.model.Street
import com.hand.log.domain.model.Suit
import com.hand.log.domain.model.TurnStreet

object HandHistoryFormatter {

	fun format(hand: HandRecord): String = buildString {
		val bb = hand.blinds?.bb ?: 1.0
		val heroSeat = hand.heroSeat
		val playerCount = hand.playerCount
		val buttonSeat = hand.buttonSeat
		val preflopFoldedSeats = hand.preflopFoldedSeats

		// --- Stacks (프리플랍 폴드 제외) ---
		appendLine("[Stacks]")
		val allSeats = hand.allSeats
		allSeats.forEach { seat ->
			if (seat in preflopFoldedSeats) return@forEach
			val posName = getPositionName(seat, buttonSeat, playerCount)
			val isHero = seat == heroSeat
			val stack = hand.streets.preflop.actions.firstOrNull { it.playerSeat == seat }?.stackBefore
			if (stack != null) {
				val bbStack = formatBb(stack, bb)
				val heroCards = if (isHero) {
					hand.heroHand?.let { " ${formatCard(it.card1)}${formatCard(it.card2)}" } ?: ""
				} else {
					""
				}
				if (isHero) {
					appendLine("Hero ($posName) - $bbStack$heroCards")
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
		var preflopMaxBet = bb // BB가 기본 베팅

		preflopActions.forEach { action ->
			if (action.type == ActionType.FOLD) {
				foldCount++
				return@forEach
			}
			val posName = getPositionName(action.playerSeat, buttonSeat, playerCount)
			val isHero = action.playerSeat == heroSeat
			val prefix = if (isHero) "Hero ($posName)" else posName
			val amount = action.amount ?: 0.0

			when (action.type) {
				ActionType.CALL -> appendLine("$prefix calls ${formatBb(amount, bb)}")
				ActionType.RAISE -> {
					val label = formatRaiseLabel(action.betLevel)
					appendLine("$prefix $label ${formatBb(amount, bb)}")
					preflopMaxBet = amount
				}
				ActionType.BET -> {
					appendLine("$prefix bets ${formatBb(amount, bb)}")
					preflopMaxBet = amount
				}
				ActionType.ALL_IN -> {
					if (amount > preflopMaxBet) {
						appendLine("$prefix bets ${formatBb(amount, bb)} (all-in)")
						preflopMaxBet = amount
					} else {
						appendLine("$prefix calls all-in for ${formatBb(amount, bb)}")
					}
				}
				ActionType.CHECK -> appendLine("$prefix checks")
				else -> {}
			}
		}

		val activePlayers = preflopActions.map { it.playerSeat }.distinct().count() - foldCount
		val preflopPot = calculatePot(hand, Street.PREFLOP)
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
			val flopPot = calculatePot(hand, Street.FLOP)
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
			val turnPot = calculatePot(hand, Street.TURN)
			appendLine("→ Pot: ${formatBb(turnPot, bb)}")
			appendLine()
		}

		// --- River ---
		hand.streets.river?.let { river ->
			appendLine("[River] ${river.cards.joinToString(" ") { formatCard(it) }}")
			if (river.actions.isNotEmpty()) {
				formatStreetActions(river.actions, heroSeat, buttonSeat, playerCount, bb, this)
			}
			val riverPot = calculatePot(hand, Street.RIVER)
			appendLine("→ Pot: ${formatBb(riverPot, bb)}")
			appendLine()
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
		actions: List<Action>,
		heroSeat: Int,
		buttonSeat: Int,
		playerCount: Int,
		bb: Double,
		sb: StringBuilder,
	) {
		val foldPositions = mutableListOf<String>()
		var currentMaxBet = 0.0

		actions.forEach { action ->
			val posName = getPositionName(action.playerSeat, buttonSeat, playerCount)
			val isHero = action.playerSeat == heroSeat
			val prefix = if (isHero) "Hero ($posName)" else posName
			val amount = action.amount ?: 0.0

			when (action.type) {
				ActionType.FOLD -> foldPositions.add(prefix)
				ActionType.CHECK -> sb.appendLine("$prefix checks")
				ActionType.CALL -> sb.appendLine("$prefix calls ${formatBb(amount, bb)}")
				ActionType.BET -> {
					sb.appendLine("$prefix bets ${formatBb(amount, bb)}")
					currentMaxBet = amount
				}
				ActionType.RAISE -> {
					val label = formatRaiseLabel(action.betLevel)
					sb.appendLine("$prefix $label ${formatBb(amount, bb)}")
					currentMaxBet = amount
				}
				ActionType.ALL_IN -> {
					if (amount > currentMaxBet) {
						// 공격적 올인 (베팅/레이즈)
						sb.appendLine("$prefix bets ${formatBb(amount, bb)} (all-in)")
						currentMaxBet = amount
					} else {
						// 콜 올인 (상대 금액 이하)
						sb.appendLine("$prefix calls all-in for ${formatBb(amount, bb)}")
					}
				}
			}
		}
		if (foldPositions.isNotEmpty()) {
			sb.appendLine(
				"${foldPositions.joinToString(", ")} fold${if (foldPositions.size > 1) "" else "s"}",
			)
		}
	}

	/**
	 * betLevel 기반 레이즈 라벨:
	 * 프리플랍: betLevel 2 = opens, 3 = 3-bets, 4 = 4-bets ...
	 * 포스트플랍: betLevel 2 = raises to, 3 = 3-bets ...
	 */
	private fun formatRaiseLabel(betLevel: Int): String = when (betLevel) {
		2 -> "opens to"
		3 -> "3-bets to"
		4 -> "4-bets to"
		5 -> "5-bets to"
		else -> "raises to"
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

	private fun formatCard(card: Card): String {
		val suit = when (card.suit) {
			Suit.SPADES -> "♠"
			Suit.HEARTS -> "♥"
			Suit.DIAMONDS -> "♦"
			Suit.CLUBS -> "♣"
		}
		return "${card.rank.symbol}$suit"
	}

	private fun calculatePot(hand: HandRecord, upToStreet: Street): Double {
		val blinds = hand.blinds
		val sb = blinds?.sb ?: 0.0
		val bb = blinds?.bb ?: 0.0
		val antePot = if (blinds?.isBigBlindAnte == true) bb else 0.0
		var pot = antePot

		val streets = listOf(Street.PREFLOP, Street.FLOP, Street.TURN, Street.RIVER)
		for (s in streets) {
			val actions = hand.streets.getActions(s)
			// 각 좌석의 마지막 투입 금액 (프리플랍 amount는 블라인드 포함)
			val seatAmounts = actions
				.groupBy { it.playerSeat }
				.mapValues { (_, acts) -> acts.last().amount ?: 0.0 }

			if (s == Street.PREFLOP) {
				// 프리플랍: 액션에 참여한 좌석의 amount에는 블라인드가 포함됨
				// 폴드한 블라인드 플레이어의 블라인드는 별도로 추가
				val playerCount = hand.playerCount
				val btn = hand.buttonSeat
				val sbSeat = (btn % playerCount) + 1
				val bbSeat = ((btn + 1) % playerCount) + 1
				val foldedSeats = actions.filter { it.type == ActionType.FOLD }.map { it.playerSeat }.toSet()

				// SB가 폴드한 경우 SB 블라인드만 팟에 추가
				if (sbSeat in foldedSeats && sbSeat !in seatAmounts) {
					pot += sb
				} else if (sbSeat in foldedSeats && (seatAmounts[sbSeat] ?: 0.0) == 0.0) {
					pot += sb
				}

				val amounts = seatAmounts.values.sortedDescending()
				if (amounts.size >= 2) {
					val uncalledBet = amounts[0] - amounts[1]
					pot += amounts.sum() - uncalledBet
				} else {
					pot += amounts.sum()
				}
			} else {
				val amounts = seatAmounts.values.sortedDescending()
				if (amounts.size >= 2) {
					val uncalledBet = amounts[0] - amounts[1]
					pot += amounts.sum() - uncalledBet
				} else {
					pot += amounts.sum()
				}
			}
			if (s == upToStreet) break
		}
		return pot
	}

	private fun countActive(hand: HandRecord, upToStreet: Street): Int {
		val seats = hand.allSeats.toMutableSet()
		val streets = listOf(Street.PREFLOP, Street.FLOP, Street.TURN, Street.RIVER)
		for (s in streets) {
			hand.streets.getActions(s).forEach { action ->
				if (action.type == ActionType.FOLD) seats.remove(action.playerSeat)
			}
			if (s == upToStreet) break
		}
		return seats.size
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

@ThemePreviews
@Composable
private fun HandHistoryFormatterPreview() {
	val hand = HandRecord(
		id = "h1",
		tableId = "t1",
		createdAt = 0L,
		blinds = Blinds(sb = 500.0, bb = 1000.0),
		heroHand = PocketCards(Card(Rank.ACE, Suit.SPADES), Card(Rank.KING, Suit.SPADES)),
		heroSeat = 3,
		heroStack = 50000.0,
		buttonSeat = 1,
		streets = HandStreets(
			preflop = PreflopStreet(
				actions = listOf(
					Action(playerSeat = 4, type = ActionType.FOLD, stackBefore = 50000.0),
					Action(playerSeat = 5, type = ActionType.FOLD, stackBefore = 50000.0),
					Action(
						playerSeat = 6,
						type = ActionType.RAISE,
						amount = 2500.0,
						stackBefore = 50000.0,
						betLevel = 2,
					),
					Action(playerSeat = 7, type = ActionType.FOLD, stackBefore = 50000.0),
					Action(playerSeat = 8, type = ActionType.FOLD, stackBefore = 50000.0),
					Action(playerSeat = 9, type = ActionType.FOLD, stackBefore = 50000.0),
					Action(playerSeat = 1, type = ActionType.FOLD, stackBefore = 50000.0),
					Action(playerSeat = 2, type = ActionType.FOLD, stackBefore = 49500.0),
					Action(
						playerSeat = 3,
						type = ActionType.RAISE,
						amount = 8000.0,
						stackBefore = 49000.0,
						betLevel = 3,
					),
					Action(playerSeat = 6, type = ActionType.CALL, amount = 8000.0, stackBefore = 47500.0),
				),
			),
			flop = FlopStreet(
				card1 = Card(Rank.ACE, Suit.HEARTS),
				card2 = Card(Rank.TEN, Suit.DIAMONDS),
				card3 = Card(Rank.SEVEN, Suit.CLUBS),
				actions = listOf(
					Action(
						playerSeat = 3,
						type = ActionType.BET,
						amount = 5000.0,
						stackBefore = 41000.0,
						betLevel = 1,
					),
					Action(playerSeat = 6, type = ActionType.CALL, amount = 5000.0, stackBefore = 39500.0),
				),
			),
			turn = TurnStreet(
				card = Card(Rank.KING, Suit.HEARTS),
				actions = listOf(
					Action(playerSeat = 3, type = ActionType.CHECK, stackBefore = 36000.0),
					Action(
						playerSeat = 6,
						type = ActionType.BET,
						amount = 12000.0,
						stackBefore = 34500.0,
						betLevel = 1,
					),
					Action(
						playerSeat = 3,
						type = ActionType.RAISE,
						amount = 30000.0,
						stackBefore = 36000.0,
						betLevel = 2,
					),
					Action(playerSeat = 6, type = ActionType.ALL_IN, amount = 34500.0, stackBefore = 22500.0),
					Action(playerSeat = 3, type = ActionType.CALL, amount = 34500.0, stackBefore = 6000.0),
				),
			),
			river = RiverStreet(
				card = Card(Rank.TWO, Suit.CLUBS),
			),
		),
		result = 49000.0,
		memo = "탑투페어로 체크레이즈 → 올인 콜, 상대 ATo",
	)

	ThemePreview {
		Text(
			text = HandHistoryFormatter.format(hand),
			style = HandyTheme.typography.regular12,
			color = HandyTheme.colorScheme.textPrimary,
			modifier = Modifier
				.background(HandyTheme.colorScheme.background)
				.verticalScroll(rememberScrollState())
				.padding(16.dp),
		)
	}
}

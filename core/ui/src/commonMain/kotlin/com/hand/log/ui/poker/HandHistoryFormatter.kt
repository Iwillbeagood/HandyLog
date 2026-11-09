package com.hand.log.ui.poker

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
import com.hand.log.domain.model.HandPlayer
import com.hand.log.domain.model.PocketCards
import com.hand.log.domain.model.PreflopStreet
import com.hand.log.domain.model.Rank
import com.hand.log.domain.model.ShowdownEntry
import com.hand.log.domain.model.RiverStreet
import com.hand.log.domain.model.Street
import com.hand.log.domain.model.Suit
import com.hand.log.domain.model.TurnStreet
import com.hand.log.domain.model.quick.BetSize
import com.hand.log.utils.poker.HandEvaluator
import kotlin.math.roundToLong

data class HandHistoryProgress(
	val openStreet: Street? = null,
	val isShowdownOpen: Boolean = false,
	val pendingShowdownSeats: Set<Int> = emptySet(),
	val isComplete: Boolean = true,
) {
	fun isStreetClosed(street: Street): Boolean = openStreet != street

	companion object {
		val Complete = HandHistoryProgress()
	}
}

object HandHistoryFormatter {

	fun format(
		hand: HandRecord,
		progress: HandHistoryProgress = HandHistoryProgress.Complete,
		showStacks: Boolean = true,
	): String = buildString {
		val bb = hand.bbAmount
		val heroSeat = hand.heroSeat
		val preflopFoldedSeats = hand.preflopFoldedSeats

		val allSeats = hand.allSeats
		val preflopSeatsWithNonFoldAction = hand.streets.preflop.actions
			.filter { it.type != ActionType.FOLD }
			.map { it.playerSeat }
			.toSet()
		if (showStacks) {
			appendLine("[Stacks]")
			allSeats.forEach { seat ->
				if (seat in preflopFoldedSeats && seat !in preflopSeatsWithNonFoldAction) return@forEach
				val posName = hand.getPositionName(seat)
				val isHero = seat == heroSeat
				val stack = hand.getInitialStack(seat)
				if (stack != null) {
					val bbStack = if (stack == 0.0) "??BB" else formatBb(stack, bb)
					val heroCards = if (isHero) {
						hand.heroHoleCards?.let { " ${formatCard(it.card1)}${formatCard(it.card2)}" } ?: ""
					} else {
						""
					}
					if (isHero) {
						appendLine("$posName (Hero) - $bbStack$heroCards")
					} else {
						appendLine("$posName - $bbStack")
					}
				}
			}
			appendLine()
		} else {
			hand.heroHoleCards?.let {
				appendLine(
					"${hand.getPositionName(heroSeat)} (Hero) ${formatCard(it.card1)}${formatCard(it.card2)}",
				)
				appendLine()
			}
		}

		appendLine("[Preflop]")
		val preflopActions = hand.streets.preflop.actions
		var preflopMaxBet = bb
		val preflopSeatsWithAction = mutableSetOf<Int>()
		val sbSeat = hand.seatRing.sbSeat
		val bbSeat = hand.seatRing.bbSeat
		val sbAmount = hand.blinds?.sb ?: 0.0
		val seatPrevAmount = mutableMapOf(sbSeat to sbAmount, bbSeat to bb)

		preflopActions.forEach { action ->
			if (action.type == ActionType.FOLD) {
				if (action.playerSeat in preflopSeatsWithAction) {
					val posName = hand.getPositionName(action.playerSeat)
					val isHero = action.playerSeat == heroSeat
					val prefix = if (isHero) "$posName (Hero)" else posName
					appendLine("$prefix folds")
				}
				return@forEach
			}
			preflopSeatsWithAction.add(action.playerSeat)
			val posName = hand.getPositionName(action.playerSeat)
			val isHero = action.playerSeat == heroSeat
			val prefix = if (isHero) "$posName (Hero)" else posName
			val amount = action.amount ?: 0.0
			val prevAmount = seatPrevAmount[action.playerSeat] ?: 0.0
			val additionalAmount = amount - prevAmount

			when (action.type) {
				ActionType.CALL -> {
					if (action.amount == null) {
						appendLine("$prefix calls")
					} else if (additionalAmount <= 0) {
						appendLine("$prefix checks")
					} else {
						val isCallAllIn = action.stackAfter == 0.0 ||
							(action.stackBefore != null && amount >= action.stackBefore!!)
						if (isCallAllIn) {
							appendLine("$prefix calls all-in for ${formatBb(additionalAmount, bb)}")
						} else {
							appendLine("$prefix calls ${formatBb(additionalAmount, bb)}")
						}
					}
				}
				ActionType.RAISE -> {
					val label = formatRaiseLabel(action.betLevel)
					appendLine("$prefix $label ${formatSized(action, amount, bb, isRaise = true)}")
					preflopMaxBet = amount
				}
				ActionType.BET -> {
					appendLine("$prefix bets ${formatSized(action, amount, bb, isRaise = false)}")
					preflopMaxBet = amount
				}
				ActionType.ALL_IN -> {
					if (action.amount == null) {
						appendLine("$prefix all-in")
					} else if (amount > preflopMaxBet) {
						val allInLabel = if (preflopMaxBet > bb) "raises to" else "bets"
						appendLine("$prefix $allInLabel ${formatBb(amount, bb)} (all-in)")
						preflopMaxBet = amount
					} else {
						appendLine("$prefix calls all-in for ${formatBb(additionalAmount, bb)}")
					}
				}
				ActionType.CHECK -> appendLine("$prefix checks")
				else -> {}
			}
			if (amount > 0) seatPrevAmount[action.playerSeat] = amount
		}

		if (progress.isStreetClosed(Street.PREFLOP)) {
			val preflopPot = hand.potAt(Street.PREFLOP)
			val preflopActive = hand.getActiveCountAt(Street.PREFLOP)
			appendLine()
			appendLine(
				"→ Pot: ${formatPot(
					hand,
					Street.PREFLOP,
					preflopPot,
					bb,
				)}${formatWay(preflopActive)}",
			)
			appendLine()
		}

		hand.streets.flop?.let { flop ->
			appendLine("[Flop] ${flop.cards.joinToString(" ") { formatCard(it) }}")
			formatStreetActions(hand, flop.actions, heroSeat, bb, this)
			if (!progress.isStreetClosed(Street.FLOP)) return@let
			val flopPot = hand.potAt(Street.FLOP)
			val flopActive = hand.getActiveCountAt(Street.FLOP)
			appendLine(
				"→ Pot: ${formatPot(
					hand,
					Street.FLOP,
					flopPot,
					bb,
				)}${formatWay(flopActive)}",
			)
			appendLine()
		}

		hand.streets.turn?.let { turn ->
			appendLine("[Turn] ${turn.cards.joinToString(" ") { formatCard(it) }}")
			formatStreetActions(hand, turn.actions, heroSeat, bb, this)
			if (!progress.isStreetClosed(Street.TURN)) return@let
			val turnPot = hand.potAt(Street.TURN)
			appendLine("→ Pot: ${formatPot(hand, Street.TURN, turnPot, bb)}")
			appendLine()
		}

		hand.streets.river?.let { river ->
			appendLine("[River] ${river.cards.joinToString(" ") { formatCard(it) }}")
			if (river.actions.isNotEmpty()) {
				formatStreetActions(hand, river.actions, heroSeat, bb, this)
			}
			if (!progress.isStreetClosed(Street.RIVER)) return@let
			val riverPot = hand.potAt(Street.RIVER)
			appendLine("→ Pot: ${formatPot(hand, Street.RIVER, riverPot, bb)}")
			appendLine()
		}

		val pots = if (progress.isComplete && !hand.isFoldWin) calculatePots(hand) else emptyList()
		val hasSidePots = pots.size >= 2
		if (hasSidePots) {
			appendLine("[Pot]")
			pots.forEachIndexed { index, pot ->
				val label = if (index == 0) "Main" else "Side $index"
				appendLine("$label: ${formatBb(pot.amount, bb)}${formatWay(pot.eligibleCount)}")
			}
			appendLine()
		}

		val showShowdown = progress.isShowdownOpen ||
			(progress.isComplete && !hand.isFoldWin && (hand.showdown.isNotEmpty() || hand.heroHoleCards != null))
		if (showShowdown) {
			val seatPotOutcome: Map<Int, String> = if (hasSidePots) {
				val allShowdownSeats = buildSet {
					hand.heroHoleCards?.let { add(heroSeat) }
					hand.showdown.forEach { add(it.seat) }
				}
				allShowdownSeats.associateWith { seat -> formatPotOutcome(seat, pots) }
			} else {
				emptyMap()
			}

			appendLine("[Showdown]")
			hand.heroHoleCards?.takeIf {
				heroSeat in hand.remainingSeats || !progress.isShowdownOpen
			}?.let { heroCards ->
				val posName = hand.getPositionName(heroSeat)
				val result = hand.getShowdownResult(heroSeat)
				val cards = "${formatCard(heroCards.card1)} ${formatCard(heroCards.card2)}"
				if (result == null && progress.isShowdownOpen) {
					appendLine("$posName (Hero): $cards")
				} else {
					val ranking = result?.ranking?.name?.replace("_", " ") ?: ""
					val outcome = seatPotOutcome[heroSeat] ?: result?.outcome?.name ?: ""
					appendLine("$posName (Hero): $cards — $ranking [$outcome]")
				}
			}
			hand.showdown.filter { it.seat != heroSeat }.forEach { entry ->
				val posName = hand.getPositionName(entry.seat)
				val result = hand.getShowdownResult(entry.seat)
				val cards = "${formatCard(entry.card1)} ${formatCard(entry.card2)}"
				if (result == null && progress.isShowdownOpen) {
					appendLine("$posName: $cards")
				} else {
					val ranking = result?.ranking?.name?.replace("_", " ") ?: ""
					val outcome = seatPotOutcome[entry.seat] ?: result?.outcome?.name ?: ""
					appendLine("$posName: $cards — $ranking [$outcome]")
				}
			}
			hand.unknownCardSeats.filter { it !in progress.pendingShowdownSeats }.forEach { seat ->
				val posName = hand.getPositionName(seat)
				appendLine("$posName: [mucked]")
			}
			appendLine()
		}

		if (!progress.isComplete) return@buildString

		val finalStacks = hand.results?.finalStacks
			?: hand.getFinalStacks(HandEvaluator::calculateShowdown)
		val heroInitialStack = hand.getInitialStack(heroSeat)
		val heroFinalStack = finalStacks[heroSeat]
		val resultLabelText = hand.resultLabel ?: ""
		val heroProfit = if (heroInitialStack != null && heroFinalStack != null) {
			heroFinalStack - heroInitialStack
		} else {
			hand.result
		}
		if (heroProfit != null) {
			val sign = if (heroProfit >= 0) "+" else ""
			val resultPrefix = if (resultLabelText.isNotEmpty()) "$resultLabelText " else ""
			appendLine("[Result] $resultPrefix$sign${formatBb2(heroProfit, bb)}")
		} else {
			appendLine("[Result] $resultLabelText")
		}

		if (showStacks) {
			allSeats.forEach { seat ->
				if (seat in preflopFoldedSeats && seat !in preflopSeatsWithNonFoldAction) return@forEach
				hand.getInitialStack(seat) ?: return@forEach
				val finalStack = finalStacks[seat] ?: return@forEach
				val posName = hand.getPositionName(seat)
				val isHero = seat == heroSeat
				val prefix = if (isHero) "$posName (Hero)" else posName
				appendLine("$prefix: ${formatBb2(finalStack, bb)}")
			}
		}

		hand.memo?.let { memo ->
			appendLine()
			appendLine(memo)
		}
	}

	fun formatActor(hand: HandRecord, seat: Int): String {
		val posName = hand.getPositionName(seat)
		return if (seat == hand.heroSeat) "$posName (Hero)" else posName
	}

	private fun formatStreetActions(
		hand: HandRecord,
		actions: List<Action>,
		heroSeat: Int,
		bb: Double,
		sb: StringBuilder,
	) {
		val prefoldPositions = mutableListOf<String>()
		val seatsWithAction = mutableSetOf<Int>()
		val seatPrev = mutableMapOf<Int, Double>()
		var currentMaxBet = 0.0

		actions.forEach { action ->
			val posName = hand.getPositionName(action.playerSeat)
			val isHero = action.playerSeat == heroSeat
			val prefix = if (isHero) "$posName (Hero)" else posName
			val amount = action.amount ?: 0.0
			val prevAmount = seatPrev[action.playerSeat] ?: 0.0
			val additionalAmount = amount - prevAmount

			when (action.type) {
				ActionType.FOLD -> {
					if (action.playerSeat in seatsWithAction) {
						sb.appendLine("$prefix folds")
					} else {
						prefoldPositions.add(prefix)
					}
				}
				ActionType.CHECK -> {
					seatsWithAction.add(action.playerSeat)
					sb.appendLine("$prefix checks")
				}
				ActionType.CALL -> {
					seatsWithAction.add(action.playerSeat)
					if (action.amount == null) {
						sb.appendLine("$prefix calls")
						return@forEach
					}
					val isCallAllIn = action.stackAfter == 0.0 ||
						(action.stackBefore != null && amount >= action.stackBefore!!)
					if (isCallAllIn) {
						sb.appendLine("$prefix calls all-in for ${formatBb(additionalAmount, bb)}")
					} else {
						sb.appendLine("$prefix calls ${formatBb(additionalAmount, bb)}")
					}
				}
				ActionType.BET -> {
					seatsWithAction.add(action.playerSeat)
					sb.appendLine("$prefix bets ${formatSized(action, amount, bb, isRaise = false)}")
					currentMaxBet = amount
				}
				ActionType.RAISE -> {
					seatsWithAction.add(action.playerSeat)
					val label = formatRaiseLabel(action.betLevel)
					sb.appendLine("$prefix $label ${formatSized(action, amount, bb, isRaise = true)}")
					currentMaxBet = amount
				}
				ActionType.ALL_IN -> {
					seatsWithAction.add(action.playerSeat)
					if (action.amount == null) {
						sb.appendLine("$prefix all-in")
						return@forEach
					}
					if (amount <= 0) return@forEach
					if (amount > currentMaxBet) {
						sb.appendLine("$prefix bets ${formatBb(amount, bb)} (all-in)")
						currentMaxBet = amount
					} else {
						sb.appendLine("$prefix calls all-in for ${formatBb(additionalAmount, bb)}")
					}
				}
			}
			if (amount > 0) seatPrev[action.playerSeat] = amount
		}
		if (prefoldPositions.isNotEmpty()) {
			sb.appendLine(
				"${prefoldPositions.joinToString(", ")} fold${if (prefoldPositions.size > 1) "" else "s"}",
			)
		}
	}

	private data class PotInfo(
		val amount: Double,
		val eligibleCount: Int,
		val winnerSeats: Set<Int> = emptySet(),
	)

	private fun calculatePots(hand: HandRecord): List<PotInfo> {
		val investments = hand.results?.seatInvestments ?: hand.seatInvestments
		if (investments.isEmpty()) return emptyList()

		val anteCost = hand.anteAmount
		val remaining = hand.remainingSeats
		val sortedLevels = investments.values.distinct().sorted()
		var previousLevel = 0.0
		val rawPots = mutableListOf<Pair<Double, Set<Int>>>()

		for (level in sortedLevels) {
			val diff = level - previousLevel
			if (diff <= 0) continue
			val eligibleSeats = investments.filter { it.value >= level }.keys
			val potForLevel = diff * eligibleSeats.size
			val remainingEligible = eligibleSeats.intersect(remaining)
			rawPots.add(potForLevel to remainingEligible)
			previousLevel = level
		}

		val boardCards = hand.streets.boardCards
		val entries = buildList {
			hand.heroHoleCards?.let { add(ShowdownEntry(seat = hand.heroSeat, cards = it)) }
			hand.showdown.filter { it.seat != hand.heroSeat }.forEach { add(it) }
		}

		val merged = mutableListOf<PotInfo>()
		var i = 0
		while (i < rawPots.size) {
			var amount = rawPots[i].first
			val eligible = rawPots[i].second
			while (i + 1 < rawPots.size && rawPots[i + 1].second == eligible) {
				i++
				amount += rawPots[i].first
			}
			if (merged.isEmpty()) amount += anteCost
			if (eligible.size >= 2) {
				val winners = calculatePotWinners(eligible, entries, boardCards)
				merged.add(PotInfo(amount, eligible.size, winners))
			}
			i++
		}
		return merged
	}

	private fun formatPotOutcome(seat: Int, pots: List<PotInfo>): String {
		val wins = mutableListOf<String>()
		pots.forEachIndexed { index, pot ->
			if (seat in pot.winnerSeats) {
				val label = if (index == 0) "Main" else "Side $index"
				val result = if (pot.winnerSeats.size > 1) "SPLIT" else "WIN"
				wins.add("$label $result")
			}
		}
		return if (wins.isEmpty()) "LOSE" else wins.joinToString(", ")
	}

	private fun calculatePotWinners(
		eligible: Set<Int>,
		entries: List<ShowdownEntry>,
		boardCards: List<Card>,
	): Set<Int> {
		if (eligible.size == 1) return eligible
		val eligibleEntries = entries.filter { it.seat in eligible }
		if (eligibleEntries.size < 2 || boardCards.size != 5) {
			return if (eligibleEntries.size == 1) setOf(eligibleEntries.first().seat) else emptySet()
		}
		val results = HandEvaluator.calculateShowdown(boardCards, eligibleEntries)
		return results.filter { it.isWinner || it.isSplit }.map { it.seat }.toSet()
	}

	fun formatRaiseLabel(betLevel: Int): String = when (betLevel) {
		2 -> "opens to"
		3 -> "3-bets to"
		4 -> "4-bets to"
		5 -> "5-bets to"
		else -> "raises to"
	}

	fun formatPotSize(size: BetSize, isRaise: Boolean): String = if (isRaise) {
		when (size) {
			BetSize.THIRD -> "2x"
			BetSize.HALF -> "2.5x"
			BetSize.TWO_THIRDS -> "3x"
			BetSize.POT -> "4x"
		}
	} else {
		when (size) {
			BetSize.THIRD -> "1/3"
			BetSize.HALF -> "1/2"
			BetSize.TWO_THIRDS -> "2/3"
			BetSize.POT -> "Pot"
		}
	}

	private fun formatSized(action: Action, amount: Double, bb: Double, isRaise: Boolean): String {
		val size = action.potSize ?: return formatBb(amount, bb)
		return "${formatPotSize(size, isRaise)} (${formatBb(amount, bb)})"
	}

	private fun formatWay(activeCount: Int): String = when {
		activeCount > 2 -> " ($activeCount-way)"
		activeCount == 2 -> " (Heads-up)"
		else -> ""
	}

	private fun formatPot(hand: HandRecord, street: Street, pot: Double, bb: Double): String {
		val hasUnknown = Street.entries.filter { it <= street }.any { s ->
			hand.streets.getActions(s).any {
				it.amount == null && (it.type == ActionType.ALL_IN || it.type == ActionType.CALL)
			}
		}
		return if (hasUnknown) UNKNOWN_AMOUNT else formatBb(pot, bb)
	}

	private const val UNKNOWN_AMOUNT = "?"

	fun formatBb(amount: Double, bb: Double): String {
		if (bb <= 0) return "${amount.toLong()}"
		return formatBbCount(amount / bb)
	}

	private fun formatBb2(amount: Double, bb: Double): String {
		if (bb <= 0) return "${amount.toLong()}"
		val rounded = (amount / bb * 100).roundToLong() / 100.0
		val text = if (rounded == rounded.toLong().toDouble()) {
			"${rounded.toLong()}"
		} else {
			rounded.toString().trimEnd('0').trimEnd('.')
		}
		return "${text}BB"
	}

	fun formatCard(card: Card): String {
		val suit = when (card.suit) {
			Suit.SPADES -> "♠"
			Suit.HEARTS -> "♥"
			Suit.DIAMONDS -> "♦"
			Suit.CLUBS -> "♣"
			null -> ""
		}
		return "${card.rank.symbol}$suit"
	}

}

@ThemePreviews
@Composable
private fun HandHistoryFormatterPreview() {
	val boardCards = listOf(
		Card(Rank.ACE, Suit.HEARTS),
		Card(Rank.TEN, Suit.DIAMONDS),
		Card(Rank.SEVEN, Suit.CLUBS),
		Card(Rank.KING, Suit.HEARTS),
		Card(Rank.TWO, Suit.CLUBS),
	)
	val showdownEntries = listOf(
		ShowdownEntry(
			seat = 3,
			cards = PocketCards(Card(Rank.ACE, Suit.SPADES), Card(Rank.KING, Suit.SPADES)),
		),
		ShowdownEntry(
			seat = 6,
			cards = PocketCards(Card(Rank.ACE, Suit.HEARTS), Card(Rank.TEN, Suit.HEARTS)),
		),
	)
	val results = HandEvaluator.calculateShowdown(boardCards, showdownEntries)
	val hand = HandRecord(
		id = "h1",
		tableId = "t1",
		createdAt = 0L,
		blinds = Blinds(sb = 500.0, bb = 1000.0),
		heroSeat = 3,
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
		players = listOf(
			HandPlayer(
				seat = 3,
				cards = PocketCards(Card(Rank.ACE, Suit.SPADES), Card(Rank.KING, Suit.SPADES)),
				initialStack = 50000.0,
				ranking = results.find { it.seat == 3 }?.ranking,
				bestCards = results.find { it.seat == 3 }?.bestCards ?: emptyList(),
				outcome = results.find { it.seat == 3 }?.outcome,
				isHero = true,
			),
			HandPlayer(
				seat = 6,
				cards = PocketCards(Card(Rank.ACE, Suit.HEARTS), Card(Rank.TEN, Suit.HEARTS)),
				ranking = results.find { it.seat == 6 }?.ranking,
				bestCards = results.find { it.seat == 6 }?.bestCards ?: emptyList(),
				outcome = results.find { it.seat == 6 }?.outcome,
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

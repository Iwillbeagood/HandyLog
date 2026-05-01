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
import com.hand.log.domain.model.HandPlayer
import com.hand.log.domain.model.PocketCards
import com.hand.log.domain.model.PreflopStreet
import com.hand.log.domain.model.Rank
import com.hand.log.domain.model.ShowdownEntry
import com.hand.log.domain.model.RiverStreet
import com.hand.log.domain.model.Street
import com.hand.log.domain.model.Suit
import com.hand.log.domain.model.TurnStreet
import com.hand.log.ui.poker.formatBbCount
import com.hand.log.utils.poker.HandEvaluator

object HandHistoryFormatter {

	fun format(hand: HandRecord): String = buildString {
		val bb = hand.bbAmount
		val heroSeat = hand.heroSeat
		val preflopFoldedSeats = hand.preflopFoldedSeats

		// --- Stacks (액션 없이 바로 폴드한 좌석만 제외) ---
		appendLine("[Stacks]")
		val allSeats = hand.allSeats
		val preflopSeatsWithNonFoldAction = hand.streets.preflop.actions
			.filter { it.type != ActionType.FOLD }
			.map { it.playerSeat }
			.toSet()
		allSeats.forEach { seat ->
			if (seat in preflopFoldedSeats && seat !in preflopSeatsWithNonFoldAction) return@forEach
			val posName = hand.getPositionName(seat)
			val isHero = seat == heroSeat
			val stack = hand.getInitialStack(seat)
			if (stack != null) {
				val bbStack = if (stack == 0.0) "??BB" else formatBb(stack, bb)
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
		var preflopMaxBet = bb
		val preflopSeatsWithAction = mutableSetOf<Int>()
		// SB/BB는 이미 블라인드를 냈으므로 초기 금액 설정
		val count = hand.playerCount
		val btn = hand.buttonSeat
		val sbSeat = if (count > 0) (btn % count) + 1 else 0
		val bbSeat = if (count > 0) ((btn + 1) % count) + 1 else 0
		val sbAmount = hand.blinds?.sb ?: 0.0
		val seatPrevAmount = mutableMapOf(sbSeat to sbAmount, bbSeat to bb)

		preflopActions.forEach { action ->
			if (action.type == ActionType.FOLD) {
				if (action.playerSeat in preflopSeatsWithAction) {
					val posName = hand.getPositionName(action.playerSeat)
					val isHero = action.playerSeat == heroSeat
					val prefix = if (isHero) "Hero ($posName)" else posName
					appendLine("$prefix folds")
				} else {
					foldCount++
				}
				return@forEach
			}
			preflopSeatsWithAction.add(action.playerSeat)
			val posName = hand.getPositionName(action.playerSeat)
			val isHero = action.playerSeat == heroSeat
			val prefix = if (isHero) "Hero ($posName)" else posName
			val amount = action.amount ?: 0.0
			val prevAmount = seatPrevAmount[action.playerSeat] ?: 0.0
			val additionalAmount = amount - prevAmount

			when (action.type) {
				ActionType.CALL -> {
					if (additionalAmount <= 0) {
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
					appendLine("$prefix $label ${formatBb(amount, bb)}")
					preflopMaxBet = amount
				}
				ActionType.BET -> {
					appendLine("$prefix bets ${formatBb(amount, bb)}")
					preflopMaxBet = amount
				}
				ActionType.ALL_IN -> {
					if (amount > preflopMaxBet) {
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

		val activePlayers = preflopActions.map { it.playerSeat }.distinct().count() - foldCount
		val preflopPot = hand.getPotAtStreet(Street.PREFLOP)
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
			formatStreetActions(hand, flop.actions, heroSeat, bb, this)
			val flopPot = hand.getPotAtStreet(Street.FLOP)
			val flopActive = hand.getActiveCountAt(Street.FLOP)
			appendLine(
				"→ Pot: ${formatBb(flopPot, bb)} (${if (flopActive > 2) "$flopActive-way" else "Heads-up"})",
			)
			appendLine()
		}

		// --- Turn ---
		hand.streets.turn?.let { turn ->
			appendLine("[Turn] ${turn.cards.joinToString(" ") { formatCard(it) }}")
			formatStreetActions(hand, turn.actions, heroSeat, bb, this)
			val turnPot = hand.getPotAtStreet(Street.TURN)
			appendLine("→ Pot: ${formatBb(turnPot, bb)}")
			appendLine()
		}

		// --- River ---
		hand.streets.river?.let { river ->
			appendLine("[River] ${river.cards.joinToString(" ") { formatCard(it) }}")
			if (river.actions.isNotEmpty()) {
				formatStreetActions(hand, river.actions, heroSeat, bb, this)
			}
			val riverPot = hand.getPotAtStreet(Street.RIVER)
			appendLine("→ Pot: ${formatBb(riverPot, bb)}")
			appendLine()
		}

		// --- Pot Breakdown (사이드팟이 있을 때만) ---
		val pots = if (!hand.isFoldWin) calculatePots(hand) else emptyList()
		val hasSidePots = pots.size >= 2
		if (hasSidePots) {
			appendLine("[Pot]")
			pots.forEachIndexed { index, pot ->
				val label = if (index == 0) "Main" else "Side $index"
				val wayLabel = if (pot.eligibleCount > 2) "${pot.eligibleCount}-way" else "Heads-up"
				appendLine("$label: ${formatBb(pot.amount, bb)} ($wayLabel)")
			}
			appendLine()
		}

		// --- Showdown ---
		if (!hand.isFoldWin && (hand.showdown.isNotEmpty() || hand.heroHand != null)) {
			// 사이드팟이 있으면 좌석별 팟 승패 라벨 생성
			val seatPotOutcome: Map<Int, String> = if (hasSidePots) {
				val allShowdownSeats = buildSet {
					hand.heroHand?.let { add(heroSeat) }
					hand.showdown.forEach { add(it.seat) }
				}
				allShowdownSeats.associateWith { seat -> formatPotOutcome(seat, pots) }
			} else {
				emptyMap()
			}

			appendLine("[Showdown]")
			// 히어로
			hand.heroHand?.let { heroCards ->
				val posName = hand.getPositionName(heroSeat)
				val result = hand.getShowdownResult(heroSeat)
				val ranking = result?.ranking?.name?.replace("_", " ") ?: ""
				val outcome = seatPotOutcome[heroSeat] ?: result?.outcome?.name ?: ""
				appendLine(
					"Hero ($posName): ${formatCard(
						heroCards.card1,
					)} ${formatCard(heroCards.card2)} — $ranking [$outcome]",
				)
			}
			// 상대
			hand.showdown.filter { it.seat != heroSeat }.forEach { entry ->
				val posName = hand.getPositionName(entry.seat)
				val result = hand.getShowdownResult(entry.seat)
				val ranking = result?.ranking?.name?.replace("_", " ") ?: ""
				val outcome = seatPotOutcome[entry.seat] ?: result?.outcome?.name ?: ""
				appendLine(
					"$posName: ${formatCard(entry.card1)} ${formatCard(entry.card2)} — $ranking [$outcome]",
				)
			}
			// 카드 미공개 플레이어
			hand.unknownCardSeats.forEach { seat ->
				val posName = hand.getPositionName(seat)
				appendLine("$posName: [mucked]")
			}
			appendLine()
		}

		// --- Result (팟에 참여한 플레이어만) ---
		val finalStacks = hand.getFinalStacks(HandEvaluator::calculateShowdown)
		val heroInitialStack = hand.getInitialStack(heroSeat)
		val heroFinalStack = finalStacks[heroSeat]
		if (heroInitialStack != null && heroFinalStack != null) {
			val heroProfit = heroFinalStack - heroInitialStack
			val sign = if (heroProfit >= 0) "+" else ""
			appendLine("[Result] $sign${formatBb(heroProfit, bb)}")
		} else {
			appendLine("[Result]")
		}
		val participatedSeats = allSeats.filter {
			it !in preflopFoldedSeats || it in preflopSeatsWithNonFoldAction
		}

		participatedSeats.forEach { seat ->
			val finalStack = finalStacks[seat] ?: return@forEach
			val initialStack = hand.getInitialStack(seat) ?: return@forEach
			val posName = hand.getPositionName(seat)
			val isHero = seat == heroSeat
			val prefix = if (isHero) "Hero ($posName)" else posName
			appendLine("$prefix: ${formatBb(finalStack, bb)}")
		}

		hand.memo?.let { memo ->
			appendLine()
			appendLine(memo)
		}
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
			val prefix = if (isHero) "Hero ($posName)" else posName
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
					sb.appendLine("$prefix bets ${formatBb(amount, bb)}")
					currentMaxBet = amount
				}
				ActionType.RAISE -> {
					seatsWithAction.add(action.playerSeat)
					val label = formatRaiseLabel(action.betLevel)
					sb.appendLine("$prefix $label ${formatBb(amount, bb)}")
					currentMaxBet = amount
				}
				ActionType.ALL_IN -> {
					seatsWithAction.add(action.playerSeat)
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

	/**
	 * 사이드팟 계산: 좌석별 총 투자액을 레벨별로 분리하고,
	 * 같은 remaining eligible set을 가진 연속 레벨을 병합.
	 * 1인 팟(언콜 베팅 반환)은 제외. 각 팟의 승자도 계산.
	 */
	private fun calculatePots(hand: HandRecord): List<PotInfo> {
		val investments = hand.seatInvestments
		if (investments.isEmpty()) return emptyList()

		val anteCost = if (hand.blinds?.isBigBlindAnte == true) (hand.blinds?.bb ?: 0.0) else 0.0
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
			hand.heroHand?.let { add(ShowdownEntry(seat = hand.heroSeat, cards = it)) }
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
		return formatBbCount(amount / bb)
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

package com.hand.log.record.contract

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import com.hand.log.domain.model.ActionType
import com.hand.log.domain.model.Blinds
import com.hand.log.domain.model.Card
import com.hand.log.domain.model.HandRanking
import com.hand.log.domain.model.GameType
import com.hand.log.domain.model.PocketCards
import com.hand.log.domain.model.Position
import com.hand.log.domain.model.PokerTable
import com.hand.log.domain.model.ShowdownEntry
import com.hand.log.domain.model.ShowdownOutcome
import com.hand.log.domain.model.ShowdownResult
import com.hand.log.domain.model.Street
import com.hand.log.record.model.PlayerStatus
import com.hand.log.record.model.RecordPlayers
import com.hand.log.domain.model.HandStreets
import com.hand.log.ui.poker.formatAmountFull
import com.hand.log.utils.poker.HandEvaluator

@Stable
internal sealed interface RecordHandState {

	@Immutable
	data object Loading : RecordHandState

	@Immutable
	data class Recording(
		val tableId: String,
		val table: PokerTable? = null,
		val buttonSeat: Int = 1,
		val blinds: Blinds? = null,
		val players: RecordPlayers = RecordPlayers(),
		val streets: HandStreets = HandStreets(),
		val currentActionSeat: Int? = null,
		val currentActionType: ActionType? = null,
		val currentActionAmount: String = "",
		val currentActionChipAmount: Double? = null,
		val memo: String = "",
		val currentStep: RecordStep = RecordStep.SETUP,
		val useBbUnit: Boolean = false,
		val actionPresets: ActionPresets = ActionPresets(),
		val isSaving: Boolean = false,
	) : RecordHandState {

		val heroHand: PocketCards?
			get() = players[table?.heroSeat ?: 0]?.cards

		val selectedCards: Set<Card>
			get() = buildSet {
				addAll(players.allCards)
				addAll(streets.boardCards)
			}

		val activeSteps: List<RecordStep>
			get() {
				if (currentStep != RecordStep.SHOWDOWN) {
					return RecordStep.entries
				}
				return buildList {
					add(RecordStep.SETUP)
					add(RecordStep.PREFLOP)
					if (streets.flop != null) add(RecordStep.FLOP)
					if (streets.turn != null) add(RecordStep.TURN)
					if (streets.river != null) add(RecordStep.RIVER)
					add(RecordStep.SHOWDOWN)
				}
			}

		val currentStreet: Street
			get() = when (currentStep) {
				RecordStep.SETUP, RecordStep.PREFLOP -> Street.PREFLOP
				RecordStep.FLOP -> Street.FLOP
				RecordStep.TURN -> Street.TURN
				RecordStep.RIVER, RecordStep.SHOWDOWN -> Street.RIVER
			}

		val heroStack: Double
			get() = players.getStack(table?.heroSeat ?: 0) ?: 0.0

		val heroInitialStack: Double
			get() = players.getInitialStack(table?.heroSeat ?: 0) ?: 0.0

		val occupiedSeats: List<Int>
			get() {
				val seats = (table?.players?.map { it.seat } ?: emptyList()).toMutableSet()
				table?.heroSeat?.let { seats.add(it) }
				return seats.sorted().toList()
			}

		private fun seatAtOffset(baseIdx: Int, offset: Int): Int {
			val seats = occupiedSeats
			if (seats.isEmpty()) return 0
			return seats[(baseIdx + offset) % seats.size]
		}

		val sbSeat: Int
			get() {
				val seats = occupiedSeats
				val btnIdx = seats.indexOf(buttonSeat)
				if (btnIdx < 0 || seats.size < 2) return 0
				return if (seats.size == 2) seats[btnIdx] else seatAtOffset(btnIdx, 1)
			}

		val bbSeat: Int
			get() {
				val seats = occupiedSeats
				val btnIdx = seats.indexOf(buttonSeat)
				if (btnIdx < 0 || seats.size < 2) return 0
				return seatAtOffset(btnIdx, if (seats.size == 2) 1 else 2)
			}

		val heroResult: Double
			get() {
				val heroSeat = table?.heroSeat ?: return 0.0
				return (showdownWinnings[heroSeat] ?: 0.0) - committedChips(heroSeat)
			}

		fun getPlayerStack(seat: Int): Double? = players.getStack(
			seat,
		)?.plus(showdownWinnings[seat] ?: 0.0)

		fun seatWonAnyPot(seat: Int?): Boolean = seat != null && potResults.any { it.winnerSeat == seat }

		fun lastAllInAmount(seat: Int): Double? =
			listOf(Street.PREFLOP, Street.FLOP, Street.TURN, Street.RIVER)
				.flatMap { streets.getActions(it) }
				.lastOrNull { it.playerSeat == seat && it.type == ActionType.ALL_IN }
				?.amount

		fun getBlindCost(seat: Int): Double {
			val sb = blinds?.sb ?: 0.0
			val bb = blinds?.bb ?: 0.0
			return when (seat) {
				sbSeat -> sb
				bbSeat -> bb + if (blinds?.isBigBlindAnte == true) bb else 0.0
				else -> 0.0
			}
		}

		val anteAmount: Double
			get() = if (blinds?.isBigBlindAnte == true) blinds.bb else 0.0

		private fun postedBlind(seat: Int): Double = when (seat) {
			sbSeat -> blinds?.sb ?: 0.0
			bbSeat -> blinds?.bb ?: 0.0
			else -> 0.0
		}

		fun streetContribution(seat: Int, street: Street = currentStreet): Double {
			val bet = streets.getActions(street)
				.filter { it.playerSeat == seat }
				.maxOfOrNull { it.amount ?: 0.0 } ?: 0.0
			return if (street == Street.PREFLOP) maxOf(bet, postedBlind(seat)) else bet
		}

		val seatInvestments: Map<Int, Double>
			get() = occupiedSeats
				.associateWith { seat -> Street.entries.sumOf { streetContribution(seat, it) } }
				.filterValues { it > 0.0 }

		fun committedChips(seat: Int): Double {
			if (currentStep == RecordStep.SETUP) return 0.0
			val ante = if (seat == bbSeat) anteAmount else 0.0
			return (seatInvestments[seat] ?: 0.0) + ante
		}

		val lastAggressorSeat: Int?
			get() {
				val actions = streets.getActions(currentStreet)
				var aggressor: Int? = null
				actions.forEachIndexed { index, action ->
					val othersMax = actions.take(index)
						.filter { it.playerSeat != action.playerSeat }
						.maxOfOrNull { it.amount ?: 0.0 } ?: 0.0
					val raisesTheBet = action.type == ActionType.BET ||
						action.type == ActionType.RAISE ||
						(action.type == ActionType.ALL_IN && (action.amount ?: 0.0) > othersMax)
					if (raisesTheBet) aggressor = action.playerSeat
				}
				return aggressor
			}

		val playersFromActionLog: RecordPlayers
			get() {
				val lastActionTypeBySeat = Street.entries
					.flatMap { streets.getActions(it) }
					.associate { it.playerSeat to it.type }
				return occupiedSeats.fold(players) { rebuilt, seat ->
					rebuilt.update(seat) {
						copy(
							stack = initialStack?.let { (it - committedChips(seat)).coerceAtLeast(0.0) } ?: 0.0,
							currentBet = if (currentStep == RecordStep.SETUP) 0.0 else streetContribution(seat),
							status = when (lastActionTypeBySeat[seat]) {
								ActionType.FOLD -> PlayerStatus.FOLDED
								ActionType.ALL_IN -> PlayerStatus.ALL_IN
								else -> PlayerStatus.ACTIVE
							},
						)
					}
				}
			}

		val isFoldWin: Boolean
			get() = remainingSeats.size == 1 && remainingSeats.first() == table?.heroSeat

		val isFoldLoss: Boolean
			get() {
				val heroSeat = table?.heroSeat ?: return false
				return heroSeat in players.foldedSeats
			}

		val remainingSeats: List<Int>
			get() = occupiedSeats.filter { it !in players.foldedSeats }

		val isAllInShowdown: Boolean
			get() {
				val remaining = remainingSeats
				if (remaining.size < 2) return false
				return remaining.count { it !in players.allInSeats } <= 1
			}

		val showdownEntries: List<ShowdownEntry>
			get() = buildList {
				remainingSeats.forEach { seat ->
					val hand = players[seat]?.cards
					if (hand != null) {
						add(ShowdownEntry(seat = seat, cards = hand))
					}
				}
			}

		val isShowdownComplete: Boolean
			get() = remainingSeats.all { seat ->
				val player = players[seat]
				player?.cards != null || player?.isCardsUnknown == true
			}

		val showdownResults: List<ShowdownResult>
			get() {
				if (remainingSeats.size == 1) {
					val winnerSeat = remainingSeats.first()
					return occupiedSeats.map { seat ->
						ShowdownResult(
							seat = seat,
							ranking = HandRanking.WIN_BY_FOLD,
							bestCards = emptyList(),
							outcome = if (seat == winnerSeat) ShowdownOutcome.WIN else ShowdownOutcome.LOSE,
						)
					}
				}

				if (!isShowdownComplete || streets.boardCards.size != 5) return emptyList()
				val knownEntries = showdownEntries.filter { players[it.seat]?.isCardsUnknown != true }
				if (knownEntries.size < 2) return emptyList()

				return HandEvaluator.calculateShowdown(streets.boardCards, knownEntries)
			}

		val showdownWinnings: Map<Int, Double>
			get() {
				if (currentStep != RecordStep.SHOWDOWN || !isShowdownComplete || showdownResults.isEmpty()) {
					return emptyMap()
				}
				val investments = seatInvestments
				val contenders = remainingSeats.toSet()
				val knownEntries = showdownEntries.filter { players[it.seat]?.isCardsUnknown != true }
				val winnings = mutableMapOf<Int, Double>()
				var deadMoney = anteAmount
				var previousLevel = 0.0
				for (level in investments.values.distinct().sorted()) {
					val eligibleSeats = investments.filterValues { it >= level }.keys
					val pot = (level - previousLevel) * eligibleSeats.size + deadMoney
					val winners = potWinners(eligibleSeats.intersect(contenders), knownEntries)
						.ifEmpty { listOfNotNull(eligibleSeats.singleOrNull()) }
					winners.forEach { seat -> winnings[seat] = (winnings[seat] ?: 0.0) + pot / winners.size }
					deadMoney = 0.0
					previousLevel = level
				}
				return winnings
			}

		private fun potWinners(contenders: Set<Int>, knownEntries: List<ShowdownEntry>): List<Int> {
			if (contenders.size == 1) return contenders.toList()
			val entries = knownEntries.filter { it.seat in contenders }
			if (entries.size == 1) return listOf(entries.first().seat)
			if (entries.size < 2 || streets.boardCards.size != 5) return emptyList()
			return HandEvaluator.calculateShowdown(streets.boardCards, entries)
				.filter { it.isWinner || it.isSplit }
				.map { it.seat }
		}

		val bbText: String
			get() = blinds?.bb?.let { if (it == 0.0) "" else it.toLong().toString() } ?: ""

		val sbText: String
			get() = blinds?.sb?.let { if (it == 0.0) "" else it.toLong().toString() } ?: ""

		fun chipToBb(chip: Double): Double {
			val bb = blinds?.bb ?: return chip
			return if (bb > 0) chip / bb else chip
		}

		fun bbToChip(bbCount: Double): Double {
			val bb = blinds?.bb ?: return bbCount
			return bbCount * bb
		}

		fun formatAmount(amount: Double): String =
			formatAmountFull(amount, useBbUnit, blinds?.bb ?: 0.0)

		fun parseInputToChip(input: String): Double {
			val parsed = input.toDoubleOrNull() ?: 0.0
			return if (useBbUnit) bbToChip(parsed) else parsed
		}

		fun chipToInput(chip: Double): String {
			if (chip == 0.0) return ""
			if (!useBbUnit) return chip.toLong().toString()
			val bbCount = chipToBb(chip)
			val rounded = (bbCount * 100).toLong() / 100.0
			return if (rounded == rounded.toLong().toDouble()) {
				rounded.toLong().toString()
			} else {
				rounded.toString()
			}
		}

		val canProceedFromSetup: Boolean
			get() {
				if (heroHand == null) return false
				if (table?.gameType is GameType.Tournament) {
					val bb = blinds?.bb ?: 0.0
					if (bb <= 0.0) return false
				}
				return true
			}

		val canProceed: Boolean
			get() = when (currentStep) {
				RecordStep.SETUP -> canProceedFromSetup
				RecordStep.SHOWDOWN -> !isSaving
				else -> true
			}

		val currentPot: Double
			get() = seatInvestments.values.sum() + anteAmount

		val potResults: List<PotResult>
			get() {
				val pots = sidePots
				if (pots.size <= 1) return emptyList()
				val results = showdownResults
				if (results.isEmpty()) return emptyList()

				val investments = buildMap {
					for (seat in occupiedSeats) {
						val player = players[seat] ?: continue
						val initial = player.initialStack ?: continue
						val invested = initial - player.stack
						if (invested > 0) put(seat, invested)
					}
				}

				val sortedLevels = investments.values.distinct().sorted()
				var previousLevel = 0.0
				val potResultList = mutableListOf<PotResult>()
				var potIndex = 0

				for (level in sortedLevels) {
					val diff = level - previousLevel
					if (diff <= 0) continue
					val eligibleSeats = investments.filter { it.value >= level }.keys
					val potForLevel = diff * eligibleSeats.size

					val eligibleRemaining = eligibleSeats.intersect(remainingSeats.toSet())
					val hasUnknownEligible = eligibleRemaining.any { seat ->
						players[seat]?.isCardsUnknown == true
					}
					val eligibleEntries = showdownEntries.filter {
						it.seat in eligibleSeats && players[it.seat]?.isCardsUnknown != true
					}
					val eligibleShowdown = if (!hasUnknownEligible && eligibleEntries.size >= 2 && streets.boardCards.size == 5) {
						HandEvaluator.calculateShowdown(streets.boardCards, eligibleEntries)
					} else {
						emptyList()
					}
					val winner = eligibleShowdown.firstOrNull { it.isWinner || it.isSplit }
						?: eligibleShowdown.firstOrNull()

					if (winner != null) {
						val label = if (potIndex == 0) "Main Pot" else "Side Pot${if (sortedLevels.size > 2) " $potIndex" else ""}"
						potResultList.add(PotResult(label, potForLevel, winner.seat))
					}
					previousLevel = level
					potIndex++
				}
				return potResultList
			}

		val sidePots: List<Double>
			get() {
				val seats = occupiedSeats
				val allInSeats = players.allInSeats
				if (allInSeats.isEmpty()) return emptyList()

				val investments = mutableMapOf<Int, Double>()
				for (seat in seats) {
					val player = players[seat] ?: continue
					val initial = player.initialStack ?: continue
					var invested = initial - player.stack
					if (seat == bbSeat && anteAmount > 0) {
						invested -= anteAmount
					}
					if (invested > 0) investments[seat] = invested
				}
				if (investments.size < 2) return emptyList()

				val sortedLevels = investments.values.distinct().sorted()
				var previousLevel = 0.0
				val pots = mutableListOf<Double>()

				for (level in sortedLevels) {
					val diff = level - previousLevel
					if (diff <= 0) continue
					val eligible = investments.count { it.value >= level }
					if (eligible <= 1) break
					val potForLevel = diff * eligible
					pots.add(potForLevel)
					previousLevel = level
				}

				if (pots.isNotEmpty() && anteAmount > 0) {
					pots[0] = pots[0] + anteAmount
				}

				return if (pots.size <= 1) emptyList() else pots
			}

		val preflopActionOrder: List<Int>
			get() = seatsInOrderAfter(bbSeat)

		val postflopActionOrder: List<Int>
			get() = seatsInOrderAfter(buttonSeat)

		private fun seatsInOrderAfter(seat: Int): List<Int> {
			val seats = occupiedSeats
			val idx = seats.indexOf(seat)
			if (idx < 0) return emptyList()
			return seats.indices.map { i -> seats[(idx + 1 + i) % seats.size] }
		}

		val actionOrder: List<Int>
			get() {
				val base = if (currentStreet == Street.PREFLOP) preflopActionOrder else postflopActionOrder
				return base.filter { it !in players.inactiveSeats }
			}

		val minRaiseAmount: Double
			get() {
				val bb = blinds?.bb ?: 0.0
				val streetActions = streets.getActions(currentStreet)

				val aggressiveActions = mutableListOf<Double>()
				var currentMax = if (currentStreet == Street.PREFLOP) bb else 0.0

				streetActions.forEach { action ->
					val amount = action.amount ?: 0.0
					if (amount > currentMax &&
						(action.type == ActionType.BET || action.type == ActionType.RAISE || action.type == ActionType.ALL_IN)
					) {
						aggressiveActions.add(amount)
						currentMax = amount
					}
				}

				return if (aggressiveActions.size >= 1) {
					val lastAmount = aggressiveActions.last()
					val previousAmount = if (aggressiveActions.size >= 2) {
						aggressiveActions[aggressiveActions.size - 2]
					} else {
						if (currentStreet == Street.PREFLOP) bb else 0.0
					}
					val raiseSize = lastAmount - previousAmount
					lastAmount + raiseSize
				} else if (currentStreet == Street.PREFLOP) {
					bb * 2
				} else {
					bb
				}
			}

		val currentBetLevel: Int
			get() {
				val streetActions = streets.getActions(currentStreet)
				var maxBet = if (currentStreet == Street.PREFLOP) (blinds?.bb ?: 0.0) else 0.0
				var raiseCount = 0
				streetActions.forEach { action ->
					val amt = action.amount ?: 0.0
					val isRaise = action.type == ActionType.BET ||
						action.type == ActionType.RAISE ||
						(action.type == ActionType.ALL_IN && amt > maxBet)
					if (isRaise) {
						raiseCount++
						maxBet = amt
					}
				}
				return if (currentStreet == Street.PREFLOP) {
					raiseCount + 1
				} else {
					raiseCount
				}
			}

		val nextRaiseLabel: String
			get() {
				val nextLevel = currentBetLevel + 1
				return when {
					currentStreet != Street.PREFLOP && currentBetLevel == 0 -> "벳"
					nextLevel == 2 -> "레이즈"
					else -> "${nextLevel}벳"
				}
			}

		val availableActions: List<ActionType>
			get() {
				val seat = currentActionSeat ?: return emptyList()
				val playerStack = getPlayerStack(seat)
				val streetActions = streets.getActions(currentStreet)

				val hasAggression = streetActions.any {
					it.type == ActionType.BET || it.type == ActionType.RAISE ||
						(it.type == ActionType.ALL_IN && (it.amount ?: 0.0) > 0)
				}

				val openingBet = if (currentStreet == Street.PREFLOP) (blinds?.bb ?: 0.0) else 0.0
				val maxBet = maxOf(openingBet, streetActions.maxOfOrNull { it.amount ?: 0.0 } ?: 0.0)
				val reachableBet = playerStack?.plus(streetContribution(seat))

				val canCall = reachableBet == null || reachableBet >= maxBet
				val lastAllInUnderMinRaise = run {
					val bb = blinds?.bb ?: 0.0
					val aggressiveAmounts = mutableListOf<Double>()
					var curMax = if (currentStreet == Street.PREFLOP) bb else 0.0
					var lastActionType: ActionType? = null
					streetActions.forEach { action ->
						val amt = action.amount ?: 0.0
						if (amt > curMax &&
							(action.type == ActionType.BET || action.type == ActionType.RAISE || action.type == ActionType.ALL_IN)
						) {
							aggressiveAmounts.add(amt)
							lastActionType = action.type
							curMax = amt
						}
					}
					if (lastActionType != ActionType.ALL_IN || aggressiveAmounts.size < 2) {
						false
					} else {
						val lastAmt = aggressiveAmounts.last()
						val prevAmt = aggressiveAmounts[aggressiveAmounts.size - 2]
						val prevRaiseSize = if (aggressiveAmounts.size >= 3) {
							prevAmt - aggressiveAmounts[aggressiveAmounts.size - 3]
						} else {
							prevAmt - (if (currentStreet == Street.PREFLOP) bb else 0.0)
						}
						val requiredMin = prevAmt + prevRaiseSize
						lastAmt < requiredMin
					}
				}
				val canRaise = (reachableBet == null || reachableBet >= minRaiseAmount) && !lastAllInUnderMinRaise

				return buildList {
					if (currentStreet == Street.PREFLOP) {
						if (seat == bbSeat && !hasAggression) {
							add(ActionType.CHECK)
							if (canRaise) add(ActionType.RAISE)
							add(ActionType.ALL_IN)
						} else {
							add(ActionType.FOLD)
							if (canCall) add(ActionType.CALL)
							if (canRaise) add(ActionType.RAISE)
							add(ActionType.ALL_IN)
						}
					} else {
						if (hasAggression) {
							add(ActionType.FOLD)
							if (canCall) {
								add(ActionType.CALL)
								if (canRaise) add(ActionType.RAISE)
							}
							add(ActionType.ALL_IN)
						} else {
							add(ActionType.CHECK)
							if (canRaise) add(ActionType.BET)
							add(ActionType.ALL_IN)
						}
					}
				}.distinct()
			}

		val heroPositionName: String
			get() = positionName(table?.heroSeat ?: 0)

		val allPositions: List<Position>
			get() = Position.forPlayerCount(occupiedSeats.size)

		val allPositionNames: List<String>
			get() = allPositions.map { it.label }

		fun buttonSeatForHeroPosition(position: String): Int {
			val heroSeat = table?.heroSeat ?: return 1
			for (btn in occupiedSeats) {
				val testState = copy(buttonSeat = btn)
				if (testState.positionName(heroSeat) == position) return btn
			}
			return buttonSeat
		}

		fun positionOf(seat: Int): Position {
			val seats = occupiedSeats
			val count = seats.size
			if (count == 0) return Position.BTN
			val btnIdx = seats.indexOf(buttonSeat).takeIf { it >= 0 }
				?: seats.indexOf(seats.firstOrNull { it >= buttonSeat } ?: seats.first())
			if (btnIdx < 0) return Position.BTN
			if (count == 2) return if (seat == seats[btnIdx]) Position.BTN else Position.BB

			val sbSeat = seatAtOffset(btnIdx, 1)
			val bbSeat = seatAtOffset(btnIdx, 2)

			if (seat == buttonSeat) return Position.BTN
			if (seat == sbSeat) return Position.SB
			if (seat == bbSeat) return Position.BB

			val utgOrder = preflopActionOrder.filter { it != buttonSeat && it != sbSeat && it != bbSeat }
			val idx = utgOrder.indexOf(seat)
			return when {
				idx == 0 -> Position.UTG
				idx == utgOrder.lastIndex -> Position.CO
				count <= 6 -> Position.MP
				idx == utgOrder.lastIndex - 1 -> Position.HJ
				idx == utgOrder.lastIndex - 2 -> Position.LJ
				idx == 1 -> Position.UTG1
				else -> Position.MP
			}
		}

		fun positionName(seat: Int): String = positionOf(seat).label

		fun withInitialStack(seat: Int, totalStack: Double?): Recording = copy(
			players = players.update(seat) {
				copy(
					stack = totalStack?.let { (it - committedChips(seat)).coerceAtLeast(0.0) } ?: 0.0,
					initialStack = totalStack,
				)
			},
		)

		fun advancedToNextStep(): Recording {
			val next = RecordStep.entries.getOrNull(currentStep.ordinal + 1) ?: return this
			val stepped = copy(
				currentStep = next,
				currentActionType = null,
				currentActionAmount = "",
				currentActionChipAmount = null,
			)
			if (next == RecordStep.SHOWDOWN) return stepped.copy(currentActionSeat = null)
			val entered = stepped.copy(streets = streets.ensureStreet(stepped.currentStreet))
			val rebuilt = entered.copy(players = entered.playersFromActionLog)
			return rebuilt.copy(
				currentActionSeat = if (rebuilt.currentStreet == Street.PREFLOP) null else rebuilt.actionOrder.firstOrNull(),
			)
		}

		fun rewoundTo(targetStep: RecordStep): Recording {
			val target = copy(
				currentStep = targetStep,
				currentActionType = null,
				currentActionAmount = "",
				currentActionChipAmount = null,
			)
			val clearedStreets = if (targetStep == RecordStep.SETUP) {
				streets.clearActions()
			} else {
				streets.clearActionsAfter(target.currentStreet)
			}
			val rewound = target.copy(streets = clearedStreets)
			val heroSeat = table?.heroSeat
			val rebuiltPlayers = occupiedSeats.fold(rewound.playersFromActionLog) { rebuilt, seat ->
				rebuilt.update(
					seat,
				) { copy(cards = if (seat == heroSeat) cards else null, isCardsUnknown = false) }
			}
			return rewound.copy(
				players = rebuiltPlayers,
				currentActionSeat = clearedStreets.getActions(rewound.currentStreet).lastOrNull()?.playerSeat,
			)
		}

		fun withLastActionUndone(): Recording {
			val removed = streets.getActions(currentStreet).lastOrNull() ?: return this
			val undone = copy(streets = streets.removeLastAction(currentStreet))
			return undone.copy(players = undone.playersFromActionLog, currentActionSeat = removed.playerSeat)
		}
	}
}

@Immutable
data class ActionPresets(
	val preflopPresets: List<Double> = listOf(2.0, 2.5, 3.0, 4.0, 5.0),
	val postflopPresets: List<Int> = listOf(33, 50, 75, 100),
)

@Immutable
data class PotResult(
	val label: String,
	val amount: Double,
	val winnerSeat: Int,
)

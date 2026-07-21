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
		// Setup
		val buttonSeat: Int = 1,
		val blinds: Blinds? = null,
		// Players (통합: 스택/상태/카드/쇼다운 모두 포함)
		val players: RecordPlayers = RecordPlayers(),
		// Streets
		val streets: HandStreets = HandStreets(),
		// Current action being built
		val currentActionSeat: Int? = null,
		val currentActionType: ActionType? = null,
		val currentActionAmount: String = "",
		val currentActionChipAmount: Double? = null,
		val lastAggressorSeat: Int? = null,
		// Showdown
		val resolvedShowdown: ResolvedShowdown? = null,
		val memo: String = "",
		// UI state
		val currentStep: RecordStep = RecordStep.SETUP,
		val useBbUnit: Boolean = false,
		val actionPresets: ActionPresets = ActionPresets(),
	) : RecordHandState {

		/** 히어로의 핸드 (players에서 조회) */
		val heroHand: PocketCards?
			get() = players[table?.heroSeat ?: 0]?.cards

		/** 현재 사용 중인 모든 카드 (플레이어 카드 + 보드) */
		val selectedCards: Set<Card>
			get() = buildSet {
				addAll(players.allCards)
				addAll(streets.boardCards)
			}

		/** 표시할 스텝 목록 */
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

		// --- Player 상태 조회 ---

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

		/** 헤즈업(2인): BTN = SB, 3인 이상: BTN 다음 좌석 = SB */
		val sbSeat: Int
			get() {
				val seats = occupiedSeats
				val btnIdx = seats.indexOf(buttonSeat)
				if (btnIdx < 0 || seats.size < 2) return 0
				return if (seats.size == 2) seats[btnIdx] else seatAtOffset(btnIdx, 1)
			}

		/** 헤즈업(2인): BTN 반대편 = BB, 3인 이상: BTN+2 좌석 = BB */
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
				val player = players[heroSeat] ?: return 0.0
				return player.stack - (player.initialStack ?: return 0.0)
			}

		fun getPlayerStack(seat: Int): Double? = players.getStack(seat)

		fun getBlindCost(seat: Int): Double {
			val sb = blinds?.sb ?: 0.0
			val bb = blinds?.bb ?: 0.0
			return when (seat) {
				sbSeat -> sb
				bbSeat -> bb + if (blinds?.isBigBlindAnte == true) bb else 0.0
				else -> 0.0
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

		/**
		 * 남은 플레이어가 2명 이상이면서 올인하지 않은(계속 액션 가능한) 좌석이 1명 이하인 상태.
		 * 더 이상 베팅이 불가능하므로 남은 스트릿을 진행하지 않고 곧바로 쇼다운으로 넘어가야 한다.
		 */
		val isAllInShowdown: Boolean
			get() {
				val remaining = remainingSeats
				if (remaining.size < 2) return false
				return remaining.count { it !in players.allInSeats } <= 1
			}

		// --- Showdown Results ---

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
				// 승패를 비교하려면 최소 두 명의 핸드가 공개되어야 한다.
				// 히어로만 공개된 상태(상대 전원 미공개)에서는 결과를 내지 않는다.
				if (knownEntries.size < 2) return emptyList()

				// 공개된 핸드끼리만 비교해 실제 순위대로 승패를 판정한다.
				// 미공개(머크) 플레이어는 팟 경쟁에서 빠지므로 결과에 포함하지 않는다.
				return HandEvaluator.calculateShowdown(streets.boardCards, knownEntries)
			}

		// --- Blinds ---

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

		// --- Pot ---

		val currentPot: Double
			get() {
				val actionsPot = streets.totalActionAmount()
				val sb = blinds?.sb ?: 0.0
				val bb = blinds?.bb ?: 0.0
				val preflopActions = streets.getActions(Street.PREFLOP)
				val sbInPot = preflopActions.filter { it.playerSeat == sbSeat }.lastOrNull()?.amount ?: 0.0
				val bbInPot = preflopActions.filter { it.playerSeat == bbSeat }.lastOrNull()?.amount ?: 0.0
				val blindsPot = (if (sbInPot >= sb) 0.0 else sb) + (if (bbInPot >= bb) 0.0 else bb)
				val antePot = if (blinds?.isBigBlindAnte == true) bb else 0.0
				return actionsPot + blindsPot + antePot
			}

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

				val anteAmount = if (blinds?.isBigBlindAnte == true) (blinds?.bb ?: 0.0) else 0.0

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

		// --- Action Order ---

		val preflopActionOrder: List<Int>
			get() {
				val seats = occupiedSeats
				val btnIdx = seats.indexOf(buttonSeat)
				if (btnIdx < 0 || seats.isEmpty()) return emptyList()
				val count = seats.size
				return (0 until count).map { i -> seats[(btnIdx + 3 + i) % count] }
			}

		val postflopActionOrder: List<Int>
			get() {
				val seats = occupiedSeats
				val btnIdx = seats.indexOf(buttonSeat)
				if (btnIdx < 0 || seats.isEmpty()) return emptyList()
				val count = seats.size
				return (0 until count).map { i -> seats[(btnIdx + 1 + i) % count] }
			}

		val actionOrder: List<Int>
			get() {
				val base = if (currentStreet == Street.PREFLOP) preflopActionOrder else postflopActionOrder
				return base.filter { it !in players.inactiveSeats }
			}

		// --- Min Raise ---

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

		// --- Available Actions ---

		val availableActions: List<ActionType>
			get() {
				val seat = currentActionSeat ?: return emptyList()
				val playerStack = getPlayerStack(seat)
				val streetActions = streets.getActions(currentStreet)
				val seats = occupiedSeats
				val btnIdx = seats.indexOf(buttonSeat)
				val bbSeat = if (btnIdx >= 0 && seats.size >= 3) seatAtOffset(btnIdx, 2) else 0

				val hasAggression = streetActions.any {
					it.type == ActionType.BET || it.type == ActionType.RAISE ||
						(it.type == ActionType.ALL_IN && (it.amount ?: 0.0) > 0)
				}

				val maxBet = streetActions.maxOfOrNull { it.amount ?: 0.0 }
					?: if (currentStreet == Street.PREFLOP) (blinds?.bb ?: 0.0) else 0.0

				val canCall = playerStack == null || playerStack >= maxBet
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
				val canRaise = (playerStack == null || playerStack >= minRaiseAmount) && !lastAllInUnderMinRaise

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

		// --- Position ---

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
	}
}

@Immutable
data class ResolvedShowdown(
	val results: List<ShowdownResult> = emptyList(),
	val potResults: List<PotResult> = emptyList(),
	val heroResult: Double = 0.0,
)

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

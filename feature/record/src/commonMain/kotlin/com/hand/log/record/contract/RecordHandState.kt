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
import com.hand.log.record.model.RecordShowdown
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
		val heroHand: PocketCards? = null,
		val buttonSeat: Int = 1,
		val blinds: Blinds? = null,
		// Players
		val players: RecordPlayers = RecordPlayers(),
		// Streets
		val streets: HandStreets = HandStreets(),
		// Current action being built
		val currentActionSeat: Int? = null,
		val currentActionType: ActionType? = null,
		val currentActionAmount: String = "",
		val lastAggressorSeat: Int? = null,
		// Showdown
		val showdown: RecordShowdown = RecordShowdown(),
		// Result
		val memo: String = "",
		// UI state
		val currentStep: RecordStep = RecordStep.SETUP,
		val useBbUnit: Boolean = false,
		val preflopPresets: List<Double> = listOf(2.0, 2.5, 3.0, 4.0, 5.0),
		val postflopPresets: List<Int> = listOf(33, 50, 75, 100),
		val showAmountWarning: Boolean = false,
		val isEditing: Boolean = false,
	) : RecordHandState {

		/** 현재 사용 중인 모든 카드 (히어로 + 보드 + 쇼다운) */
		val selectedCards: Set<Card>
			get() = buildSet {
				heroHand?.let {
					add(it.card1)
					add(it.card2)
				}
				addAll(streets.boardCards)
				addAll(showdown.allCards)
			}

		/** 표시할 스텝 목록: 쇼다운 진입 전에는 전체, 쇼다운 이후에는 실제 진행된 스텝만 */
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

		// --- Current Street (currentStep에서 파생) ---
		val currentStreet: Street
			get() = when (currentStep) {
				RecordStep.SETUP, RecordStep.PREFLOP -> Street.PREFLOP
				RecordStep.FLOP -> Street.FLOP
				RecordStep.TURN -> Street.TURN
				RecordStep.RIVER, RecordStep.SHOWDOWN -> Street.RIVER
			}

		// --- Player 상태 조회 ---

		val heroStack: Double
			get() = players.getStack(table?.heroSeat ?: 0)

		/** 히어로의 수익/손실 (현재 스택 - 초기 스택) */
		val heroResult: Double
			get() {
				val heroSeat = table?.heroSeat ?: return 0.0
				val player = players[heroSeat] ?: return 0.0
				return player.stack - player.initialStack
			}

		fun getPlayerStack(seat: Int): Double = players.getStack(seat)

		/** 좌석의 프리플랍 블라인드 차감액 (SB/BB/Ante) */
		fun getBlindCost(seat: Int): Double {
			val count = table?.playerCount ?: return 0.0
			val btn = buttonSeat
			val sbSeat = (btn % count) + 1
			val bbSeat = ((btn + 1) % count) + 1
			val sb = blinds?.sb ?: 0.0
			val bb = blinds?.bb ?: 0.0
			return when (seat) {
				sbSeat -> sb
				bbSeat -> bb + if (blinds?.isBigBlindAnte == true) bb else 0.0
				else -> 0.0
			}
		}

		/** 폴드 승리 여부 (1명만 남은 경우) */
		val isFoldWin: Boolean
			get() = remainingSeats.size == 1

		/** 팟에 참여 중인(폴드 안 한) 좌석 목록 */
		val remainingSeats: List<Int>
			get() {
				val count = table?.playerCount ?: return emptyList()
				return (1..count).filter { it !in players.foldedSeats }
			}

		// --- Showdown Results ---
		/** 쇼다운 엔트리 (카드가 공개된 플레이어) */
		val showdownEntries: List<ShowdownEntry>
			get() = buildList {
				remainingSeats.forEach { seat ->
					val hand = if (seat == table?.heroSeat) heroHand else showdown[seat]
					if (hand != null) {
						add(ShowdownEntry(seat = seat, cards = hand))
					}
				}
			}

		/** 모든 남은 플레이어의 카드가 선택 또는 미공개 처리되었는지 */
		val isShowdownComplete: Boolean
			get() = remainingSeats.all { seat ->
				if (seat == table?.heroSeat) {
					heroHand != null
				} else {
					showdown.isResolved(seat)
				}
			}

		/** 쇼다운 결과 (폴드 승리 또는 핸드 비교) */
		val showdownResults: List<ShowdownResult>
			get() {
				// 1명만 남으면 폴드 승리 — 승자 + 폴드한 플레이어 모두 표시
				if (remainingSeats.size == 1) {
					val winnerSeat = remainingSeats.first()
					val count = table?.playerCount ?: return emptyList()
					return (1..count).map { seat ->
						ShowdownResult(
							seat = seat,
							ranking = HandRanking.HIGH_CARD,
							bestCards = emptyList(),
							outcome = if (seat == winnerSeat) ShowdownOutcome.WIN else ShowdownOutcome.LOSE,
						)
					}
				}

				if (!isShowdownComplete || streets.boardCards.size != 5) return emptyList()
				val knownEntries = showdownEntries.filter { !showdown.isUnknown(it.seat) }
				if (knownEntries.size < 2) return emptyList()

				val hasUnknownPlayers = remainingSeats.any { seat ->
					seat != table?.heroSeat && showdown.isUnknown(seat)
				}
				val results = HandEvaluator.calculateShowdown(streets.boardCards, knownEntries)
				if (hasUnknownPlayers) {
					// 미공개 플레이어가 있으면 승자 판정 불가 — 족보만 표시
					return results.map { it.copy(outcome = ShowdownOutcome.LOSE) }
				}
				return results
			}

		// --- Blinds ---

		val bbText: String
			get() = blinds?.bb?.let { if (it == 0.0) "" else it.toLong().toString() } ?: ""

		val sbText: String
			get() = blinds?.sb?.let { if (it == 0.0) "" else it.toLong().toString() } ?: ""

		/** 칩 금액 → BB 수 */
		fun chipToBb(chip: Double): Double {
			val bb = blinds?.bb ?: return chip
			return if (bb > 0) chip / bb else chip
		}

		/** BB 수 → 칩 금액 */
		fun bbToChip(bbCount: Double): Double {
			val bb = blinds?.bb ?: return bbCount
			return bbCount * bb
		}

		/** 칩 금액을 표시용 문자열로 변환 (BB 모드면 BB 단위) */
		fun formatAmount(amount: Double): String =
			formatAmountFull(amount, useBbUnit, blinds?.bb ?: 0.0)

		/** 입력 문자열(BB 또는 칩)을 칩 금액으로 변환 */
		fun parseInputToChip(input: String): Double {
			val parsed = input.toDoubleOrNull() ?: 0.0
			return if (useBbUnit) bbToChip(parsed) else parsed
		}

		/** 칩 금액을 입력 문자열로 변환 (BB 모드면 BB 수) */
		fun chipToInput(chip: Double): String {
			if (chip == 0.0) return ""
			if (!useBbUnit) return chip.toLong().toString()
			val bbCount = chipToBb(chip)
			val rounded = (bbCount * 10).toLong() / 10.0
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

		/** 팟 = 각 스트릿에서 투입된 총 금액 + 블라인드 + 앤티 */
		val currentPot: Double
			get() {
				val count = table?.playerCount ?: 0
				// 각 스트릿에서 각 좌석의 최종 투입 금액 합산
				val actionsPot = streets.totalActionAmount()
				// 블라인드: 프리플랍 액션에 아직 SB/BB가 포함 안 될 수 있음
				val sb = blinds?.sb ?: 0.0
				val bb = blinds?.bb ?: 0.0
				val btn = buttonSeat
				val sbSeat = if (count > 0) (btn % count) + 1 else 0
				val bbSeat = if (count > 0) ((btn + 1) % count) + 1 else 0
				// 프리플랍에서 SB/BB의 현재 베팅이 블라인드보다 크면 이미 액션에 포함됨
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

				val count = table?.playerCount ?: return emptyList()
				val investments = buildMap {
					for (seat in 1..count) {
						val player = players[seat] ?: continue
						val invested = player.initialStack - player.stack
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

					// eligible 플레이어끼리 핸드 비교하여 팟별 승자 결정
					val eligibleRemaining = eligibleSeats.intersect(remainingSeats.toSet())
					val hasUnknownEligible = eligibleRemaining.any { seat ->
						seat != table?.heroSeat && showdown.isUnknown(seat)
					}
					val eligibleEntries = showdownEntries.filter {
						it.seat in eligibleSeats && !showdown.isUnknown(it.seat)
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

		/** 사이드 팟 목록 (올인 플레이어가 있을 때 발생) */
		val sidePots: List<Double>
			get() {
				val count = table?.playerCount ?: return emptyList()
				val allInSeats = players.allInSeats
				if (allInSeats.isEmpty()) return emptyList()

				// BBA ante는 BB의 투자금에서 분리하여 메인팟에 별도 추가
				val bbSeat = ((buttonSeat + 1) % count) + 1
				val anteAmount = if (blinds?.isBigBlindAnte == true) (blinds?.bb ?: 0.0) else 0.0

				val investments = mutableMapOf<Int, Double>()
				for (seat in 1..count) {
					val player = players[seat] ?: continue
					var invested = player.initialStack - player.stack
					// BB의 ante는 별도 처리하므로 투자금에서 제외
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
					if (eligible <= 1) break // 1명만 eligible하면 미콜 베팅 → 반환
					val potForLevel = diff * eligible
					pots.add(potForLevel)
					previousLevel = level
				}

				// ante를 메인팟에 추가
				if (pots.isNotEmpty() && anteAmount > 0) {
					pots[0] = pots[0] + anteAmount
				}

				return if (pots.size <= 1) emptyList() else pots
			}

		// --- Action Order ---

		val preflopActionOrder: List<Int>
			get() {
				val count = table?.playerCount ?: return emptyList()
				val btn = buttonSeat
				return (1..count).map { offset ->
					((btn + 2 + offset - 1) % count) + 1
				}
			}

		val postflopActionOrder: List<Int>
			get() {
				val count = table?.playerCount ?: return emptyList()
				val btn = buttonSeat
				return (1..count).map { offset ->
					((btn + offset - 1) % count) + 1
				}
			}

		val actionOrder: List<Int>
			get() {
				val base = if (currentStreet == Street.PREFLOP) preflopActionOrder else postflopActionOrder
				return base.filter { it !in players.inactiveSeats }
			}

		// --- Min Raise ---

		/** 최소 레이즈/벳 금액 */
		val minRaiseAmount: Double
			get() {
				val bb = blinds?.bb ?: 0.0
				val streetActions = streets.getActions(currentStreet)

				// 베팅/레이즈/올인(금액이 이전 최대보다 큰) 액션들을 추적
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

		/**
		 * 현재 스트릿의 레이즈 횟수.
		 * 프리플랍: BB=1벳, 오픈레이즈=2벳, 3벳, 4벳...
		 * 포스트플랍: 오픈벳=1벳, 레이즈=2벳, 3벳...
		 */
		val currentBetLevel: Int
			get() {
				val streetActions = streets.getActions(currentStreet)
				// 올인이 이전 최대 베팅보다 클 때만 레이즈로 카운트
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
					raiseCount + 1 // BB가 1벳
				} else {
					raiseCount
				}
			}

		/** 다음 레이즈의 표시 이름 (3벳, 4벳 등) */
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
				val count = table?.playerCount ?: return ActionType.entries
				val btn = buttonSeat
				val bbSeat = ((btn + 1) % count) + 1

				// 베팅/레이즈/올인(금액 있는) 여부
				val hasAggression = streetActions.any {
					it.type == ActionType.BET || it.type == ActionType.RAISE ||
						(it.type == ActionType.ALL_IN && (it.amount ?: 0.0) > 0)
				}

				// 현재 매칭해야 할 금액 (콜 금액)
				val maxBet = streetActions.maxOfOrNull { it.amount ?: 0.0 }
					?: if (currentStreet == Street.PREFLOP) (blinds?.bb ?: 0.0) else 0.0

				// 스택 미입력(0) = 무제한
				val isUnlimited = playerStack == 0.0
				// 콜 가능 여부
				val canCall = isUnlimited || playerStack >= maxBet
				// 마지막 올인이 민레이즈 미달이면 리오픈 불가 (콜/폴드만)
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
				// 민레이즈 이상 가능 여부
				val canRaise = (isUnlimited || playerStack >= minRaiseAmount) && !lastAllInUnderMinRaise

				return buildList {
					if (currentStreet == Street.PREFLOP) {
						if (seat == bbSeat && !hasAggression) {
							// BB 옵션: 체크 or 레이즈 or 올인
							add(ActionType.CHECK)
							if (canRaise) add(ActionType.RAISE)
							add(ActionType.ALL_IN)
						} else {
							// 프리플랍 기본
							add(ActionType.FOLD)
							if (canCall) add(ActionType.CALL)
							if (canRaise) add(ActionType.RAISE)
							add(ActionType.ALL_IN)
						}
					} else {
						// 포스트플랍
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

		/** 히어로의 현재 포지션 이름 */
		val heroPositionName: String
			get() = positionName(table?.heroSeat ?: 0)

		val allPositions: List<Position>
			get() = Position.forPlayerCount(table?.playerCount ?: 0)

		val allPositionNames: List<String>
			get() = allPositions.map { it.label }

		/** 포지션 이름으로 buttonSeat 설정 (히어로가 해당 포지션이 되도록) */
		fun buttonSeatForHeroPosition(position: String): Int {
			val count = table?.playerCount ?: return 1
			val heroSeat = table.heroSeat
			// buttonSeat를 1~count까지 시도해서 heroSeat의 positionName이 일치하는 값을 찾음
			for (btn in 1..count) {
				val testState = copy(buttonSeat = btn)
				if (testState.positionName(heroSeat) == position) return btn
			}
			return buttonSeat
		}

		fun positionOf(seat: Int): Position {
			val count = table?.playerCount ?: return Position.BTN
			val btn = buttonSeat
			val sbSeat = (btn % count) + 1
			val bbSeat = ((btn + 1) % count) + 1

			if (seat == btn) return Position.BTN
			if (seat == sbSeat) return Position.SB
			if (seat == bbSeat) return Position.BB

			val utgOrder = preflopActionOrder.filter { it != btn && it != sbSeat && it != bbSeat }
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
data class PotResult(
	val label: String,
	val amount: Double,
	val winnerSeat: Int,
)

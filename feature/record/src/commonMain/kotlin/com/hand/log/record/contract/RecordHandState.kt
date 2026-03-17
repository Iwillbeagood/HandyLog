package com.hand.log.record.contract

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import com.hand.log.domain.model.ActionType
import com.hand.log.domain.model.Blinds
import com.hand.log.domain.model.Card
import com.hand.log.domain.model.GameType
import com.hand.log.domain.model.HeroHand
import com.hand.log.domain.model.Position
import com.hand.log.domain.model.PokerTable
import com.hand.log.domain.model.Street
import com.hand.log.record.model.RecordPlayers
import com.hand.log.record.model.RecordStreets

@Stable
internal sealed interface RecordHandState {

	@Immutable
	data object Loading : RecordHandState

	@Immutable
	data class Recording(
		val tableId: String,
		val table: PokerTable? = null,
		// Setup
		val heroHand: HeroHand? = null,
		val buttonSeat: Int = 1,
		val blinds: Blinds? = null,
		// Players
		val players: RecordPlayers = RecordPlayers(),
		// Streets
		val streets: RecordStreets = RecordStreets(),
		val currentStreet: Street = Street.PREFLOP,
		// Current action being built
		val currentActionSeat: Int? = null,
		val currentActionType: ActionType? = null,
		val currentActionAmount: String = "",
		val lastAggressorSeat: Int? = null,
		// Result
		val result: String = "",
		val memo: String = "",
		// UI state
		val currentStep: RecordStep = RecordStep.SETUP,
		val selectedCards: Set<Card> = emptySet(),
	) : RecordHandState {

		// --- Player 상태 조회 ---

		val heroStack: Double
			get() = players.getStack(table?.heroSeat ?: 0)

		fun getPlayerStack(seat: Int): Double = players.getStack(seat)

		val foldedSeats: Set<Int> get() = players.foldedSeats
		val allInSeats: Set<Int> get() = players.allInSeats
		val inactiveSeats: Set<Int> get() = players.inactiveSeats

		// --- Blinds ---

		val bbText: String
			get() = blinds?.bb?.let { if (it == 0.0) "" else it.toLong().toString() } ?: ""

		val sbText: String
			get() = blinds?.sb?.let { if (it == 0.0) "" else it.toLong().toString() } ?: ""

		val heroCards: List<Card> get() = heroHand?.cards ?: emptyList()

		val canProceedFromSetup: Boolean
			get() {
				if (heroHand == null) return false
				if (table?.gameType == GameType.TOURNAMENT) {
					val bb = blinds?.bb ?: 0.0
					if (bb <= 0.0) return false
				}
				return true
			}

		// --- Pot ---

		val currentPot: Double
			get() {
				val blindsPot = (blinds?.sb ?: 0.0) + (blinds?.bb ?: 0.0)
				val antePot = if (blinds?.isBigBlindAnte == true) blinds.bb else 0.0
				val actionsPot = streets.totalActionAmount()
				return blindsPot + antePot + actionsPot
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
				return base.filter { it !in inactiveSeats }
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
				val raiseCount = streetActions.count {
					it.type == ActionType.BET || it.type == ActionType.RAISE ||
						(it.type == ActionType.ALL_IN && (it.amount ?: 0.0) > 0)
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

				// 콜 가능 여부
				val canCall = playerStack >= maxBet
				// 민레이즈 이상 가능 여부
				val canRaise = playerStack >= minRaiseAmount

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
							if (!canCall) {
								// 콜 불가(스택 부족) → 올인 or 폴드만
								add(ActionType.ALL_IN)
							} else {
								add(ActionType.ALL_IN)
							}
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

enum class RecordStep {
	SETUP,
	PREFLOP,
	FLOP,
	TURN,
	RIVER,
	;

	val label: String
		get() = when (this) {
			SETUP -> "설정"
			PREFLOP -> "프리플랍"
			FLOP -> "플랍"
			TURN -> "턴"
			RIVER -> "리버"
		}
}

@Immutable
internal sealed interface CardSelectorTarget {
	val maxCards: Int
	data class HeroCard(override val maxCards: Int = 2) : CardSelectorTarget
	data class BoardCard(val street: Street, override val maxCards: Int) : CardSelectorTarget
}

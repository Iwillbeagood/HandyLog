package com.hand.log.domain.model.quick

import com.hand.log.domain.model.Action
import com.hand.log.domain.model.ActionType
import com.hand.log.domain.model.Card
import com.hand.log.domain.model.HandPlayer
import com.hand.log.domain.model.HandRanking
import com.hand.log.domain.model.HandRecord
import com.hand.log.domain.model.HandStreets
import com.hand.log.domain.model.PocketCards
import com.hand.log.domain.model.Position
import com.hand.log.domain.model.Rank
import com.hand.log.domain.model.SeatRing
import com.hand.log.domain.model.ShowdownEntry
import com.hand.log.domain.model.ShowdownOutcome
import com.hand.log.domain.model.ShowdownResult
import com.hand.log.domain.model.Street
import com.hand.log.domain.model.Suit
import kotlin.math.roundToLong

data class QuickHandSnapshot(
	val record: HandRecord,
	val phase: QuickPhase,
	val street: Street,
	val pending: QuickPending?,
	val allowed: Set<QuickInput>,
	val canSave: Boolean,
	val positions: List<Position>,
	val involvedPositions: List<Position>,
	val heroPosition: Position?,
	val actorPosition: Position?,
	val openStreet: Street?,
	val isShowdownOpen: Boolean,
	val pendingShowdownSeats: Set<Int>,
	val finish: QuickFinish?,
	val amountInChips: Boolean,
	val heroCards: List<Card>,
	val opponentHands: List<QuickShowdownHand>,
	val usedCards: Set<Card>,
	val boardSlots: Int,
	val isBetOpen: Boolean = false,
)

class QuickHandMachine(private val config: QuickHandConfig) {

	fun append(inputs: List<QuickInput>, input: QuickInput): List<QuickInput> {
		val base = reselectBase(inputs)
		return if (base != null && input is QuickInput.Seat && input in reselectableSeats(base)) {
			base + input
		} else {
			inputs + input
		}
	}

	fun withHero(inputs: List<QuickInput>, position: Position): List<QuickInput> {
		val heroIndex = inputs.indexOfFirst { it is QuickInput.Hero }
		val candidate = when {
			heroIndex >= 0 -> inputs.toMutableList().apply { set(heroIndex, QuickInput.Hero(position)) }
			builderFor(inputs).phase == QuickPhase.REVIEW -> appendFinish(inputs, QuickInput.Seat(position))
			else -> listOf(QuickInput.Hero(position)) + inputs
		}
		return candidate.takeIf { builderFor(it).heroPosition == position } ?: inputs
	}

	fun replay(inputs: List<QuickInput>): QuickHandSnapshot {
		val builder = builderFor(inputs)
		val afterLastCard = if (builder.isLastBoardCardPending) {
			builderFor(inputs).apply { commitPendingCard() }.allowedInputs()
		} else {
			emptySet()
		}
		val allowed = builder.allowedInputs() + afterLastCard + reselectBase(inputs)?.let(::reselectableSeats).orEmpty()
		return QuickHandSnapshot(
			record = builder.toRecord(),
			phase = builder.phase,
			street = builder.street,
			pending = builder.pending(),
			allowed = allowed,
			canSave = builder.canSave,
			positions = builder.positions,
			involvedPositions = builder.involvedPositions,
			heroPosition = builder.heroPosition,
			actorPosition = builder.actorPosition,
			openStreet = builder.openStreet,
			isShowdownOpen = builder.isShowdownOpen,
			pendingShowdownSeats = builder.pendingShowdownSeats,
			finish = builder.finish,
			amountInChips = builder.amountInChips,
			heroCards = builder.heroHand,
			opponentHands = builder.opponentHands,
			usedCards = builder.usedCards,
			boardSlots = builder.boardSlots,
			isBetOpen = builder.isBetOpen,
		)
	}

	fun appendFinish(inputs: List<QuickInput>, input: QuickInput): List<QuickInput> {
		val base = withoutFinish(inputs)
		return when (input) {
			is QuickInput.Seat -> base + input
			is QuickInput.FillCards -> base + inputs.drop(base.size).filterNot {
				it is QuickInput.FillCards && it.owner == input.owner
			} + input
			else -> inputs
		}
	}

	fun withCards(inputs: List<QuickInput>, input: QuickInput.FillCards): List<QuickInput> = when {
		builderFor(inputs).phase == QuickPhase.REVIEW -> appendFinish(inputs, input)
		input.owner == CardOwner.Board -> inputs + input
		else -> inputs.filterNot { it is QuickInput.FillCards && it.owner == input.owner } + input
	}

	fun reopenActions(inputs: List<QuickInput>): List<QuickInput> = withoutFinish(inputs).dropLast(1)

	private fun withoutFinish(inputs: List<QuickInput>): List<QuickInput> {
		val builder = Builder(config, buttonFor(inputs))
		inputs.forEachIndexed { index, input ->
			if (builder.phase == QuickPhase.REVIEW) return inputs.take(index)
			builder.apply(input)
		}
		return inputs
	}

	fun build(
		inputs: List<QuickInput>,
		evaluator: (List<Card>, List<ShowdownEntry>) -> List<ShowdownResult>,
	): HandRecord? {
		val builder = builderFor(inputs)
		if (!builder.canSave) return null
		return builder.toFinalRecord(evaluator)
	}

	private fun reselectBase(inputs: List<QuickInput>): List<QuickInput>? {
		if (inputs.lastOrNull() !is QuickInput.Seat) return null
		val base = inputs.dropLast(1)
		val builder = builderFor(base)
		val pending = builder.pending()
		val isFresh = pending is QuickPending.Actor ||
			(pending is QuickPending.PlayerCards && pending.cards.isEmpty() && pending.rank == null)
		return base.takeIf { builder.phase == QuickPhase.ACTION && isFresh }
	}

	private fun reselectableSeats(base: List<QuickInput>): Set<QuickInput> =
		builderFor(base).allowedInputs().filterIsInstance<QuickInput.Seat>().toSet()

	private fun buttonFor(inputs: List<QuickInput>): Int? {
		val probe = Builder(config, buttonSeat = null).apply { inputs.forEach(::apply) }
		val heroPosition = probe.heroPosition ?: return null
		val seats = config.seats.distinct().sorted()
		return SeatRing(
			seats,
			seats.first(),
			config.tableId == null,
		).buttonForPosition(config.heroSeat, heroPosition)
	}

	private fun builderFor(inputs: List<QuickInput>): Builder =
		Builder(config, buttonFor(inputs)).apply { inputs.forEach(::apply) }

	private class Builder(private val config: QuickHandConfig, buttonSeat: Int?) {
		private val seats = config.seats.distinct().sorted()
		private val bb = config.blinds.bb.takeIf { it > 0 } ?: 1.0
		private val isStackKnown = config.stackInBb != null
		private val startStack = (config.stackInBb ?: UNKNOWN_STACK_BB) * bb
		private val buttonSeat = buttonSeat ?: seats.firstOrNull() ?: 1

		var phase = QuickPhase.ACTION
			private set
		var street = Street.PREFLOP
			private set
		var heroPosition: Position? = null
			private set

		private var streets = HandStreets()
		private val heroCards = mutableListOf<Card>()
		private val opponentCards = mutableMapOf<Int, MutableList<Card>>()
		private val boardBuffer = mutableListOf<Card>()
		private var pendingRank: Rank? = null

		private val stacks = seats.associateWith { startStack }.toMutableMap()
		private val topUps = mutableMapOf<Int, Double>()
		private val streetBets = mutableMapOf<Int, Double>()
		private val folded = mutableSetOf<Int>()
		private val allIn = mutableSetOf<Int>()
		private val acted = mutableSetOf<Int>()
		private val explicitPositions = mutableSetOf<Position>()
		private var raiseCount = 0
		private var currentActor: Int? = null
			set(value) {
				field = value
				isActorChosen = false
				cardSeat = null
			}
		private var isActorChosen = false
		private var cardSeat: Int? = null

		private var pendingType: ActionType? = null
		private var amountText = ""
		private var pendingPotSize: BetSize? = null

		private var unknownShoveLevel: Double? = null
		private var hasUnknownAmount = false
		private var lastRaiseSize = bb
		private var isRaiseCapped = false
		private var isMaxBetAllIn = false

		private var heroFromReview = false

		var amountInChips = config.startInChips
			private set

		private val canToggleAmountUnit: Boolean
			get() = phase != QuickPhase.REVIEW

		init {
			startPreflop()
		}

		private val ring: SeatRing get() = SeatRing(seats, buttonSeat, config.tableId == null)

		val positions: List<Position>
			get() = ring.preflopOrder.map { ring.positionOf(it) }

		val involvedPositions: List<Position>
			get() = positions.filter { it in explicitPositions || it == heroPosition }

		val actorPosition: Position?
			get() = currentActor?.takeIf { phase == QuickPhase.ACTION || phase == QuickPhase.HERO_CARDS }
				?.let { ring.positionOf(it) }

		private val heroSeat: Int
			get() = if (heroPosition != null) config.heroSeat else NO_HERO_SEAT

		private val maxBet: Double
			get() = streetBets.values.maxOrNull() ?: 0.0

		val isBetOpen: Boolean
			get() = street != Street.PREFLOP && maxBet > 0.0

		private val remaining: List<Int>
			get() = seats.filter { it !in folded }

		private val isShowdown: Boolean
			get() = remaining.size > 1

		private val showdownOrder: List<Int>
			get() = ring.postflopOrder.filter { it in remaining && it != heroSeat }

		private val order: List<Int>
			get() = if (street == Street.PREFLOP) ring.preflopOrder else ring.postflopOrder

		private val allCards: List<Card>
			get() = heroCards + streets.boardCards + boardBuffer + opponentCards.values.flatten()

		val openStreet: Street?
			get() = street.takeIf {
				phase == QuickPhase.ACTION || phase == QuickPhase.HERO_CARDS || phase == QuickPhase.BOARD
			}

		val isShowdownOpen: Boolean
			get() = phase == QuickPhase.REVIEW && isShowdown

		val pendingShowdownSeats: Set<Int>
			get() = if (isShowdownOpen) {
				showdownOrder.filter { seat ->
					cardsOf(seat).size < 2
				}.toSet()
			} else {
				emptySet()
			}

		val canSave: Boolean
			get() = heroPosition != null && phase == QuickPhase.REVIEW && streets.preflop.actions.any {
				it.type != ActionType.FOLD
			}

		val finish: QuickFinish?
			get() = if (phase == QuickPhase.REVIEW) {
				QuickFinish(
					heroPosition = heroPosition,
					canSelectHeroPosition = canSelectHeroPosition,
					heroCards = heroCards.toList(),
					board = streets.boardCards,
					showdown = if (isShowdown) {
						showdownOrder.map {
							QuickShowdownHand(it, ring.positionOf(it), opponentCards[it].orEmpty().toList())
						}
					} else {
						emptyList()
					},
					usedCards = allCards.filter { it.suit != null }.toSet(),
				)
			} else {
				null
			}

		private val canSelectHeroPosition: Boolean
			get() = heroPosition == null || heroFromReview

		private val canMarkHero: Boolean
			get() = heroPosition == null && pendingType == null && amountText.isEmpty()

		private val pendingAmountChips: Double?
			get() = amountText.toDoubleOrNull()?.let { if (amountInChips) it else it * bb }

		private fun isAmountValidFor(type: ActionType?): Boolean {
			val actor = currentActor ?: return false
			val amount = pendingAmountChips ?: return false
			return when (type) {
				ActionType.ALL_IN -> amount > betOf(actor)
				else -> amount > maxBet && amount > betOf(actor)
			}
		}

		private fun requiresAmount(type: ActionType): Boolean =
			type.isSized || (type == ActionType.ALL_IN && !isStackKnown)

		private val isLineComplete: Boolean
			get() = when (phase) {
				QuickPhase.ACTION ->
					cardSeat != null ||
						pendingType?.let { !requiresAmount(it) || isAmountValidFor(it) || isAmountlessAllIn(it) } ?: false
				QuickPhase.HERO_CARDS -> true
				QuickPhase.BOARD -> pendingRank != null
				QuickPhase.REVIEW -> false
			}

		private fun isAmountlessAllIn(type: ActionType) = type == ActionType.ALL_IN && amountText.isEmpty()

		private fun canAct(seat: Int) = seat !in folded && seat !in allIn

		private fun betOf(seat: Int) = streetBets[seat] ?: 0.0

		private fun stackOf(seat: Int) = stacks[seat] ?: 0.0

		private fun initialStackOf(seat: Int) = startStack + (topUps[seat] ?: 0.0)

		private fun coverUpTo(seat: Int, target: Double) {
			val shortfall = target - (stackOf(seat) + betOf(seat))
			if (shortfall <= 0.0) return
			val extra = shortfall + startStack
			stacks[seat] = stackOf(seat) + extra
			topUps[seat] = (topUps[seat] ?: 0.0) + extra
		}

		private fun isFacing(seat: Int) = betOf(seat) < maxBet

		private fun needsAction(seat: Int) = canAct(seat) && (seat !in acted || isFacing(seat))

		private fun cardsOf(seat: Int): MutableList<Card> = opponentCards.getOrPut(
			seat,
		) { mutableListOf() }

		fun apply(input: QuickInput) {
			if (input == QuickInput.ToggleAmountUnit) {
				if (canToggleAmountUnit) {
					amountInChips = !amountInChips
					val actor = currentActor
					val size = pendingPotSize
					if (actor != null && size != null) amountText = potSizedText(actor, size)
				}
				return
			}
			if (input == QuickInput.NextLine) {
				commitLine()
				return
			}
			if (input == QuickInput.CloseStreet) {
				if (canCloseStreet) closeStreet()
				return
			}
			if (input is QuickInput.FillCards && input.owner != CardOwner.Board) {
				fillCards(input.owner, input.cards)
				return
			}
			if (input is QuickInput.FillCards && phase == QuickPhase.BOARD) {
				fillBoard(input.cards)
				return
			}
			if (pendingRank != null && input !is QuickInput.CardSuit) commitCard(null)
			when (phase) {
				QuickPhase.ACTION -> applyAction(input)
				QuickPhase.HERO_CARDS -> applyHeroCards(input)
				QuickPhase.BOARD -> applyCardKey(input)
				QuickPhase.REVIEW -> applyReview(input)
			}
		}

		private fun commitLine() {
			if (!isLineComplete) return
			when (phase) {
				QuickPhase.ACTION -> if (cardSeat != null) {
					commitCard(null)
					cardSeat = null
				} else {
					commitStaged()
				}
				QuickPhase.HERO_CARDS -> {
					commitCard(null)
					phase = QuickPhase.ACTION
				}
				QuickPhase.BOARD -> commitCard(null)
				QuickPhase.REVIEW -> Unit
			}
		}

		val isLastBoardCardPending: Boolean
			get() = phase == QuickPhase.BOARD && pendingRank != null && boardBuffer.size + 1 >= boardSize(street)

		fun commitPendingCard() = commitCard(null)

		private fun commitStaged() {
			val actor = currentActor ?: return
			val type = pendingType ?: return
			val amount = if (requiresAmount(type) && !isAmountlessAllIn(type)) pendingAmountChips ?: return else null
			val potSize = pendingPotSize
			pendingType = null
			amountText = ""
			pendingPotSize = null
			explicitPositions += ring.positionOf(actor)
			act(actor, type, amount, potSize)
		}

		private fun applyAction(input: QuickInput) {
			val actor = currentActor ?: return
			if (cardSeat != null) {
				if (input is QuickInput.CardRank || input is QuickInput.CardSuit) {
					applyCardKey(input)
					return
				}
				cardSeat = null
			}
			val isIdle = pendingType == null && amountText.isEmpty()
			when (input) {
				is QuickInput.Seat -> if (isIdle) jumpTo(input.position)
				is QuickInput.Hero -> if (canMarkHero) {
					heroPosition = input.position
					phase = QuickPhase.HERO_CARDS
				}
				is QuickInput.Act -> applyAct(actor, input.type)
				is QuickInput.Digit -> if (canEditAmount(actor) && canAppendDigit()) {
					amountText += input.value.toString()
					pendingPotSize = null
				}
				QuickInput.Dot -> if (canEditAmount(actor) && amountText.isNotEmpty() && '.' !in amountText) {
					amountText += "."
					pendingPotSize = null
				}
				is QuickInput.PotSize -> if (canUsePotSize && canEditAmount(actor)) {
					amountText = potSizedText(actor, input.size)
					pendingPotSize = input.size
				}
				else -> Unit
			}
		}

		private fun canEditAmount(actor: Int): Boolean =
			pendingType?.let(::requiresAmount) ?: canEnterAmount(actor)

		private fun applyAct(actor: Int, type: ActionType) {
			if (type !in legalActions(actor)) return
			val amountFirst = pendingType == null && amountText.isNotEmpty()
			if (amountFirst && !(requiresAmount(type) && isAmountValidFor(type))) return
			if (!requiresAmount(type)) {
				amountText = ""
				pendingPotSize = null
			}
			pendingType = type
		}

		private fun applyHeroCards(input: QuickInput) {
			when (input) {
				is QuickInput.CardRank, is QuickInput.CardSuit -> applyCardKey(input)
				else -> {
					phase = QuickPhase.ACTION
					applyAction(input)
				}
			}
		}

		private fun applyCardKey(input: QuickInput) {
			when (input) {
				is QuickInput.CardRank -> if (canStartCard(input.rank)) pendingRank = input.rank
				is QuickInput.CardSuit -> commitCard(input.suit)
				else -> Unit
			}
		}

		private fun cardSlot(): Pair<MutableList<Card>, Int>? = when (phase) {
			QuickPhase.HERO_CARDS -> heroCards to 2
			QuickPhase.BOARD -> boardBuffer to boardSize(street)
			QuickPhase.ACTION -> cardSeat?.let { holeCardsOf(it) to 2 }
			else -> null
		}

		private fun canStartCard(rank: Rank): Boolean {
			if (pendingRank != null) return false
			val (buffer, size) = cardSlot() ?: return false
			return buffer.size < size && allCards.count { it.rank == rank } < Suit.entries.size
		}

		private fun commitCard(suit: Suit?) {
			val rank = pendingRank ?: return
			val (buffer, _) = cardSlot() ?: run {
				pendingRank = null
				return
			}
			val card = Card(rank, suit)
			if (suit != null && card in allCards) return
			pendingRank = null
			buffer += card
			if (phase == QuickPhase.BOARD && boardBuffer.size >= boardSize(street)) completeBoard()
			if (cardSeat != null && buffer.size >= 2) cardSeat = null
		}

		private fun holeCardsOf(seat: Int): MutableList<Card> =
			if (heroPosition != null && seat == config.heroSeat) heroCards else cardsOf(seat)

		val boardSlots: Int
			get() = if (phase == QuickPhase.BOARD) boardSize(street) else 0

		private fun fillBoard(cards: List<Card>) {
			val others = allCards - boardBuffer.toSet()
			val isValid = cards.size == boardSize(street) &&
				cards.all { it.suit != null } &&
				cards.distinct().size == cards.size &&
				cards.none { it in others }
			if (!isValid) return
			pendingRank = null
			boardBuffer.replaceWith(cards)
			completeBoard()
		}

		private fun completeBoard() {
			streets = streets.setCards(street, boardBuffer.toList())
			boardBuffer.clear()
			startRound()
		}

		private fun boardSize(street: Street) = if (street == Street.FLOP) 3 else 1

		private fun startPreflop() {
			street = Street.PREFLOP
			postBlind(ring.sbSeat, config.blinds.sb)
			postBlind(ring.bbSeat, config.blinds.bb)
			if (config.blinds.isBigBlindAnte) {
				stacks[ring.bbSeat] = (stackOf(ring.bbSeat) - config.blinds.bb).coerceAtLeast(0.0)
			}
			startRound()
		}

		private fun postBlind(seat: Int, amount: Double) {
			if (amount <= 0) return
			val posted = minOf(amount, stackOf(seat))
			streetBets[seat] = posted
			stacks[seat] = stackOf(seat) - posted
		}

		private fun startStreet(next: Street) {
			street = next
			streets = streets.ensureStreet(next)
			streetBets.clear()
			acted.clear()
			raiseCount = 0
			unknownShoveLevel = null
			lastRaiseSize = bb
			isRaiseCapped = false
			isMaxBetAllIn = false
			currentActor = null
			phase = QuickPhase.BOARD
		}

		private fun startRound() {
			val actors = seats.filter { canAct(it) }
			if (actors.size <= 1 && actors.none { isFacing(it) }) {
				closeRound()
				return
			}
			val first = order.firstOrNull { needsAction(it) }
			if (first == null) {
				closeRound()
				return
			}
			currentActor = first
			phase = QuickPhase.ACTION
		}

		private fun closeRound() {
			currentActor = null
			val next = nextStreet()
			if (remaining.size <= 1 || next == null) {
				phase = QuickPhase.REVIEW
			} else {
				startStreet(next)
			}
		}

		private fun canEnterAmount(actor: Int): Boolean =
			pendingType != null || legalActions(actor).any { it == ActionType.BET || it == ActionType.RAISE }

		private fun canAppendDigit(): Boolean {
			val dot = amountText.indexOf('.')
			val maxDigits = if (amountInChips) MAX_CHIP_DIGITS else MAX_AMOUNT_DIGITS
			return if (dot >= 0) amountText.length - dot - 1 < 1 else amountText.length < maxDigits
		}

		private fun potSizedText(actor: Int, size: BetSize): String {
			val target = if (isBetOpen) {
				maxBet * size.raiseMultiple
			} else {
				val pot = seats.sumOf { initialStackOf(it) - stackOf(it) }
				maxBet + size.ratio * (pot + maxBet - betOf(actor))
			}
			if (amountInChips) return target.roundToLong().toString()
			val tenths = (target / bb * 10).roundToLong()
			return if (tenths % 10 == 0L) "${tenths / 10}" else "${tenths / 10}.${tenths % 10}"
		}

		fun legalActions(seat: Int): List<ActionType> {
			if (!canAct(seat)) return emptyList()
			val facing = isFacing(seat)
			val effective = stackOf(seat) + betOf(seat)
			return buildList {
				if (!facing) add(ActionType.CHECK) else if (!foldsToWalk(1)) add(ActionType.FOLD)
				if (facing) add(ActionType.CALL)
				if (facing && isRaiseClosed(effective)) return@buildList
				if (maxBet <= 0.0) {
					if (effective > 0) add(ActionType.BET)
				} else {
					add(ActionType.RAISE)
				}
				if (stackOf(seat) > 0) add(ActionType.ALL_IN)
			}
		}

		private fun isRaiseClosed(effective: Double): Boolean =
			unknownShoveLevel != null || isRaiseCapped || (isMaxBetAllIn && effective <= maxBet)

		private fun foldsToWalk(folds: Int): Boolean =
			street == Street.PREFLOP &&
				streets.preflop.actions.all { it.type == ActionType.FOLD } &&
				remaining.size - folds <= 1

		private fun jumpTargets(): List<Int> {
			val actor = currentActor ?: return emptyList()
			val rotation = order
			val start = rotation.indexOf(actor).takeIf { it >= 0 } ?: return emptyList()
			return rotation.indices
				.map { rotation[(start + it) % rotation.size] }
				.filter { needsAction(it) }
		}

		private fun seatOf(position: Position): Int? = seats.firstOrNull { ring.positionOf(it) == position }

		private fun jumpTo(position: Position) {
			val target = seatOf(position) ?: return
			val sequence = jumpTargets()
			val index = sequence.indexOf(target)
			if (index < 0 || foldsToWalk(index)) return
			sequence.take(index).forEach { skippedSeat ->
				if (phase != QuickPhase.ACTION) return
				act(skippedSeat, if (isFacing(skippedSeat)) ActionType.FOLD else ActionType.CHECK, null)
			}
			if (phase == QuickPhase.ACTION && target !in folded) {
				currentActor = target
				isActorChosen = true
				if (holeCardsOf(target).size < 2) cardSeat = target
			}
		}

		private val canCloseStreet: Boolean
			get() {
				if (phase != QuickPhase.ACTION || cardSeat != null || pendingType != null || amountText.isNotEmpty()) return false
				if (street == Street.PREFLOP && acted.isEmpty()) return false
				if (street == Street.PREFLOP && streets.preflop.actions.all {
						it.type == ActionType.FOLD
					}
				) {
					return false
				}
				val first = jumpTargets().firstOrNull() ?: return false
				return !isHeroFacing(first)
			}

		private fun isHeroFacing(seat: Int) = seat == heroSeat && isFacing(seat)

		private fun closeStreet() {
			val openStreet = street
			jumpTargets().forEach { seat ->
				if (phase != QuickPhase.ACTION || street != openStreet) return
				if (isHeroFacing(seat)) {
					currentActor = seat
					isActorChosen = true
					return
				}
				act(seat, if (isFacing(seat)) ActionType.FOLD else ActionType.CHECK, null)
			}
		}

		private fun act(seat: Int, type: ActionType, target: Double?, potSize: BetSize? = null) {
			when (type) {
				ActionType.BET, ActionType.RAISE -> target?.let { coverUpTo(seat, it) }
				ActionType.CALL -> if (maxBet > stackOf(seat) + betOf(seat)) coverUpTo(seat, maxBet)
				else -> Unit
			}
			val currentBet = betOf(seat)
			val effective = stackOf(seat) + currentBet
			val previousMax = maxBet

			val (resolvedType, amount) = when (type) {
				ActionType.FOLD, ActionType.CHECK -> type to null
				ActionType.CALL -> if (previousMax >= effective && unknownShoveLevel == null) {
					ActionType.ALL_IN to effective
				} else {
					ActionType.CALL to previousMax
				}
				ActionType.ALL_IN -> ActionType.ALL_IN to (target ?: effective)
				ActionType.BET, ActionType.RAISE -> {
					val to = target ?: return
					if (to == effective) ActionType.ALL_IN to to else type to to
				}
			}

			val isAggressive = resolvedType == ActionType.BET ||
				resolvedType == ActionType.RAISE ||
				(resolvedType == ActionType.ALL_IN && (amount ?: 0.0) > previousMax)
			if (isAggressive) {
				val raiseSize = (amount ?: 0.0) - previousMax
				isRaiseCapped = resolvedType == ActionType.ALL_IN && raiseCount > 0 && raiseSize < lastRaiseSize
				if (!isRaiseCapped) lastRaiseSize = raiseSize
				isMaxBetAllIn = resolvedType == ActionType.ALL_IN
			}
			val isUnknownShove = type == ActionType.ALL_IN && target == null && !isStackKnown
			if (isUnknownShove) {
				unknownShoveLevel = amount
				hasUnknownAmount = true
			}
			val isUnknownAmount = amount != null && unknownShoveLevel?.let { amount >= it } == true
			val betLevel = if (isAggressive) {
				raiseCount += 1
				raiseCount + if (street == Street.PREFLOP) 1 else 0
			} else {
				0
			}

			val newStack = if (amount != null) (effective - amount).coerceAtLeast(0.0) else stackOf(seat)
			val tablePlayer = config.tablePlayers.find { it.seat == seat }
			streets = streets.addAction(
				street,
				Action(
					playerSeat = seat,
					type = resolvedType,
					amount = amount.takeUnless { isUnknownAmount },
					stackBefore = effective.takeIf { isStackKnown },
					stackAfter = newStack.takeIf { isStackKnown },
					betLevel = betLevel,
					playerName = tablePlayer?.name,
					savedPlayerId = tablePlayer?.savedPlayerId,
					potSize = potSize.takeIf { resolvedType == type && type.isSized },
				),
			)
			stacks[seat] = newStack
			if (amount != null) streetBets[seat] = amount
			when (resolvedType) {
				ActionType.FOLD -> folded += seat
				ActionType.ALL_IN -> allIn += seat
				else -> if (newStack <= 0.0 && amount != null) allIn += seat
			}
			if (isAggressive) acted.clear()
			acted += seat

			advanceFrom(seat)
		}

		private fun advanceFrom(seat: Int) {
			if (remaining.size <= 1) {
				closeRound()
				return
			}
			val rotation = order
			val start = rotation.indexOf(seat)
			val next = (1..rotation.size)
				.map { rotation[(start + it) % rotation.size] }
				.firstOrNull { needsAction(it) }
			val othersCanAct = seats.count { canAct(it) }
			if (next == null || (othersCanAct <= 1 && !isFacing(next))) {
				closeRound()
			} else {
				currentActor = next
			}
		}

		private fun nextStreet(): Street? = when (street) {
			Street.PREFLOP -> Street.FLOP
			Street.FLOP -> Street.TURN
			Street.TURN -> Street.RIVER
			Street.RIVER -> null
		}

		private fun applyReview(input: QuickInput) {
			when (input) {
				is QuickInput.Seat -> if (canSelectHeroPosition && input.position in involvedPositions) {
					heroPosition = input.position
					heroFromReview = true
					val entered = opponentCards.remove(config.heroSeat).orEmpty()
					if (heroCards.isEmpty()) heroCards += entered
				}
				is QuickInput.FillCards -> fillCards(input.owner, input.cards)
				else -> Unit
			}
		}

		private fun fillCards(owner: CardOwner, cards: List<Card>) {
			val current = when (owner) {
				CardOwner.Hero -> heroCards.toList().takeIf { heroPosition != null }
				CardOwner.Board -> streets.boardCards
				is CardOwner.Opponent -> seatOf(owner.position)?.takeIf { it != heroSeat }?.let {
					opponentCards[it].orEmpty().toList()
				}
			} ?: return
			val isSizeValid = if (owner == CardOwner.Board) cards.size == current.size else cards.isEmpty() || cards.size == 2
			val others = allCards.toMutableList().apply { current.forEach { remove(it) } }
			if (!isSizeValid || cards.distinct().size != cards.size || cards.any { it in others }) return
			when (owner) {
				CardOwner.Hero -> heroCards.replaceWith(cards)
				is CardOwner.Opponent -> cardsOf(seatOf(owner.position) ?: return).replaceWith(cards)
				CardOwner.Board -> {
					var offset = 0
					Street.entries.forEach { street ->
						val size = streets.getCards(street).size
						if (size > 0) {
							streets = streets.setCards(street, cards.subList(offset, offset + size))
							offset += size
						}
					}
				}
			}
		}

		private fun MutableList<Card>.replaceWith(cards: List<Card>) {
			clear()
			addAll(cards)
		}

		fun allowedInputs(): Set<QuickInput> = buildSet {
			if (isLineComplete) add(QuickInput.NextLine)
			if (canCloseStreet) add(QuickInput.CloseStreet)
			if (canToggleAmountUnit) add(QuickInput.ToggleAmountUnit)
			when (phase) {
				QuickPhase.ACTION -> if (cardSeat != null) {
					addCardInputs()
					addActionInputs(includeAmount = false)
				} else {
					addActionInputs(includeAmount = true)
				}
				QuickPhase.HERO_CARDS -> {
					addCardInputs()
					addActionInputs(includeAmount = false)
				}
				QuickPhase.BOARD -> addCardInputs()
				QuickPhase.REVIEW -> if (canSelectHeroPosition) {
					involvedPositions.forEach {
						add(QuickInput.Seat(it))
					}
				}
			}
		}

		private fun MutableSet<QuickInput>.addActionInputs(includeAmount: Boolean) {
			val actor = currentActor ?: return
			val amountInputs = includeAmount || pendingType != null
			when {
				pendingType != null -> {
					legalActions(actor).forEach { add(QuickInput.Act(it)) }
					if (canEditAmount(actor)) {
						if (canAppendDigit()) (0..9).forEach { add(QuickInput.Digit(it)) }
						if (amountText.isNotEmpty() && '.' !in amountText) add(QuickInput.Dot)
						addPotSizes()
					}
				}
				amountText.isNotEmpty() -> {
					if (canAppendDigit()) (0..9).forEach { add(QuickInput.Digit(it)) }
					if ('.' !in amountText) add(QuickInput.Dot)
					addPotSizes()
					legalActions(actor)
						.filter { requiresAmount(it) && isAmountValidFor(it) }
						.forEach { add(QuickInput.Act(it)) }
				}
				else -> {
					legalActions(actor).forEach { add(QuickInput.Act(it)) }
					jumpTargets().forEachIndexed { index, seat ->
						if (!foldsToWalk(index)) add(QuickInput.Seat(ring.positionOf(seat)))
					}
					if (canMarkHero) positions.forEach { add(QuickInput.Hero(it)) }
					if (amountInputs && canEnterAmount(actor)) {
						(0..9).forEach { add(QuickInput.Digit(it)) }
						addPotSizes()
					}
				}
			}
		}

		private val canUsePotSize: Boolean
			get() = street != Street.PREFLOP

		private fun MutableSet<QuickInput>.addPotSizes() {
			if (canUsePotSize) BetSize.entries.forEach { add(QuickInput.PotSize(it)) }
		}

		val heroHand: List<Card>
			get() = if (heroPosition != null) heroCards.toList() else emptyList()

		val opponentHands: List<QuickShowdownHand>
			get() = ring.preflopOrder.filter { it != heroSeat }.map {
				QuickShowdownHand(it, ring.positionOf(it), opponentCards[it].orEmpty().toList())
			}

		val usedCards: Set<Card>
			get() = allCards.filter { it.suit != null }.toSet()

		private fun MutableSet<QuickInput>.addCardInputs() {
			val (buffer, size) = cardSlot() ?: return
			val rank = pendingRank
			val countByRank = allCards.groupingBy { it.rank }.eachCount()
			if (rank != null) {
				Suit.entries.filter { Card(rank, it) !in allCards }.forEach { add(QuickInput.CardSuit(it)) }
			}
			val nextIndex = buffer.size + if (rank != null) 1 else 0
			if (nextIndex < size) {
				Rank.entries
					.filter { (countByRank[it] ?: 0) + (if (it == rank) 1 else 0) < Suit.entries.size }
					.forEach { add(QuickInput.CardRank(it)) }
			}
		}

		fun pending(): QuickPending? = when (phase) {
			QuickPhase.ACTION -> currentActor?.let { actor ->
				val staged = pendingType
				val holeSeat = cardSeat
				if (holeSeat != null) {
					QuickPending.PlayerCards(holeSeat, holeCardsOf(holeSeat).toList(), pendingRank)
				} else if (staged == null && amountText.isEmpty()) {
					QuickPending.Actor(actor, isChosen = isActorChosen || street != Street.PREFLOP)
				} else if (staged != null && !requiresAmount(staged)) {
					QuickPending.Staged(seat = actor, type = staged, amount = stagedAmount(actor, staged))
				} else {
					QuickPending.Amount(
						seat = actor,
						type = pendingType,
						betLevel = raiseCount + 1 + if (street == Street.PREFLOP) 1 else 0,
						text = amountText,
						potSize = pendingPotSize,
					)
				}
			}
			QuickPhase.HERO_CARDS -> heroPosition?.let {
				QuickPending.HeroCards(it, heroCards.toList(), pendingRank)
			}
			QuickPhase.BOARD -> QuickPending.BoardCard(boardBuffer.toList(), pendingRank)
			QuickPhase.REVIEW -> null
		}

		private fun stagedAmount(actor: Int, type: ActionType): Double? = when (type) {
			ActionType.CALL -> (maxBet - betOf(actor)).takeIf { unknownShoveLevel == null }
			ActionType.ALL_IN -> stackOf(actor) + betOf(actor)
			else -> null
		}

		private val displayStreets: HandStreets
			get() = if (phase == QuickPhase.BOARD && boardBuffer.isNotEmpty()) {
				streets.setCards(street, boardBuffer.toList())
			} else {
				streets
			}

		private fun pocketOf(cards: List<Card>): PocketCards? =
			if (cards.size == 2) PocketCards(cards[0], cards[1]) else null

		fun toRecord(): HandRecord = HandRecord(
			id = "",
			tableId = config.tableId,
			createdAt = 0L,
			blinds = config.blinds,
			heroSeat = heroSeat,
			buttonSeat = buttonSeat,
			streets = displayStreets,
			players = seats.map { seat ->
				val tablePlayer = config.tablePlayers.find { it.seat == seat }
				HandPlayer(
					seat = seat,
					cards = pocketOf(if (seat == heroSeat) heroCards else opponentCards[seat].orEmpty()),
					initialStack = initialStackOf(seat).takeIf { isStackKnown },
					playerName = tablePlayer?.name,
					savedPlayerId = tablePlayer?.savedPlayerId,
					isHero = seat == heroSeat,
				)
			},
		)

		fun toFinalRecord(
			evaluator: (List<Card>, List<ShowdownEntry>) -> List<ShowdownResult>,
		): HandRecord {
			val base = toRecord()
			val results = resolveResults(base, evaluator)
			val players = base.players.map { player ->
				val result = results.find { it.seat == player.seat } ?: return@map player
				player.copy(ranking = result.ranking, bestCards = result.bestCards, outcome = result.outcome)
			}
			val withResults = base.copy(players = players)
			val finalStack = withResults.getFinalStacks(evaluator)[heroSeat]
			val heroInitial = if (isStackKnown) initialStackOf(heroSeat) else 0.0
			return withResults.copy(
				result = finalStack?.takeUnless {
					hasUnknownAmount
				}?.let { it - heroInitial },
			)
		}

		private fun resolveResults(
			record: HandRecord,
			evaluator: (List<Card>, List<ShowdownEntry>) -> List<ShowdownResult>,
		): List<ShowdownResult> {
			val alive = remaining
			if (alive.size == 1) {
				val winner = alive.first()
				return seats.map { seat ->
					ShowdownResult(
						seat = seat,
						ranking = HandRanking.WIN_BY_FOLD,
						outcome = if (seat == winner) ShowdownOutcome.WIN else ShowdownOutcome.LOSE,
					)
				}
			}
			val board = record.streets.boardCards
			val entries = record.players
				.filter { it.seat in alive }
				.mapNotNull { it.toShowdownEntry() }
			if (board.size != 5 || entries.size < 2) return emptyList()
			return evaluator(board, entries)
		}
	}

	private companion object {
		val ActionType.isSized: Boolean
			get() = this == ActionType.BET || this == ActionType.RAISE

		const val NO_HERO_SEAT = 0
		const val UNKNOWN_STACK_BB = 1_000_000.0
		const val MAX_AMOUNT_DIGITS = 5
		const val MAX_CHIP_DIGITS = 9
	}
}

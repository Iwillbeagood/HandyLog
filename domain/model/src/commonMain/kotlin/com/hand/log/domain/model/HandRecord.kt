package com.hand.log.domain.model

data class HandRecord(
	val id: String,
	val tableId: String,
	val createdAt: Long,
	val blinds: Blinds? = null,
	val heroSeat: Int = 0,
	val buttonSeat: Int = 1,
	val streets: HandStreets = HandStreets(),
	val players: List<HandPlayer> = emptyList(),
	val result: Double? = null,
	val resultLabel: String? = null,
	val memo: String? = null,
	val results: HandResults? = null,
) {

	/**
	 * Action 로그로부터 파생 결과를 계산한다. 저장 시점에 한 번 호출해 [results]에 담는다.
	 * evaluator는 데이터 레이어가 주입한다(도메인은 평가기 구현에 의존하지 않음).
	 */
	fun computeResults(
		evaluator: (List<Card>, List<ShowdownEntry>) -> List<ShowdownResult>,
	): HandResults = HandResults(
		potByStreet = listOf(Street.PREFLOP, Street.FLOP, Street.TURN, Street.RIVER)
			.associateWith { getPotAtStreet(it) },
		finalStacks = getFinalStacks(evaluator),
		winnerSeats = winnerSeats,
		seatInvestments = seatInvestments,
	)

	/** 저장된 스트릿별 팟을 우선 읽고, 없으면(미저장 핸드) 계산으로 폴백 */
	fun potAt(street: Street): Double = results?.potByStreet?.get(street) ?: getPotAtStreet(street)

	/** 히어로의 홀 카드 */
	val heroHoleCards: PocketCards?
		get() = players.find { it.isHero }?.cards

	/** 히어로의 초기 스택 */
	val heroStack: Double
		get() = players.find { it.isHero }?.initialStack ?: 0.0

	fun getShowdownResult(seat: Int): ShowdownResult? {
		val p = players.find { it.seat == seat } ?: return null
		val ranking = p.ranking ?: return null
		return ShowdownResult(
			seat = seat,
			ranking = ranking,
			bestCards = p.bestCards,
			outcome = p.outcome ?: ShowdownOutcome.LOSE,
		)
	}

	/** 카드가 공개된 플레이어의 ShowdownEntry 목록 */
	val showdown: List<ShowdownEntry>
		get() = players.mapNotNull { it.toShowdownEntry() }

	val showdownSeats: Set<Int>
		get() = players.filter { it.cards != null }.map { it.seat }.toSet()

	val unknownCardSeats: List<Int>
		get() = remainingSeats.filter { seat -> players.none { it.seat == seat && it.cards != null } }

	val heroShowdownEntry: ShowdownEntry?
		get() = players.find { it.isHero }?.toShowdownEntry()

	val resolvedHeroResultType: HeroResultType
		get() {
			val heroPlayer = players.find { it.isHero }
			val isWin = (result ?: 0.0) >= 0
			return when {
				isFoldWin && isWin -> HeroResultType.FOLD_WIN
				isFoldWin -> HeroResultType.FOLD_LOSE
				heroPlayer?.isSplit == true -> HeroResultType.SHOWDOWN_SPLIT
				isWin -> HeroResultType.SHOWDOWN_WIN
				else -> HeroResultType.SHOWDOWN_LOSE
			}
		}

	val heroRanking: HandRanking?
		get() {
			val r = players.find { it.isHero }?.ranking
			return if (r == HandRanking.WIN_BY_FOLD) null else r
		}

	/**
	 * 히어로(나) 기준 승/무/패. 쇼다운 결과([HandPlayer.outcome])를 우선 판정하고,
	 * 쇼다운 없이 끝난 핸드(폴드 등)는 순수익([result]) 부호로 폴백한다.
	 */
	val heroOutcome: HeroOutcome
		get() {
			val hero = players.find { it.isHero }
			return when {
				isFoldWin -> HeroOutcome.WIN
				hero?.isSplit == true -> HeroOutcome.DRAW
				hero?.isWinner == true -> HeroOutcome.WIN
				hero?.outcome == ShowdownOutcome.LOSE -> HeroOutcome.LOSE
				(result ?: 0.0) > 0 -> HeroOutcome.WIN
				(result ?: 0.0) < 0 -> HeroOutcome.LOSE
				else -> HeroOutcome.DRAW
			}
		}

	val winnerSeats: Set<Int>
		get() {
			if (isFoldWin) return remainingSeats
			val fromPlayers = players
				.filter { it.isWinner || it.isSplit }
				.map { it.seat }.toSet()
			return if (fromPlayers.isNotEmpty()) {
				fromPlayers
			} else if (result != null && result >= 0) {
				setOf(heroSeat)
			} else {
				emptySet()
			}
		}

	/** 프리플랍에 참여한 모든 좌석 (정렬) */
	val allSeats: List<Int>
		get() = streets.preflop.actions.map { it.playerSeat }.distinct().sorted()

	/** 프리플랍 참여 인원 수 */
	val playerCount: Int
		get() = allSeats.size.coerceAtLeast(2)

	/** 프리플랍에서 폴드한 좌석 */
	val preflopFoldedSeats: Set<Int>
		get() = streets.preflop.actions
			.filter { it.type == ActionType.FOLD }
			.map { it.playerSeat }
			.toSet()

	/** 최종까지 남은 좌석 (모든 스트릿의 폴드 제외) */
	val remainingSeats: Set<Int>
		get() {
			val seats = allSeats.toMutableSet()
			listOf(Street.PREFLOP, Street.FLOP, Street.TURN, Street.RIVER).forEach { street ->
				streets.getActions(street).forEach { action ->
					if (action.type == ActionType.FOLD) seats.remove(action.playerSeat)
				}
			}
			return seats
		}

	/** 폴드로 승리했는지 (1명만 남음) */
	val isFoldWin: Boolean
		get() = remainingSeats.size == 1

	/** 히어로의 포지션 */
	val heroPosition: Position
		get() = getPosition(heroSeat)

	fun getPosition(seat: Int): Position {
		val count = playerCount
		val btn = buttonSeat
		val sbSeat = (btn % count) + 1
		val bbSeat = ((btn + 1) % count) + 1

		if (seat == btn) return Position.BTN
		if (seat == sbSeat) return Position.SB
		if (seat == bbSeat) return Position.BB

		val preflopOrder = (1..count).map { offset -> ((btn + 2 + offset - 1) % count) + 1 }
		val utgOrder = preflopOrder.filter { it != btn && it != sbSeat && it != bbSeat }
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

	/** BB 금액 (기본값 1.0) */
	val bbAmount: Double
		get() = blinds?.bb?.takeIf { it > 0 } ?: 1.0

	/** 좌석의 포지션 이름 */
	fun getPositionName(seat: Int): String = getPosition(seat).label

	/** 좌석의 프리플랍 시작 스택 */
	fun getInitialStack(seat: Int): Double? =
		players.find { it.seat == seat }?.initialStack
			?: streets.preflop.actions.firstOrNull { it.playerSeat == seat }?.stackBefore

	/** 좌석의 쇼다운 카드 */
	fun getShowdownCards(seat: Int): PocketCards? =
		players.find { it.seat == seat }?.cards

	/** 특정 스트릿까지의 누적 팟 */
	fun getPotAtStreet(upToStreet: Street): Double {
		val sb = blinds?.sb ?: 0.0
		val bb = blinds?.bb ?: 0.0
		val count = playerCount
		val btn = buttonSeat
		val sbSeat = if (count > 0) (btn % count) + 1 else 0
		val bbSeat = if (count > 0) ((btn + 1) % count) + 1 else 0
		val antePot = if (blinds?.isBigBlindAnte == true) bb else 0.0

		var pot = antePot
		val streetOrder = listOf(Street.PREFLOP, Street.FLOP, Street.TURN, Street.RIVER)
		for (s in streetOrder) {
			val actions = streets.getActions(s)
			if (s == Street.PREFLOP) {
				val seatAmounts = actions.groupBy { it.playerSeat }
					.mapValues { (_, acts) -> acts.maxOf { it.amount ?: 0.0 } }
				val sorted = seatAmounts.values.sortedDescending()
				val uncalled = if (!isFoldWin && sorted.size >= 2) sorted[0] - sorted[1] else 0.0
				pot += seatAmounts.values.sum() - uncalled
				if ((seatAmounts[sbSeat] ?: 0.0) == 0.0) pot += sb
				if ((seatAmounts[bbSeat] ?: 0.0) == 0.0) pot += bb
			} else {
				val seatAmounts = actions.groupBy { it.playerSeat }
					.mapValues { (_, acts) -> acts.maxOf { it.amount ?: 0.0 } }
				val sorted = seatAmounts.values.sortedDescending()
				val uncalled = if (!isFoldWin && sorted.size >= 2) sorted[0] - sorted[1] else 0.0
				pot += seatAmounts.values.sum() - uncalled
			}
			if (s == upToStreet) break
		}
		return pot
	}

	/** 좌석의 마킹된 플레이어 ID */
	fun getSavedPlayerId(seat: Int): String? =
		players.find { it.seat == seat }?.savedPlayerId
			?: streets.preflop.actions.firstOrNull { it.playerSeat == seat }?.savedPlayerId

	/** 좌석의 플레이어 이름 */
	fun getPlayerName(seat: Int): String? =
		players.find { it.seat == seat }?.playerName
			?: streets.preflop.actions.firstOrNull { it.playerSeat == seat }?.playerName

	/** 특정 스트릿 시점의 참여(폴드 안한) 인원 수 */
	fun getActiveCountAt(upToStreet: Street): Int {
		val foldedSeats = mutableSetOf<Int>()
		val streetOrder = listOf(Street.PREFLOP, Street.FLOP, Street.TURN, Street.RIVER)
		for (s in streetOrder) {
			streets.getActions(s).forEach { action ->
				if (action.type == ActionType.FOLD) foldedSeats.add(action.playerSeat)
			}
			if (s == upToStreet) break
		}
		return (allSeats.size - foldedSeats.size).coerceAtLeast(0)
	}

	/** 특정 savedPlayerId가 이 핸드에 참여했는지 */
	fun containsPlayer(savedPlayerId: String): Boolean =
		players.any { it.savedPlayerId == savedPlayerId } ||
			streets.preflop.actions.any { it.savedPlayerId == savedPlayerId }

	/** 좌석이 마킹된 플레이어인지 */
	fun isPlayerMarked(seat: Int): Boolean = getSavedPlayerId(seat) != null

	/** 각 좌석의 총 투입 금액 (블라인드 포함, 모든 스트릿 합산) */
	val seatInvestments: Map<Int, Double>
		get() {
			val result = mutableMapOf<Int, Double>()
			val count = playerCount
			val btn = buttonSeat
			val sbSeat = if (count > 0) (btn % count) + 1 else 0
			val bbSeat = if (count > 0) ((btn + 1) % count) + 1 else 0
			val sbAmount = blinds?.sb ?: 0.0
			val bbAmount = blinds?.bb ?: 0.0

			listOf(Street.PREFLOP, Street.FLOP, Street.TURN, Street.RIVER).forEach { street ->
				streets.getActions(street)
					.groupBy { it.playerSeat }
					.forEach { (seat, actions) ->
						val streetAmount = actions.maxOfOrNull { it.amount ?: 0.0 } ?: 0.0
						result[seat] = (result[seat] ?: 0.0) + streetAmount
					}
			}
			if ((result[sbSeat] ?: 0.0) == 0.0 && sbSeat in allSeats) result[sbSeat] = sbAmount
			if ((result[bbSeat] ?: 0.0) == 0.0 && bbSeat in allSeats) result[bbSeat] = bbAmount
			return result
		}

	/**
	 * 모든 플레이어의 최종 스택 (팟 분배 후).
	 */
	fun getFinalStacks(
		evaluator: (List<Card>, List<ShowdownEntry>) -> List<ShowdownResult>,
	): Map<Int, Double> {
		val investments = seatInvestments.toMutableMap()
		if (investments.isEmpty()) return emptyMap()

		val boardCards = streets.boardCards
		val entries = players.mapNotNull { it.toShowdownEntry() }

		val sortedLevels = investments.values.distinct().sorted()
		var previousLevel = 0.0
		val winnings = mutableMapOf<Int, Double>()

		for (level in sortedLevels) {
			val diff = level - previousLevel
			if (diff <= 0) continue

			val eligibleSeats = investments.filter { it.value >= level }.keys
			val potForLevel = diff * eligibleSeats.size
			val remainingEligible = eligibleSeats.intersect(remainingSeats)

			if (remainingEligible.size == 1) {
				val sole = remainingEligible.first()
				winnings[sole] = (winnings[sole] ?: 0.0) + potForLevel
			} else if (remainingEligible.size >= 2 && boardCards.size == 5) {
				val eligibleEntries = entries.filter { it.seat in remainingEligible }
				if (eligibleEntries.size >= 2) {
					val results = evaluator(boardCards, eligibleEntries)
					val winners = results.filter { it.isWinner || it.isSplit }
					if (winners.isNotEmpty()) {
						val share = potForLevel / winners.size
						winners.forEach { w -> winnings[w.seat] = (winnings[w.seat] ?: 0.0) + share }
					}
				} else if (eligibleEntries.size == 1) {
					val sole = eligibleEntries.first().seat
					winnings[sole] = (winnings[sole] ?: 0.0) + potForLevel
				}
			} else if (eligibleSeats.size == 1) {
				val sole = eligibleSeats.first()
				winnings[sole] = (winnings[sole] ?: 0.0) + potForLevel
			}
			previousLevel = level
		}

		// 앤티 팟 분배
		val anteCost = if (blinds?.isBigBlindAnte == true) (blinds?.bb ?: 0.0) else 0.0
		if (anteCost > 0) {
			val anteWinners = if (remainingSeats.size == 1) {
				listOf(remainingSeats.first())
			} else {
				val remainingEntries = entries.filter { it.seat in remainingSeats }
				if (remainingEntries.size >= 2 && boardCards.size == 5) {
					val results = evaluator(boardCards, remainingEntries)
					results.filter { it.isWinner || it.isSplit }.map { it.seat }
				} else if (remainingEntries.size == 1) {
					listOf(remainingEntries.first().seat)
				} else {
					emptyList()
				}
			}
			if (anteWinners.isNotEmpty()) {
				val share = anteCost / anteWinners.size
				anteWinners.forEach { seat -> winnings[seat] = (winnings[seat] ?: 0.0) + share }
			}
		}

		return allSeats.associateWith { seat ->
			val initial = getInitialStack(seat) ?: 0.0
			val invested = investments[seat] ?: 0.0
			val won = winnings[seat] ?: 0.0
			initial - invested + won
		}
	}
}

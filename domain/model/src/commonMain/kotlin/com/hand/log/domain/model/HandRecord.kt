package com.hand.log.domain.model

data class HandRecord(
	val id: String,
	val tableId: String,
	val createdAt: Long,
	val blinds: Blinds? = null,
	val heroHand: PocketCards? = null,
	val heroSeat: Int = 0,
	val heroStack: Double = 0.0,
	val buttonSeat: Int = 1,
	val streets: HandStreets = HandStreets(),
	val showdown: List<ShowdownEntry> = emptyList(),
	val showdownResults: List<ShowdownResult> = emptyList(),
	val result: Double? = null,
	val memo: String? = null,
) {
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
		streets.preflop.actions.firstOrNull { it.playerSeat == seat }?.stackBefore

	/** 좌석의 쇼다운 카드 (히어로면 heroHand, 아니면 showdown에서 검색) */
	fun getShowdownCards(seat: Int): PocketCards? =
		if (seat == heroSeat) heroHand else showdown.find { it.seat == seat }?.cards

	/** 특정 스트릿까지의 누적 팟 */
	fun getPotAtStreet(upToStreet: Street): Double {
		val blindsPot = (blinds?.sb ?: 0.0) + (blinds?.bb ?: 0.0)
		val antePot = if (blinds?.isBigBlindAnte == true) blinds!!.bb else 0.0
		var pot = blindsPot + antePot
		val streetOrder = listOf(Street.PREFLOP, Street.FLOP, Street.TURN, Street.RIVER)
		for (s in streetOrder) {
			pot += streets.getActions(s).sumOf { it.amount ?: 0.0 }
			if (s == upToStreet) break
		}
		return pot
	}

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

	/** 쇼다운 승자 좌석 */
	val winnerSeats: Set<Int>
		get() {
			// 폴드 승리: 남은 1명이 승자
			if (isFoldWin) return remainingSeats
			val fromResults = showdownResults.filter { it.isWinner }.map { it.seat }.toSet()
			return if (fromResults.isNotEmpty()) {
				fromResults
			} else if (result != null && result >= 0) {
				setOf(heroSeat)
			} else {
				emptySet()
			}
		}
}

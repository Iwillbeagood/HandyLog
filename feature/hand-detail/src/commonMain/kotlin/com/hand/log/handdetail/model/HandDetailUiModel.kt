package com.hand.log.handdetail.model

import androidx.compose.runtime.Immutable
import com.hand.log.domain.model.Action
import com.hand.log.domain.model.ActionType
import com.hand.log.domain.model.Card
import com.hand.log.domain.model.HandRecord
import com.hand.log.domain.model.Position
import com.hand.log.domain.model.Street

@Immutable
data class HandDetailUiModel(
	val hand: HandRecord,
	val heroSeat: Int,
	val playerCount: Int,
	val streetSections: List<StreetSectionUiModel>,
	val useBbUnit: Boolean = false,
) {
	companion object {
		fun from(hand: HandRecord, useBbUnit: Boolean = false): HandDetailUiModel {
			val heroSeat = hand.heroSeat
			val playerCount = hand.streets.preflop.actions
				.map { it.playerSeat }.distinct().size.coerceAtLeast(2)
			val bb = hand.blinds?.bb ?: 0.0

			val sections = buildList {
				add(
					StreetSectionUiModel.from(
						street = Street.PREFLOP,
						actions = hand.streets.preflop.actions,
						boardCards = emptyList(),
						previousCards = emptyList(),
						hand = hand,
						heroSeat = heroSeat,
						playerCount = playerCount,
						useBbUnit = useBbUnit,
						bb = bb,
					),
				)
				hand.streets.flop?.let { flop ->
					add(
						StreetSectionUiModel.from(
							street = Street.FLOP,
							actions = flop.actions,
							boardCards = flop.cards,
							previousCards = emptyList(),
							hand = hand,
							heroSeat = heroSeat,
							playerCount = playerCount,
							useBbUnit = useBbUnit,
							bb = bb,
						),
					)
				}
				hand.streets.turn?.let { turn ->
					add(
						StreetSectionUiModel.from(
							street = Street.TURN,
							actions = turn.actions,
							boardCards = turn.cards,
							previousCards = hand.streets.flop?.cards ?: emptyList(),
							hand = hand,
							heroSeat = heroSeat,
							playerCount = playerCount,
							useBbUnit = useBbUnit,
							bb = bb,
						),
					)
				}
				hand.streets.river?.let { river ->
					add(
						StreetSectionUiModel.from(
							street = Street.RIVER,
							actions = river.actions,
							boardCards = river.cards,
							previousCards = (hand.streets.flop?.cards ?: emptyList()) +
								(hand.streets.turn?.cards ?: emptyList()),
							hand = hand,
							heroSeat = heroSeat,
							playerCount = playerCount,
							useBbUnit = useBbUnit,
							bb = bb,
						),
					)
				}
			}

			return HandDetailUiModel(
				hand = hand,
				heroSeat = heroSeat,
				playerCount = playerCount,
				streetSections = sections,
				useBbUnit = useBbUnit,
			)
		}
	}
}

@Immutable
data class StreetSectionUiModel(
	val street: Street,
	val label: String,
	val boardCards: List<Card>,
	val previousCards: List<Card>,
	val pot: String,
	val actionRows: List<ActionRowUiModel>,
	val foldCount: Int,
) {
	companion object {
		fun from(
			street: Street,
			actions: List<Action>,
			boardCards: List<Card>,
			previousCards: List<Card>,
			hand: HandRecord,
			heroSeat: Int,
			playerCount: Int,
			useBbUnit: Boolean = false,
			bb: Double = 0.0,
		): StreetSectionUiModel {
			val foldCount = if (street == Street.PREFLOP) {
				actions.count { it.type == ActionType.FOLD }
			} else {
				0
			}

			val displayActions = if (street == Street.PREFLOP) {
				actions.filter { it.type != ActionType.FOLD }
			} else {
				actions
			}

			val actionRows = displayActions.map { action ->
				ActionRowUiModel.from(
					action = action,
					street = street,
					heroSeat = heroSeat,
					buttonSeat = hand.buttonSeat,
					playerCount = playerCount,
					useBbUnit = useBbUnit,
					bb = bb,
				)
			}

			val potValue = calculatePotAtStreet(hand, street)

			return StreetSectionUiModel(
				street = street,
				label = street.label,
				boardCards = boardCards,
				previousCards = previousCards,
				pot = formatAmount(potValue, useBbUnit, bb),
				actionRows = actionRows,
				foldCount = foldCount,
			)
		}

		private fun calculatePotAtStreet(hand: HandRecord, street: Street): Double {
			val blinds = hand.blinds
			val blindsPot = (blinds?.sb ?: 0.0) + (blinds?.bb ?: 0.0)
			val antePot = if (blinds?.isBigBlindAnte == true) blinds.bb else 0.0

			val streets = listOf(Street.PREFLOP, Street.FLOP, Street.TURN, Street.RIVER)
			var pot = blindsPot + antePot

			for (s in streets) {
				val actions = hand.streets.getActions(s)
				pot += actions.sumOf { action ->
					when (action.type) {
						ActionType.CALL, ActionType.BET, ActionType.RAISE, ActionType.ALL_IN -> action.amount ?: 0.0
						else -> 0.0
					}
				}
				if (s == street) break
			}
			return pot
		}
	}
}

@Immutable
data class ActionRowUiModel(
	val positionName: String,
	val actionType: ActionType,
	val actionLabel: String,
	val amount: String?,
	val stackBefore: String?,
	val isHero: Boolean,
) {
	companion object {
		fun from(
			action: Action,
			street: Street,
			heroSeat: Int,
			buttonSeat: Int,
			playerCount: Int,
			useBbUnit: Boolean = false,
			bb: Double = 0.0,
		): ActionRowUiModel {
			val label = if (street == Street.PREFLOP && action.betLevel == 2 &&
				(action.type == ActionType.RAISE || action.type == ActionType.ALL_IN)
			) {
				"오픈"
			} else {
				action.label
			}
			return ActionRowUiModel(
				positionName = getPositionName(action.playerSeat, buttonSeat, playerCount),
				actionType = action.type,
				actionLabel = label,
				amount = action.amount?.let { formatAmount(it, useBbUnit, bb) },
				stackBefore = action.stackBefore?.let { formatAmount(it, useBbUnit, bb) },
				isHero = action.playerSeat == heroSeat,
			)
		}

		private fun getPositionName(seat: Int, buttonSeat: Int, count: Int): String {
			val btn = buttonSeat
			val sbSeat = (btn % count) + 1
			val bbSeat = ((btn + 1) % count) + 1

			if (seat == btn) return Position.BTN.label
			if (seat == sbSeat) return Position.SB.label
			if (seat == bbSeat) return Position.BB.label

			val preflopOrder = (1..count).map { offset ->
				((btn + 2 + offset - 1) % count) + 1
			}
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
}

/** 금액을 포맷: BB 모드 또는 쉼표 구분 */
private fun formatAmount(amount: Double, useBbUnit: Boolean, bb: Double): String {
	return if (useBbUnit && bb > 0) {
		val bbCount = (amount * 10 / bb).toLong() / 10.0
		if (bbCount == bbCount.toLong().toDouble()) {
			"${bbCount.toLong()}BB"
		} else {
			"${bbCount}BB"
		}
	} else {
		formatWithComma(amount.toLong())
	}
}

/** 숫자에 천 단위 쉼표 추가 */
internal fun formatWithComma(value: Long): String {
	val str = value.toString()
	val isNegative = value < 0
	val digits = if (isNegative) str.drop(1) else str
	val result = buildString {
		digits.reversed().forEachIndexed { index, c ->
			if (index > 0 && index % 3 == 0) append(',')
			append(c)
		}
	}.reversed()
	return if (isNegative) "-$result" else result
}

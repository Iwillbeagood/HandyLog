package com.hand.log.handdetail.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.hand.log.designsystem.etc.ThemePreview
import com.hand.log.designsystem.etc.ThemePreviews
import com.hand.log.designsystem.theme.HandyTheme
import com.hand.log.domain.model.Action
import com.hand.log.domain.model.ActionType
import com.hand.log.domain.model.Blinds
import com.hand.log.domain.model.Card
import com.hand.log.domain.model.FlopStreet
import com.hand.log.domain.model.HandPlayer
import com.hand.log.domain.model.HandRecord
import com.hand.log.domain.model.HandStreets
import com.hand.log.domain.model.PocketCards
import com.hand.log.domain.model.PreflopStreet
import com.hand.log.domain.model.Rank
import com.hand.log.domain.model.RiverStreet
import com.hand.log.domain.model.Street
import com.hand.log.domain.model.Suit
import com.hand.log.domain.model.TurnStreet
import com.hand.log.ui.poker.formatAmountFull
import com.hand.log.ui.poker.indicatorColor

@Composable
internal fun ActionGridSection(
	hand: HandRecord,
	useBbUnit: Boolean = false,
	modifier: Modifier = Modifier,
) {
	val colors = HandyTheme.colorScheme
	val bb = hand.bbAmount

	// 스트릿별 데이터 준비 (프리플랍 폴드 제외)
	val preflopActions = hand.streets.preflop.actions.filter { it.type != ActionType.FOLD }
	val streets = buildList {
		add(
			StreetColumn(
				"Pre-Flop",
				preflopActions,
				formatAmountFull(hand.getPotAtStreet(Street.PREFLOP), useBbUnit, bb),
			),
		)
		hand.streets.flop?.let {
			add(
				StreetColumn(
					"Flop",
					it.actions,
					formatAmountFull(hand.getPotAtStreet(Street.FLOP), useBbUnit, bb),
				),
			)
		}
		hand.streets.turn?.let {
			add(
				StreetColumn(
					"Turn",
					it.actions,
					formatAmountFull(hand.getPotAtStreet(Street.TURN), useBbUnit, bb),
				),
			)
		}
		hand.streets.river?.let {
			add(
				StreetColumn(
					"River",
					it.actions,
					formatAmountFull(hand.getPotAtStreet(Street.RIVER), useBbUnit, bb),
				),
			)
		}
	}

	// 각 스트릿의 최대 액션 수
	val maxRows = streets.maxOf { if (it.name == "Blinds") 2 else it.actions.size.coerceAtLeast(1) }
	val blindsWidth = 56.dp

	Column(
		modifier = modifier
			.fillMaxWidth()
			.clip(RoundedCornerShape(12.dp))
			.background(colors.card),
	) {
		// 스트릿 헤더 + 팟
		Row(
			modifier = Modifier.fillMaxWidth(),
		) {
			streets.forEach { street ->
				val columnModifier = if (street.name == "Blinds") {
					Modifier.width(blindsWidth)
				} else {
					Modifier.weight(1f)
				}
				Column(
					modifier = columnModifier
						.background(colors.muted)
						.border(0.5.dp, colors.border)
						.padding(vertical = 6.dp),
					horizontalAlignment = Alignment.CenterHorizontally,
				) {
					Text(
						text = street.name,
						style = HandyTheme.typography.bold10,
						color = colors.textPrimary,
						textAlign = TextAlign.Center,
					)
					Text(
						text = street.pot,
						style = HandyTheme.typography.regular8,
						color = colors.gold,
						textAlign = TextAlign.Center,
					)
				}
			}
		}

		// 액션 그리드
		for (rowIndex in 0 until maxRows) {
			Row(
				modifier = Modifier
					.fillMaxWidth()
					.height(IntrinsicSize.Min),
			) {
				streets.forEachIndexed { colIndex, street ->
					val cellContent = when {
						street.name == "Blinds" -> {
							when (rowIndex) {
								0 -> hand.blinds?.let { "SB ${formatAmountFull(it.sb, useBbUnit, bb)}" }
								1 -> hand.blinds?.let { "BB ${formatAmountFull(it.bb, useBbUnit, bb)}" }
								else -> null
							}
						}
						rowIndex < street.actions.size -> {
							val action = street.actions[rowIndex]
							val pos = hand.getPositionName(action.playerSeat)
							val isHero = action.playerSeat == hand.heroSeat
							val prefix = if (isHero) "$pos (HERO)" else pos
							formatActionCell(prefix, action, bb, useBbUnit)
						}
						else -> null
					}

					val actionType = if (street.name != "Blinds" && rowIndex < street.actions.size) {
						street.actions[rowIndex].type
					} else {
						null
					}
					val cellModifier = if (street.name == "Blinds") {
						Modifier.width(blindsWidth)
					} else {
						Modifier.weight(1f)
					}

					ActionCell(
						text = cellContent,
						actionType = actionType,
						modifier = cellModifier,
					)
				}
			}
		}
	}
}

@Composable
private fun ActionCell(
	text: String?,
	actionType: ActionType?,
	modifier: Modifier = Modifier,
) {
	val colors = HandyTheme.colorScheme
	val textColor = when {
		actionType != null -> actionType.indicatorColor()
		else -> colors.textSecondary
	}

	Box(
		modifier = modifier
			.fillMaxHeight()
			.border(0.5.dp, colors.border.copy(alpha = 0.3f))
			.padding(horizontal = 4.dp, vertical = 6.dp),
		contentAlignment = Alignment.Center,
	) {
		if (text != null) {
			Text(
				text = text,
				style = HandyTheme.typography.regular10,
				color = textColor,
				textAlign = TextAlign.Center,
				maxLines = 2,
			)
		}
	}
}

private data class StreetColumn(
	val name: String,
	val actions: List<Action>,
	val pot: String,
)

private fun formatActionCell(prefix: String, action: Action, bb: Double, useBbUnit: Boolean): String {
	val amount = action.amount?.let { formatAmountFull(it, useBbUnit, bb) } ?: ""
	return when (action.type) {
		ActionType.FOLD -> "$prefix\nFold"
		ActionType.CHECK -> "$prefix\nCheck"
		ActionType.CALL -> "$prefix\nCall $amount"
		ActionType.BET -> "$prefix\nBet $amount"
		ActionType.RAISE -> {
			val label = if (action.betLevel <= 2) "Raise" else "${action.betLevel}-Bet"
			"$prefix\n$label $amount"
		}
		ActionType.ALL_IN -> "$prefix\nAll-in $amount"
	}
}

@ThemePreviews
@Composable
private fun ActionGridSectionPreview() {
	ThemePreview {
		ActionGridSection(
			hand = HandRecord(
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
					river = RiverStreet(card = Card(Rank.TWO, Suit.CLUBS)),
				),
				players = listOf(
					HandPlayer(
						seat = 3,
						cards = PocketCards(Card(Rank.ACE, Suit.SPADES), Card(Rank.KING, Suit.SPADES)),
						initialStack = 50000.0,
						isHero = true,
					),
				),
				result = 49000.0,
			),
			useBbUnit = true,
			modifier = Modifier.padding(16.dp),
		)
	}
}

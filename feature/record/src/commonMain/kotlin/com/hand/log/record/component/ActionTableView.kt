package com.hand.log.record.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.clickable
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Icon
import com.hand.log.designsystem.component.ScaleInAnimation
import com.hand.log.designsystem.component.VerticalSpacer
import com.hand.log.ui.localizedLabel
import com.hand.log.designsystem.theme.HandyTheme
import com.hand.log.designsystem.theme.nonScaledSp
import com.hand.log.domain.model.Action
import com.hand.log.domain.model.Card
import com.hand.log.ui.poker.AllInMarker
import com.hand.log.ui.poker.CardSize
import com.hand.log.ui.poker.PlayingCard
import com.hand.log.ui.poker.indicatorColor
import handylog.core.res.generated.resources.*
import org.jetbrains.compose.resources.painterResource
import com.hand.log.domain.model.ActionType
import com.hand.log.domain.model.Blinds
import com.hand.log.domain.model.GameType
import com.hand.log.domain.model.PokerTable
import com.hand.log.domain.model.Rank
import com.hand.log.domain.model.Street
import com.hand.log.domain.model.FlopStreet
import com.hand.log.domain.model.PreflopStreet
import com.hand.log.domain.model.PocketCards
import com.hand.log.record.model.PlayerStatus
import com.hand.log.record.model.RecordPlayer
import com.hand.log.record.model.RecordPlayers
import com.hand.log.domain.model.HandStreets
import com.hand.log.domain.model.Suit
import com.hand.log.record.contract.RecordHandState
import com.hand.log.record.contract.RecordStep
import kotlinx.datetime.LocalDate
import com.hand.log.designsystem.etc.ThemePreview
import com.hand.log.designsystem.etc.ThemePreviews
import org.jetbrains.compose.resources.stringResource
import handylog.core.res.generated.resources.Res
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

@Composable
internal fun ActionTableView(
	state: RecordHandState.Recording,
	modifier: Modifier = Modifier,
	onSeatClick: ((Int) -> Unit)? = null,
) {
	val colors = HandyTheme.colorScheme
	val playerCount = state.table?.playerCount ?: return
	val actions = state.streets.getActions(state.currentStreet)

	val seatActions = mutableMapOf<Int, Action>()
	actions.forEach { action ->
		seatActions[action.playerSeat] = action
	}

	val allInSeats = state.players.allInSeats

	BoxWithConstraints(
		modifier = modifier
			.padding(horizontal = 8.dp)
			.aspectRatio(1.3f),
	) {
		val density = LocalDensity.current
		val containerWidthPx = with(density) { maxWidth.toPx() }
		val containerHeightPx = with(density) { maxHeight.toPx() }

		Box(
			modifier = Modifier
				.fillMaxWidth(0.75f)
				.aspectRatio(1.8f)
				.align(Alignment.Center)
				.clip(RoundedCornerShape(40))
				.background(colors.felt)
				.border(2.dp, colors.feltLight, RoundedCornerShape(40)),
			contentAlignment = Alignment.Center,
		) {
			val sidePots = state.sidePots
			val mainPot = if (sidePots.isNotEmpty()) sidePots.first() else state.currentPot

			Column(
				horizontalAlignment = Alignment.CenterHorizontally,
				verticalArrangement = Arrangement.spacedBy(1.dp),
			) {
				if (state.blinds != null) {
					Text(
						text = "SB: ${state.blinds.sb.toLong()} / BB: ${state.blinds.bb.toLong()}",
						style = HandyTheme.typography.regular8.nonScaledSp,
						color = colors.gold.copy(alpha = 0.7f),
					)
				}
				Text(
					text = "POT: ${state.formatAmount(mainPot)}",
					style = HandyTheme.typography.bold14.nonScaledSp,
					color = colors.gold.copy(alpha = 0.8f),
				)
				if (state.currentStreet == Street.PREFLOP && state.blinds?.isBigBlindAnte == true) {
					Text(
						text = "Ante: ${state.blinds.bb.toLong()}",
						style = HandyTheme.typography.regular10.nonScaledSp,
						color = Color.White.copy(alpha = 0.7f),
					)
				}

				// 사이드 팟 (메인 팟 제외)
				if (sidePots.size > 1) {
					VerticalSpacer(2.dp)
					Row(
						horizontalArrangement = Arrangement.spacedBy(4.dp),
					) {
						sidePots.drop(1).forEachIndexed { index, pot ->
							Box(
								modifier = Modifier
									.clip(RoundedCornerShape(10.dp))
									.background(colors.primary.copy(alpha = 0.2f))
									.padding(horizontal = 6.dp, vertical = 2.dp),
								contentAlignment = Alignment.Center,
							) {
								Text(
									text = "Side${if (sidePots.size > 2) " ${index + 1}" else ""} ${state.formatAmount(pot)}",
									style = HandyTheme.typography.bold8.nonScaledSp,
									color = colors.primary,
								)
							}
						}
					}
				}
			}
		}

		val centerX = containerWidthPx / 2f
		val centerY = containerHeightPx / 2f
		val tableWidth = containerWidthPx * 0.75f
		val tableHeight = tableWidth / 1.8f
		val tableRadiusX = tableWidth / 2f
		val tableRadiusY = tableHeight / 2f

		val seatSizeDp = 48.dp
		val seatSizePx = with(density) { seatSizeDp.toPx() }
		val seatCircleRadius = with(density) { 16.dp.toPx() }
		val chipSizeDp = 16.dp
		val dealerSizeDp = 16.dp
		val chipSizePx = with(density) { chipSizeDp.toPx() }
		val chipHalf = chipSizePx / 2f
		val gapPx = with(density) { 1.dp.toPx() }
		val seatHalf = seatSizePx / 2f
		val chipGapPx = with(density) { 12.dp.toPx() }

		val btnSeat = state.buttonSeat
		val sbSeat = (btnSeat % playerCount) + 1
		val bbSeat = ((btnSeat + 1) % playerCount) + 1

		// 딜러 버튼: 칩/좌석보다 먼저 그려서 아래(뒤)에 위치
		run {
			val btnAngle = (2 * kotlin.math.PI * (btnSeat - 1) / playerCount) - (kotlin.math.PI / 2)
			val sbAngle = (2 * kotlin.math.PI * (sbSeat - 1) / playerCount) - (kotlin.math.PI / 2)
			val midAngle = if (sbAngle > btnAngle) {
				(btnAngle + sbAngle) / 2.0
			} else {
				val adjusted = sbAngle + 2 * kotlin.math.PI
				((btnAngle + adjusted) / 2.0) % (2 * kotlin.math.PI)
			}
			val dealerSizePx = with(density) { dealerSizeDp.toPx() }
			val dealerCosA = cos(midAngle).toFloat()
			val dealerSinA = sin(midAngle).toFloat()
			val dealerMargin = dealerSizePx / 2f + with(density) { 2.dp.toPx() }
			val dealerCenterX = centerX + (tableRadiusX - dealerMargin) * dealerCosA
			val dealerCenterY = centerY + (tableRadiusY - dealerMargin) * dealerSinA
			val dealerX = dealerCenterX - dealerSizePx / 2f
			val dealerY = dealerCenterY - dealerSizePx / 2f

			ScaleInAnimation(
				modifier = Modifier.offset { IntOffset(dealerX.roundToInt(), dealerY.roundToInt()) },
			) {
				Box(
					modifier = Modifier
						.size(dealerSizeDp)
						.clip(CircleShape)
						.background(Color(0xFF3A3A3A)),
					contentAlignment = Alignment.Center,
				) {
					Text(
						text = "D",
						style = HandyTheme.typography.bold8.nonScaledSp,
						color = Color.White,
						textAlign = TextAlign.Center,
					)
				}
			}
		}

		for (seat in 1..playerCount) {
			val angle = (2 * kotlin.math.PI * (seat - 1) / playerCount) - (kotlin.math.PI / 2)
			val cosA = cos(angle).toFloat()
			val sinA = sin(angle).toFloat()

			val seatCenterX = centerX + (tableRadiusX + gapPx + seatHalf) * cosA
			val seatCenterY = centerY + (tableRadiusY + gapPx + seatHalf) * sinA
			val sx = seatCenterX - seatSizePx / 2f
			val sy = seatCenterY - seatSizePx / 2f

			val action = seatActions[seat]
			val isHero = seat == state.table.heroSeat
			val isCurrent = seat == state.currentActionSeat
			val posName = state.positionName(seat)
			val isFolded = seat in state.players.foldedSeats && action == null

			val seatModifier = if (isHero && state.heroHand != null) {
				Modifier.offset { IntOffset(sx.roundToInt(), sy.roundToInt()) }
			} else {
				Modifier
					.size(seatSizeDp)
					.offset { IntOffset(sx.roundToInt(), sy.roundToInt()) }
			}.let { mod ->
				if (onSeatClick != null) mod.clickable { onSeatClick(seat) } else mod
			}

			ActionSeatView(
				seatNumber = seat,
				positionName = posName,
				action = action,
				isHero = isHero,
				isCurrent = isCurrent,
				isFolded = isFolded,
				heroHand = if (isHero) state.heroHand else null,
				modifier = seatModifier,
			)

			val isBtn = seat == btnSeat
			val isPreflop = state.currentStreet == Street.PREFLOP
			val totalBet = action?.amount ?: 0.0
			val hasBetAction = totalBet > 0 && action?.type != ActionType.FOLD
			val chipLabel = when {
				isBtn -> "D"
				isPreflop && !hasBetAction && seat == sbSeat -> "SB"
				isPreflop && !hasBetAction && seat == bbSeat -> "BB"
				else -> null
			}
			val chipColor = when {
				isBtn -> colors.accent
				isPreflop && !hasBetAction && (seat == sbSeat || seat == bbSeat) -> colors.gold
				else -> null
			}
			val blindAmt = when {
				isPreflop && !hasBetAction && seat == sbSeat -> state.blinds?.sb
				isPreflop && !hasBetAction && seat == bbSeat -> state.blinds?.bb
				else -> null
			}

			if (chipLabel != null && chipColor != null && !isBtn) {
				val cx = centerX + (tableRadiusX - chipHalf - chipGapPx) * cosA - chipHalf
				val cy = centerY + (tableRadiusY - chipHalf - chipGapPx) * sinA - chipHalf

				ScaleInAnimation(
					modifier = Modifier.offset { IntOffset(cx.roundToInt(), cy.roundToInt()) },
				) {
					Column(
						horizontalAlignment = Alignment.CenterHorizontally,
					) {
						Box(
							modifier = Modifier.size(chipSizeDp),
							contentAlignment = Alignment.Center,
						) {
							Icon(
								painter = painterResource(Res.drawable.poker_chip),
								contentDescription = null,
								modifier = Modifier.size(chipSizeDp),
								tint = chipColor,
							)
						}
						if (blindAmt != null) {
							Text(
								text = state.formatAmount(blindAmt),
								style = HandyTheme.typography.bold8.nonScaledSp,
								color = colors.gold,
								modifier = Modifier.padding(top = 1.dp),
							)
						}
					}
				}
			}

			val isAllIn = seat in allInSeats
			if (isAllIn) {
				val ax = centerX + (tableRadiusX - chipHalf - chipGapPx) * cosA - chipHalf
				val ay = centerY + (tableRadiusY - chipHalf - chipGapPx) * sinA - chipHalf

				val allInAmount = action?.takeIf { it.type == ActionType.ALL_IN }?.amount
					?: state.streets.let { streets ->
						listOf(Street.PREFLOP, Street.FLOP, Street.TURN, Street.RIVER)
							.flatMap { streets.getActions(it) }
							.lastOrNull { it.playerSeat == seat && it.type == ActionType.ALL_IN }
							?.amount
					}

				ScaleInAnimation(
					modifier = Modifier.offset { IntOffset(ax.roundToInt(), ay.roundToInt()) },
				) {
					AllInMarker(
						size = dealerSizeDp,
						amount = allInAmount?.let { state.formatAmount(it) },
					)
				}
			}

			val isBlindChipShown = chipLabel == "SB" || chipLabel == "BB"
			if (hasBetAction && !isAllIn && !isBlindChipShown) {
				val bx = centerX + (tableRadiusX - chipHalf - chipGapPx) * cosA - chipHalf
				val by = centerY + (tableRadiusY - chipHalf - chipGapPx) * sinA - chipHalf

				ScaleInAnimation(
					modifier = Modifier.offset { IntOffset(bx.roundToInt(), by.roundToInt()) },
				) {
					Column(
						horizontalAlignment = Alignment.CenterHorizontally,
					) {
						Box(
							modifier = Modifier.size(chipSizeDp),
							contentAlignment = Alignment.Center,
						) {
							Icon(
								painter = painterResource(Res.drawable.poker_chip),
								contentDescription = null,
								modifier = Modifier.size(chipSizeDp),
								tint = action?.type?.indicatorColor() ?: colors.primary,
							)
						}
						Text(
							text = state.formatAmount(totalBet),
							style = HandyTheme.typography.bold8.nonScaledSp,
							color = Color.White,
							modifier = Modifier
								.clip(RoundedCornerShape(4.dp))
								.background(Color.Black.copy(alpha = 0.6f))
								.padding(horizontal = 3.dp, vertical = 1.dp),
						)
					}
				}
			}
		}

	}
}

@Composable
private fun ActionSeatView(
	seatNumber: Int,
	positionName: String,
	action: Action?,
	isHero: Boolean,
	isCurrent: Boolean,
	isFolded: Boolean = false,
	heroHand: PocketCards? = null,
	modifier: Modifier = Modifier,
) {
	val colors = HandyTheme.colorScheme
	val hasFolded = action?.type == ActionType.FOLD || isFolded

	val actionColor = action?.type?.indicatorColor()

	val borderColor = when {
		isCurrent -> colors.primary
		hasFolded -> colors.border.copy(alpha = 0.3f)
		isHero -> colors.gold
		actionColor != null -> actionColor
		else -> colors.border
	}

	val bgColor = when {
		isCurrent -> colors.primary.copy(alpha = 0.15f)
		hasFolded -> colors.muted.copy(alpha = 0.3f)
		isHero -> colors.gold.copy(alpha = 0.15f)
		else -> colors.muted
	}

	val textAlpha = if (hasFolded) 0.4f else 1f

	Column(
		modifier = modifier,
		horizontalAlignment = Alignment.CenterHorizontally,
		verticalArrangement = Arrangement.Center,
	) {
		Row(
			verticalAlignment = Alignment.CenterVertically,
			horizontalArrangement = Arrangement.spacedBy(4.dp),
		) {
			Column(horizontalAlignment = Alignment.CenterHorizontally) {
				if (isHero) {
					Icon(
						painter = painterResource(Res.drawable.crown),
						contentDescription = null,
						modifier = Modifier.size(12.dp),
						tint = colors.gold,
					)
				}
				Box(
					modifier = Modifier
						.requiredSize(32.dp)
						.clip(CircleShape)
						.background(bgColor)
						.border(
							width = if (isCurrent || isHero) 2.dp else 1.dp,
							color = borderColor,
							shape = CircleShape,
						),
					contentAlignment = Alignment.Center,
				) {
					Text(
						text = positionName,
						style = HandyTheme.typography.bold8.nonScaledSp,
						color = when {
							isHero -> colors.gold
							isCurrent -> colors.primary
							else -> colors.textPrimary.copy(alpha = textAlpha)
						},
						textAlign = TextAlign.Center,
						maxLines = 1,
					)
				}

				if (action != null) {
					Text(
						text = action.localizedLabel(),
						style = HandyTheme.typography.bold8.nonScaledSp,
						color = (actionColor ?: colors.textSecondary).copy(alpha = textAlpha),
						maxLines = 1,
						overflow = TextOverflow.Ellipsis,
					)
				} else if (isCurrent) {
					Text(
						text = "▶",
						style = HandyTheme.typography.bold8.nonScaledSp,
						color = colors.primary,
					)
				} else if (isFolded) {
					Text(
						text = stringResource(Res.string.action_fold),
						style = HandyTheme.typography.bold8.nonScaledSp,
						color = colors.textSecondary.copy(alpha = 0.4f),
						maxLines = 1,
					)
				}
			}

			heroHand?.let { pocket ->
				Row(horizontalArrangement = Arrangement.spacedBy((-4).dp)) {
					listOf(pocket.card1, pocket.card2).forEach { card ->
						PlayingCard(
							card = card,
							size = CardSize.XXS,
							modifier = Modifier.requiredSize(CardSize.XXS.width, CardSize.XXS.height),
						)
					}
				}
			}
		}
	}
}

@ThemePreviews
@Composable
private fun ActionTableViewAllElementsPreview() {
	ThemePreview {
		ActionTableView(
			state = RecordHandState.Recording(
				tableId = "test",
				table = PokerTable(
					id = "test",
					date = LocalDate(2026, 3, 14),
					gameType = GameType.TOURNAMENT,
					startingStack = 50000.0,
					blinds = Blinds(sb = 500.0, bb = 1000.0, isBigBlindAnte = true),
					playerCount = 9,
					heroSeat = 3,
					createdAt = 0L,
				),
				heroHand = PocketCards(Card(Rank.ACE, Suit.SPADES), Card(Rank.KING, Suit.HEARTS)),
				players = RecordPlayers(
					player1 = RecordPlayer(seat = 1, stack = 50000.0),
					player2 = RecordPlayer(seat = 2, stack = 49500.0),
					player3 = RecordPlayer(seat = 3, stack = 49000.0),
					player4 = RecordPlayer(seat = 4, stack = 47500.0),
					player5 = RecordPlayer(seat = 5, stack = 50000.0, status = PlayerStatus.FOLDED),
					player6 = RecordPlayer(seat = 6, stack = 42000.0),
					player7 = RecordPlayer(seat = 7, stack = 42000.0),
					player8 = RecordPlayer(seat = 8, stack = 0.0, status = PlayerStatus.ALL_IN),
					player9 = RecordPlayer(seat = 9, stack = 50000.0),
				),
				currentStep = RecordStep.PREFLOP,
				buttonSeat = 4,
				blinds = Blinds(sb = 500.0, bb = 1000.0, isBigBlindAnte = true),
				currentActionSeat = 9,
				streets = HandStreets(
					preflop = PreflopStreet(
						actions = listOf(
							Action(playerSeat = 4, type = ActionType.RAISE, amount = 2500.0),
							Action(playerSeat = 5, type = ActionType.FOLD),
							Action(playerSeat = 6, type = ActionType.RAISE, amount = 8000.0),
							Action(playerSeat = 7, type = ActionType.CALL, amount = 8000.0),
							Action(playerSeat = 8, type = ActionType.ALL_IN, amount = 50000.0),
						),
					),
				),
			),
			modifier = Modifier
				.fillMaxWidth()
				.padding(16.dp),
		)
	}
}

@ThemePreviews
@Composable
private fun ActionTableView9MaxFlopPreview() {
	ThemePreview {
		ActionTableView(
			state = RecordHandState.Recording(
				tableId = "test",
				table = PokerTable(
					id = "test",
					date = LocalDate(2026, 3, 14),
					gameType = GameType.CASH,
					startingStack = 50000.0,
					blinds = Blinds(sb = 500.0, bb = 1000.0),
					playerCount = 9,
					heroSeat = 3,
					createdAt = 0L,
				),
				heroHand = PocketCards(Card(Rank.ACE, Suit.SPADES), Card(Rank.KING, Suit.HEARTS)),
				players = RecordPlayers(
					player1 = RecordPlayer(seat = 1, stack = 47000.0),
					player2 = RecordPlayer(seat = 2, stack = 47000.0),
					player3 = RecordPlayer(seat = 3, stack = 44000.0),
					player4 = RecordPlayer(seat = 4, stack = 47000.0),
					player5 = RecordPlayer(seat = 5, stack = 50000.0, status = PlayerStatus.FOLDED),
					player6 = RecordPlayer(seat = 6, stack = 50000.0, status = PlayerStatus.FOLDED),
					player7 = RecordPlayer(seat = 7, stack = 50000.0, status = PlayerStatus.FOLDED),
					player8 = RecordPlayer(seat = 8, stack = 50000.0, status = PlayerStatus.FOLDED),
					player9 = RecordPlayer(seat = 9, stack = 50000.0, status = PlayerStatus.FOLDED),
				),
				currentStep = RecordStep.FLOP,
				buttonSeat = 1,
				blinds = Blinds(sb = 500.0, bb = 1000.0),
				currentActionSeat = 4,
				streets = HandStreets(
					preflop = PreflopStreet(
						actions = listOf(
							Action(playerSeat = 4, type = ActionType.RAISE, amount = 2500.0),
							Action(playerSeat = 5, type = ActionType.FOLD),
							Action(playerSeat = 6, type = ActionType.FOLD),
							Action(playerSeat = 7, type = ActionType.FOLD),
							Action(playerSeat = 8, type = ActionType.FOLD),
							Action(playerSeat = 9, type = ActionType.FOLD),
							Action(playerSeat = 1, type = ActionType.FOLD),
							Action(playerSeat = 2, type = ActionType.FOLD),
							Action(playerSeat = 3, type = ActionType.CALL, amount = 2500.0),
						),
					),
					flop = FlopStreet(
						card1 = Card(Rank.ACE, Suit.HEARTS),
						card2 = Card(Rank.KING, Suit.DIAMONDS),
						card3 = Card(Rank.QUEEN, Suit.CLUBS),
						actions = listOf(
							Action(playerSeat = 3, type = ActionType.BET, amount = 6000.0),
						),
					),
				),
			),
			modifier = Modifier
				.fillMaxWidth()
				.padding(16.dp),
		)
	}
}

package com.hand.log.record.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Icon
import com.hand.log.designsystem.theme.HandyTheme
import com.hand.log.designsystem.theme.nonScaledSp
import com.hand.log.domain.model.Action
import com.hand.log.domain.model.Card
import com.hand.log.ui.poker.CardSize
import com.hand.log.ui.poker.PlayingCard
import com.hand.log.ui.poker.indicatorColor
import handylog.core.res.generated.resources.Res
import handylog.core.res.generated.resources.crown
import handylog.core.res.generated.resources.poker_chip
import org.jetbrains.compose.resources.painterResource
import com.hand.log.domain.model.ActionType
import com.hand.log.domain.model.Blinds
import com.hand.log.domain.model.GameType
import com.hand.log.domain.model.PokerTable
import com.hand.log.domain.model.Rank
import com.hand.log.domain.model.Street
import com.hand.log.domain.model.FlopStreet
import com.hand.log.domain.model.PreflopStreet
import com.hand.log.domain.model.HeroHand
import com.hand.log.record.model.PlayerStatus
import com.hand.log.record.model.RecordPlayer
import com.hand.log.record.model.RecordPlayers
import com.hand.log.record.model.RecordStreets
import com.hand.log.domain.model.Suit
import com.hand.log.record.contract.RecordHandState
import com.hand.log.record.contract.RecordStep
import kotlinx.datetime.LocalDate
import com.hand.log.designsystem.etc.ThemePreview
import com.hand.log.designsystem.etc.ThemePreviews
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

@Composable
internal fun ActionTableView(
	state: RecordHandState.Recording,
	modifier: Modifier = Modifier,
) {
	val colors = HandyTheme.colorScheme
	val playerCount = state.table?.playerCount ?: return
	val actions = state.streets.getActions(state.currentStreet)

	// 각 좌석의 마지막 액션
	val seatActions = mutableMapOf<Int, Action>()
	actions.forEach { action ->
		seatActions[action.playerSeat] = action
	}

	val allInSeats = state.allInSeats

	// 각 좌석 액션의 벳 레벨 라벨 계산
	val actionLabels = mutableMapOf<Int, String>()
	var betLevel = if (state.currentStreet == Street.PREFLOP) 1 else 0 // 프리플랍: BB=1벳
	actions.forEach { action ->
		if (action.type == ActionType.BET || action.type == ActionType.RAISE ||
			(action.type == ActionType.ALL_IN && (action.amount ?: 0.0) > 0)
		) {
			betLevel++
			val label = when {
				action.type == ActionType.ALL_IN -> "올인"
				state.currentStreet != Street.PREFLOP && betLevel == 1 -> "벳"
				betLevel == 2 -> "레이즈"
				else -> "${betLevel}벳"
			}
			actionLabels[action.playerSeat] = label
		}
	}

	Box(
		modifier = modifier
			.background(colors.card, RoundedCornerShape(12.dp))
			.padding(horizontal = 8.dp),
	) {
		BoxWithConstraints(
			modifier = Modifier
				.fillMaxWidth()
				.aspectRatio(1.1f),
		) {
			val density = LocalDensity.current
			val containerWidthPx = with(density) { maxWidth.toPx() }
			val containerHeightPx = with(density) { maxHeight.toPx() }

			// Oval table
			Box(
				modifier = Modifier
					.fillMaxWidth(0.80f)
					.aspectRatio(1.8f)
					.align(Alignment.Center)
					.clip(RoundedCornerShape(40))
					.background(colors.felt)
					.border(2.dp, colors.feltLight, RoundedCornerShape(40)),
				contentAlignment = Alignment.Center,
			) {
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
						text = "POT: ${state.currentPot.toLong()}",
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
				}
			}

			// Seats
			val seatRadiusX = containerWidthPx * 0.42f
			val seatRadiusY = containerHeightPx * 0.32f
			val chipRadiusX = containerWidthPx * 0.28f
			val chipRadiusY = containerHeightPx * 0.18f
			val betChipRadiusX = containerWidthPx * 0.28f
			val betChipRadiusY = containerHeightPx * 0.18f
			val centerX = containerWidthPx / 2f
			val centerY = containerHeightPx / 2f
			val seatSizeDp = 48.dp
			val seatSizePx = with(density) { seatSizeDp.toPx() }
			val chipSizeDp = 16.dp
			val dealerSizeDp = 16.dp
			val chipSizePx = with(density) { chipSizeDp.toPx() }
			val btnSeat = state.buttonSeat
			val sbSeat = (btnSeat % playerCount) + 1
			val bbSeat = ((btnSeat + 1) % playerCount) + 1

			for (seat in 1..playerCount) {
				val angle = (2 * kotlin.math.PI * (seat - 1) / playerCount) - (kotlin.math.PI / 2)

				// Seat (outer)
				val sx = centerX + seatRadiusX * cos(angle).toFloat() - seatSizePx / 2f
				val sy = centerY + seatRadiusY * sin(angle).toFloat() - seatSizePx / 2f

				val action = seatActions[seat]
				val isHero = seat == state.table.heroSeat
				val isCurrent = seat == state.currentActionSeat
				val posName = state.positionName(seat)
				val isFolded = seat in state.foldedSeats && action == null

				ActionSeatView(
					seatNumber = seat,
					positionName = posName,
					action = action,
					actionLabel = actionLabels[seat],
					isHero = isHero,
					isCurrent = isCurrent,
					isFolded = isFolded,
					heroCards = if (isHero) state.heroCards else emptyList(),
					modifier = if (isHero && state.heroCards.isNotEmpty()) {
						Modifier.offset { IntOffset(sx.roundToInt(), sy.roundToInt()) }
					} else {
						Modifier
							.size(seatSizeDp)
							.offset { IntOffset(sx.roundToInt(), sy.roundToInt()) }
					},
				)

				// Dealer / Blind chips (inner, on the table)
				val isBtn = seat == btnSeat
				val isPreflop = state.currentStreet == Street.PREFLOP
				val actionAmount = action?.amount ?: 0.0
				val hasBetAction = actionAmount > 0 && action?.type != ActionType.FOLD
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
					isPreflop && !hasBetAction && seat == sbSeat -> state.blinds?.sb?.toLong()
					isPreflop && !hasBetAction && seat == bbSeat -> state.blinds?.bb?.toLong()
					else -> null
				}

				if (chipLabel != null && chipColor != null) {
					val dealerOffsetPx = with(density) { 18.dp.toPx() }
					val cx = centerX + chipRadiusX * cos(angle).toFloat() - chipSizePx / 2f
					val cy = centerY + chipRadiusY * sin(angle).toFloat() - chipSizePx / 2f

					if (isBtn) {
						// Dealer: 좌석 왼쪽에 배치
						val dx = cx - dealerOffsetPx
						Box(
							modifier = Modifier
								.offset { IntOffset(dx.roundToInt(), cy.roundToInt()) }
								.size(dealerSizeDp)
								.clip(CircleShape)
								.background(chipColor),
							contentAlignment = Alignment.Center,
						) {
							Text(
								text = "D",
								style = HandyTheme.typography.bold8.nonScaledSp,
								color = colors.card,
								textAlign = TextAlign.Center,
							)
						}
					} else {
						// Blind: poker_chip 아이콘
						Column(
							modifier = Modifier.offset { IntOffset(cx.roundToInt(), cy.roundToInt()) },
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
									text = "$blindAmt",
									style = HandyTheme.typography.bold8.nonScaledSp,
									color = colors.gold,
									modifier = Modifier.padding(top = 1.dp),
								)
							}
						}
					}
				}

				// All-in marker (딜러보다 안쪽)
				val isAllIn = seat in allInSeats
				if (isAllIn) {
					val allInRadiusX = containerWidthPx * 0.22f
					val allInRadiusY = containerHeightPx * 0.22f
					val ax = centerX + allInRadiusX * cos(angle).toFloat() - chipSizePx / 2f
					val ay = centerY + allInRadiusY * sin(angle).toFloat() - chipSizePx / 2f

					// 올인 금액 찾기 (현재 스트릿 또는 이전 스트릿)
					val allInAmount = action?.takeIf { it.type == ActionType.ALL_IN }?.amount
						?: state.streets.let { streets ->
							listOf(Street.PREFLOP, Street.FLOP, Street.TURN, Street.RIVER)
								.flatMap { streets.getActions(it) }
								.lastOrNull { it.playerSeat == seat && it.type == ActionType.ALL_IN }
								?.amount
						}

					Column(
						modifier = Modifier
							.offset { IntOffset(ax.roundToInt(), ay.roundToInt()) },
						horizontalAlignment = Alignment.CenterHorizontally,
					) {
						Box(
							modifier = Modifier.size(dealerSizeDp),
							contentAlignment = Alignment.Center,
						) {
							Canvas(modifier = Modifier.size(dealerSizeDp)) {
								val path = Path().apply {
									moveTo(size.width / 2f, 0f)
									lineTo(size.width, size.height)
									lineTo(0f, size.height)
									close()
								}
								drawPath(path, color = Color(0xFFE84040))
							}
							Text(
								text = "A",
								style = HandyTheme.typography.bold8.nonScaledSp,
								color = Color.White,
								textAlign = TextAlign.Center,
							)
						}
						if (allInAmount != null) {
							Text(
								text = "${allInAmount.toLong()}",
								style = HandyTheme.typography.bold8.nonScaledSp,
								color = Color.White,
								modifier = Modifier
									.clip(RoundedCornerShape(4.dp))
									.padding(top = 3.dp)
									.background(Color(0xFFE84040).copy(alpha = 0.8f))
									.padding(horizontal = 3.dp, vertical = 1.dp),
							)
						}
					}
				}

				// Bet chips on the table (블라인드 칩과만 양립 불가)
				val isBlindChipShown = chipLabel == "SB" || chipLabel == "BB"
				if (hasBetAction && !isAllIn && !isBlindChipShown) {
					val bx = centerX + betChipRadiusX * cos(angle).toFloat() - chipSizePx / 2f
					val by = centerY + betChipRadiusY * sin(angle).toFloat() - chipSizePx / 2f

					val animatedScale by animateFloatAsState(
						targetValue = 1f,
						animationSpec = tween(durationMillis = 300),
						label = "betChip_$seat",
					)

					Column(
						modifier = Modifier
							.offset { IntOffset(bx.roundToInt(), by.roundToInt()) }
							.graphicsLayer {
								scaleX = animatedScale
								scaleY = animatedScale
							},
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
							text = "${actionAmount.toLong()}",
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
	actionLabel: String? = null,
	isHero: Boolean,
	isCurrent: Boolean,
	isFolded: Boolean = false,
	heroCards: List<Card> = emptyList(),
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
			}

			if (heroCards.isNotEmpty()) {
				Row(horizontalArrangement = Arrangement.spacedBy((-4).dp)) {
					heroCards.forEach { card ->
						PlayingCard(
							card = card,
							size = CardSize.XXS,
							modifier = Modifier.requiredSize(CardSize.XXS.width, CardSize.XXS.height),
						)
					}
				}
			}
		}

		// Action label
		if (action != null) {
			Text(
				text = actionLabel ?: action.type.label,
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
				text = "폴드",
				style = HandyTheme.typography.bold8.nonScaledSp,
				color = colors.textSecondary.copy(alpha = 0.4f),
				maxLines = 1,
			)
		}
	}
}

@ThemePreviews
@Composable
private fun ActionTableViewAllElementsPreview() {
	ThemePreview {
		// 9인 프리플랍: 딜러(1), SB(2), BB(3), UTG 레이즈, UTG+1 폴드,
		// LJ 3벳, HJ 콜, CO 올인, 현재 BTN 액션 중, 히어로=3(BB) 카드 표시
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
				heroHand = HeroHand(Card(Rank.ACE, Suit.SPADES), Card(Rank.KING, Suit.HEARTS)),
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
				currentStreet = Street.PREFLOP,
				buttonSeat = 1,
				blinds = Blinds(sb = 500.0, bb = 1000.0, isBigBlindAnte = true),
				currentActionSeat = 9, // 현재 MP 액션 중
				streets = RecordStreets(
					preflop = PreflopStreet(
						actions = listOf(
							Action(playerSeat = 4, type = ActionType.RAISE, amount = 2500.0), // UTG 레이즈
							Action(playerSeat = 5, type = ActionType.FOLD), // UTG+1 폴드
							Action(playerSeat = 6, type = ActionType.RAISE, amount = 8000.0), // LJ 3벳
							Action(playerSeat = 7, type = ActionType.CALL, amount = 8000.0), // HJ 콜
							Action(playerSeat = 8, type = ActionType.ALL_IN, amount = 50000.0), // CO 올인
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
private fun ActionTableView6MaxFlopPreview() {
	ThemePreview {
		// 6인 플랍: 딜러(4), 폴드(5,6), 히어로=3 베팅, 현재 seat1 액션 중
		ActionTableView(
			state = RecordHandState.Recording(
				tableId = "test",
				table = PokerTable(
					id = "test",
					date = LocalDate(2026, 3, 14),
					gameType = GameType.CASH,
					startingStack = 50000.0,
					blinds = Blinds(sb = 500.0, bb = 1000.0),
					playerCount = 6,
					heroSeat = 3,
					createdAt = 0L,
				),
				heroHand = HeroHand(Card(Rank.ACE, Suit.SPADES), Card(Rank.KING, Suit.HEARTS)),
				players = RecordPlayers(
					player1 = RecordPlayer(seat = 1, stack = 47000.0),
					player2 = RecordPlayer(seat = 2, stack = 47000.0),
					player3 = RecordPlayer(seat = 3, stack = 44000.0),
					player4 = RecordPlayer(seat = 4, stack = 47000.0),
					player5 = RecordPlayer(seat = 5, stack = 50000.0, status = PlayerStatus.FOLDED),
					player6 = RecordPlayer(seat = 6, stack = 50000.0, status = PlayerStatus.FOLDED),
				),
				currentStep = RecordStep.FLOP,
				currentStreet = Street.FLOP,
				buttonSeat = 4,
				blinds = Blinds(sb = 500.0, bb = 1000.0),
				currentActionSeat = 1,
				streets = RecordStreets(
					preflop = PreflopStreet(
						actions = listOf(
							Action(playerSeat = 1, type = ActionType.CALL, amount = 1000.0),
							Action(playerSeat = 2, type = ActionType.CALL, amount = 1000.0),
							Action(playerSeat = 3, type = ActionType.RAISE, amount = 3000.0),
							Action(playerSeat = 4, type = ActionType.CALL, amount = 3000.0),
							Action(playerSeat = 5, type = ActionType.FOLD),
							Action(playerSeat = 6, type = ActionType.FOLD),
							Action(playerSeat = 1, type = ActionType.CALL, amount = 3000.0),
							Action(playerSeat = 2, type = ActionType.CALL, amount = 3000.0),
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

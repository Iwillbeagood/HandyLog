package com.hand.log.handdetail.component

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.hand.log.designsystem.etc.ThemePreview
import com.hand.log.designsystem.etc.ThemePreviews
import com.hand.log.designsystem.theme.HandyTheme
import com.hand.log.designsystem.theme.nonScaledSp
import handylog.core.res.generated.resources.Res
import handylog.core.res.generated.resources.spade_filled
import handylog.core.res.generated.resources.trophy
import org.jetbrains.compose.resources.painterResource
import androidx.compose.material3.Icon
import com.hand.log.domain.model.Action
import com.hand.log.domain.model.ActionType
import com.hand.log.domain.model.Blinds
import com.hand.log.domain.model.Card
import com.hand.log.domain.model.FlopStreet
import com.hand.log.domain.model.HandRecord
import com.hand.log.domain.model.HandStreets
import com.hand.log.domain.model.Position
import com.hand.log.domain.model.PreflopStreet
import com.hand.log.domain.model.Rank
import com.hand.log.domain.model.RiverStreet
import com.hand.log.domain.model.PocketCards
import com.hand.log.domain.model.ShowdownEntry
import com.hand.log.domain.model.Suit
import com.hand.log.domain.model.TurnStreet
import com.hand.log.handdetail.model.formatWithComma
import androidx.compose.ui.unit.Dp
import com.hand.log.ui.poker.CardSize
import com.hand.log.ui.poker.PlayingCard
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

@Composable
internal fun HandDetailTableView(
	hand: HandRecord,
	useBbUnit: Boolean = false,
	modifier: Modifier = Modifier,
	seatCircleSize: Dp = 28.dp,
	seatCardSize: CardSize = CardSize.XXS,
	boardCardSize: CardSize = CardSize.SM,
) {
	val colors = HandyTheme.colorScheme
	val bb = hand.blinds?.bb ?: 1.0
	val playerCount = hand.streets.preflop.actions
		.map { it.playerSeat }.distinct().size.coerceAtLeast(2)
	val buttonSeat = hand.buttonSeat
	val heroSeat = hand.heroSeat
	val boardCards = hand.streets.boardCards

	val allSeats = hand.allSeats
	val foldedSeats = hand.preflopFoldedSeats
	val winnerSeats = hand.winnerSeats

	BoxWithConstraints(
		modifier = modifier
			.fillMaxWidth()
			.clip(RoundedCornerShape(12.dp))
			.background(colors.card)
			.padding(horizontal = 8.dp)
			.aspectRatio(1.4f),
	) {
		// 우측 상단 앱 로고
		Row(
			modifier = Modifier
				.align(Alignment.TopEnd)
				.padding(top = 6.dp, end = 2.dp),
		) {
			SmallLogo()
		}

		val density = LocalDensity.current
		val containerWidthPx = with(density) { maxWidth.toPx() }
		val containerHeightPx = with(density) { maxHeight.toPx() }

		// 테이블 타원
		val tableWidth = containerWidthPx * 0.65f
		val tableHeight = tableWidth / 1.8f
		val tableRadiusX = tableWidth / 2f
		val tableRadiusY = tableHeight / 2f
		val centerX = containerWidthPx / 2f
		val centerY = containerHeightPx / 2f

		Box(
			modifier = Modifier
				.fillMaxWidth(0.65f)
				.aspectRatio(1.8f)
				.align(Alignment.Center)
				.clip(RoundedCornerShape(40))
				.background(colors.felt)
				.border(2.dp, colors.feltLight, RoundedCornerShape(40)),
			contentAlignment = Alignment.Center,
		) {
			// 보드 카드
			if (boardCards.isNotEmpty()) {
				Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
					boardCards.forEach { card ->
						PlayingCard(card = card, size = boardCardSize)
					}
				}
			}
		}

		// 좌석 배치
		val seatSizeDp = 44.dp
		val seatSizePx = with(density) { seatSizeDp.toPx() }
		val gapPx = with(density) { 1.dp.toPx() }
		val seatHalf = seatSizePx / 2f

		for (seat in allSeats) {
			val seatIndex = allSeats.indexOf(seat)
			val angle = (2 * kotlin.math.PI * seatIndex / allSeats.size) - (kotlin.math.PI / 2)
			val cosA = cos(angle).toFloat()
			val sinA = sin(angle).toFloat()

			val seatCenterX = centerX + (tableRadiusX + gapPx + seatHalf) * cosA
			val seatCenterY = centerY + (tableRadiusY + gapPx + seatHalf) * sinA
			val sx = seatCenterX - seatSizePx / 2f
			val sy = seatCenterY - seatSizePx / 2f

			val posName = getPositionName(seat, buttonSeat, playerCount)
			val isHero = seat == heroSeat
			val isFolded = seat in foldedSeats
			val stack = hand.streets.preflop.actions.firstOrNull { it.playerSeat == seat }?.stackBefore

			// 히어로 카드 또는 쇼다운에서 밝혀진 카드
			val pocketCards = if (isHero) {
				hand.heroHand
			} else {
				hand.showdown.find { it.seat == seat }?.cards
			}

			DetailSeatView(
				positionName = posName,
				stack = stack?.let { formatAmount(it, bb, useBbUnit) },
				isHero = isHero,
				isWinner = seat in winnerSeats,
				isFolded = isFolded,
				pocketCards = pocketCards,
				circleSize = seatCircleSize,
				cardSize = seatCardSize,
				modifier = Modifier.offset { IntOffset(sx.roundToInt(), sy.roundToInt()) },
			)
		}

	}
}

@Composable
private fun DetailSeatView(
	positionName: String,
	stack: String?,
	isHero: Boolean,
	isWinner: Boolean = false,
	isFolded: Boolean,
	pocketCards: PocketCards? = null,
	circleSize: Dp = 28.dp,
	cardSize: CardSize = CardSize.XXS,
	modifier: Modifier = Modifier,
) {
	val colors = HandyTheme.colorScheme
	val textAlpha = if (isFolded) 0.4f else 1f
	val borderColor = when {
		isFolded -> colors.border.copy(alpha = 0.3f)
		isHero -> colors.gold
		else -> colors.border
	}
	val bgColor = when {
		isWinner -> colors.primary.copy(alpha = 0.15f)
		isFolded -> colors.muted.copy(alpha = 0.3f)
		isHero -> colors.gold.copy(alpha = 0.15f)
		else -> colors.muted
	}

	Column(
		modifier = modifier,
		horizontalAlignment = Alignment.CenterHorizontally,
	) {
		// WIN 뱃지
		if (isWinner) {
			Row(
				verticalAlignment = Alignment.CenterVertically,
				horizontalArrangement = Arrangement.spacedBy(2.dp),
				modifier = Modifier
					.clip(RoundedCornerShape(4.dp))
					.background(colors.gold)
					.padding(horizontal = 4.dp, vertical = 1.dp),
			) {
				Icon(
					painter = painterResource(Res.drawable.trophy),
					contentDescription = null,
					modifier = Modifier.size(8.dp),
					tint = Color.White,
				)
				Text(
					text = "WIN",
					style = HandyTheme.typography.bold8.nonScaledSp,
					color = Color.White,
				)
			}
		}

		Row(
			verticalAlignment = Alignment.CenterVertically,
			horizontalArrangement = Arrangement.spacedBy(3.dp),
		) {
			// 포지션 원
			Box(
				modifier = Modifier
					.requiredSize(circleSize)
					.clip(CircleShape)
					.background(bgColor)
					.border(
						width = if (isHero || isWinner) 2.dp else 1.dp,
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
						else -> colors.textPrimary.copy(alpha = textAlpha)
					},
					textAlign = TextAlign.Center,
					maxLines = 1,
				)
			}

			// 공개된 카드 (히어로 + 쇼다운)
			if (pocketCards != null) {
				Row(horizontalArrangement = Arrangement.spacedBy((-3).dp)) {
					PlayingCard(
						card = pocketCards.card1,
						size = cardSize,
						modifier = Modifier.requiredSize(cardSize.width, cardSize.height),
					)
					PlayingCard(
						card = pocketCards.card2,
						size = cardSize,
						modifier = Modifier.requiredSize(cardSize.width, cardSize.height),
					)
				}
			}
		}

		// 스택
		if (stack != null && !isFolded) {
			Text(
				text = stack,
				style = HandyTheme.typography.regular8.nonScaledSp,
				color = colors.textSecondary.copy(alpha = textAlpha),
				textAlign = TextAlign.Center,
			)
		}

		// 폴드 표시
		if (isFolded) {
			Text(
				text = "Fold",
				style = HandyTheme.typography.bold8.nonScaledSp,
				color = colors.textSecondary.copy(alpha = 0.4f),
			)
		}
	}
}

private fun formatAmount(amount: Double, bb: Double, useBbUnit: Boolean): String {
	return if (useBbUnit && bb > 0) {
		val bbCount = (amount * 10 / bb).toLong() / 10.0
		if (bbCount == bbCount.toLong().toDouble()) "${bbCount.toLong()} BB" else "$bbCount BB"
	} else {
		formatWithComma(amount.toLong())
	}
}

private fun getPositionName(seat: Int, buttonSeat: Int, count: Int): String {
	val btn = buttonSeat
	val sbSeat = (btn % count) + 1
	val bbSeat = ((btn + 1) % count) + 1
	if (seat == btn) return Position.BTN.label
	if (seat == sbSeat) return Position.SB.label
	if (seat == bbSeat) return Position.BB.label
	val preflopOrder = (1..count).map { offset -> ((btn + 2 + offset - 1) % count) + 1 }
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

@ThemePreviews
@Composable
private fun HandDetailTableViewPreview() {
	ThemePreview {
		HandDetailTableView(
			hand = HandRecord(
				id = "h1",
				tableId = "t1",
				createdAt = 0L,
				blinds = Blinds(sb = 500.0, bb = 1000.0),
				heroHand = PocketCards(Card(Rank.ACE, Suit.SPADES), Card(Rank.KING, Suit.SPADES)),
				heroSeat = 3,
				heroStack = 50000.0,
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
							Action(playerSeat = 8, type = ActionType.FOLD, stackBefore = 50000.0),
							Action(playerSeat = 9, type = ActionType.FOLD, stackBefore = 50000.0),
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
					),
					turn = TurnStreet(card = Card(Rank.KING, Suit.HEARTS)),
					river = RiverStreet(card = Card(Rank.TWO, Suit.CLUBS)),
				),
				showdown = listOf(
					ShowdownEntry(
						seat = 3,
						cards = PocketCards(Card(Rank.ACE, Suit.SPADES), Card(Rank.KING, Suit.SPADES)),
					),
					ShowdownEntry(
						seat = 6,
						cards = PocketCards(Card(Rank.QUEEN, Suit.HEARTS), Card(Rank.JACK, Suit.HEARTS)),
					),
				),
				result = 49000.0,
			),
			useBbUnit = true,
			modifier = Modifier.padding(16.dp),
		)
	}
}

@Composable
private fun SmallLogo() {
	val colors = HandyTheme.colorScheme

	Row(
		verticalAlignment = Alignment.CenterVertically,
		horizontalArrangement = Arrangement.spacedBy(4.dp),
	) {
		Box(
			modifier = Modifier
				.size(16.dp)
				.clip(RoundedCornerShape(4.dp))
				.background(colors.felt),
			contentAlignment = Alignment.Center,
		) {
			Icon(
				painter = painterResource(Res.drawable.spade_filled),
				contentDescription = null,
				tint = colors.primary,
				modifier = Modifier.size(9.dp),
			)
		}
		Text(
			text = "HandyLog",
			style = HandyTheme.typography.bold8.nonScaledSp,
			color = colors.textSecondary.copy(alpha = 0.6f),
		)
	}
}

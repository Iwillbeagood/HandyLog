package com.hand.log.handdetail.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.hand.log.designsystem.etc.ThemePreview
import com.hand.log.ui.localizedLabel
import com.hand.log.designsystem.etc.ThemePreviews
import com.hand.log.designsystem.theme.HandyTheme
import com.hand.log.domain.model.Blinds
import com.hand.log.domain.model.Position
import com.hand.log.domain.model.Card
import com.hand.log.domain.model.FlopStreet
import com.hand.log.domain.model.HandRecord
import com.hand.log.domain.model.HandStreets
import com.hand.log.domain.model.PreflopStreet
import com.hand.log.domain.model.Rank
import com.hand.log.domain.model.RiverStreet
import com.hand.log.domain.model.PocketCards
import com.hand.log.domain.model.ShowdownEntry
import com.hand.log.domain.model.ShowdownResult
import com.hand.log.domain.model.Suit
import com.hand.log.domain.model.TurnStreet
import com.hand.log.handdetail.model.formatWithComma
import handylog.core.res.generated.resources.Res
import handylog.core.res.generated.resources.showdown_result
import org.jetbrains.compose.resources.stringResource
import com.hand.log.ui.poker.CardSize
import com.hand.log.ui.poker.PlayingCard

@Composable
internal fun ResultSection(hand: HandRecord) {
	val colors = HandyTheme.colorScheme
	val showdownResults = hand.showdownResults

	Column(
		modifier = Modifier
			.fillMaxWidth()
			.clip(RoundedCornerShape(12.dp))
			.background(colors.card)
			.padding(16.dp),
		verticalArrangement = Arrangement.spacedBy(12.dp),
	) {
		Text(
			text = stringResource(Res.string.showdown_result),
			style = HandyTheme.typography.bold16,
			color = colors.textPrimary,
		)

		// 히어로 수익/손실
		hand.result?.let { result ->
			val isPositive = result >= 0
			Text(
				text = if (isPositive) {
					"+${formatWithComma(
						result.toLong(),
					)}"
				} else {
					formatWithComma(result.toLong())
				},
				style = HandyTheme.typography.bold20,
				color = if (isPositive) colors.primary else colors.error,
			)
		}

		// 쇼다운 플레이어 핸드
		if (showdownResults.isNotEmpty()) {
			val playerCount = hand.streets.preflop.actions
				.map { it.playerSeat }.distinct().size.coerceAtLeast(2)
			showdownResults.forEach { result ->
				val entry = hand.showdown.find { it.seat == result.seat }
				if (entry != null) {
					ShowdownPlayerRow(
						positionName = getPositionName(entry.seat, hand.buttonSeat, playerCount),
						entry = entry,
						result = result,
						isHero = entry.seat == hand.heroSeat,
					)
				}
			}
		}

		// 메모
		hand.memo?.let { memo ->
			Text(
				text = memo,
				style = HandyTheme.typography.regular14,
				color = colors.textSecondary,
			)
		}
	}
}

@Composable
private fun ShowdownPlayerRow(
	positionName: String,
	entry: ShowdownEntry,
	result: ShowdownResult,
	isHero: Boolean,
) {
	val colors = HandyTheme.colorScheme
	val isWinner = result.isWinner

	Row(
		modifier = Modifier
			.fillMaxWidth()
			.clip(RoundedCornerShape(8.dp))
			.then(
				if (isWinner) Modifier.border(1.dp, colors.gold, RoundedCornerShape(8.dp)) else Modifier,
			)
			.background(
				when {
					isWinner -> colors.gold.copy(alpha = 0.1f)
					else -> colors.muted
				},
			)
			.padding(10.dp),
		verticalAlignment = Alignment.CenterVertically,
		horizontalArrangement = Arrangement.spacedBy(8.dp),
	) {
		// 포지션
		Box(
			modifier = Modifier
				.size(28.dp)
				.clip(CircleShape)
				.background(
					if (isHero) colors.gold.copy(alpha = 0.15f) else colors.primary.copy(alpha = 0.15f),
				),
			contentAlignment = Alignment.Center,
		) {
			Text(
				text = positionName,
				style = HandyTheme.typography.bold10,
				color = if (isHero) colors.gold else colors.primary,
				maxLines = 1,
			)
		}

		// 포지션 + 태그 + 족보
		Column(modifier = Modifier.weight(1f)) {
			Row(
				verticalAlignment = Alignment.CenterVertically,
				horizontalArrangement = Arrangement.spacedBy(4.dp),
			) {
				if (isHero) {
					Text(
						text = "Hero",
						style = HandyTheme.typography.bold12,
						color = colors.gold,
					)
				}
				if (isWinner) {
					Text(
						text = "WIN",
						style = HandyTheme.typography.bold10,
						color = colors.gold,
						modifier = Modifier
							.clip(RoundedCornerShape(4.dp))
							.background(colors.gold.copy(alpha = 0.2f))
							.padding(horizontal = 4.dp, vertical = 1.dp),
					)
				}
			}
			Text(
				text = result.ranking.localizedLabel(),
				style = HandyTheme.typography.regular12,
				color = if (isWinner) colors.gold else colors.textSecondary,
			)
			// 베스트 5장
			if (result.bestCards.isNotEmpty()) {
				Row(
					horizontalArrangement = Arrangement.spacedBy(2.dp),
					modifier = Modifier.padding(top = 4.dp),
				) {
					result.bestCards.forEach { card ->
						PlayingCard(card = card, size = CardSize.XS)
					}
				}
			}
		}

		// 홀카드 2장
		Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
			PlayingCard(card = entry.card1, size = CardSize.SM)
			PlayingCard(card = entry.card2, size = CardSize.SM)
		}
	}
}

@ThemePreviews
@Composable
private fun ResultSectionWinPreview() {
	ThemePreview {
		ResultSection(
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
					preflop = PreflopStreet(),
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
						seat = 1,
						cards = PocketCards(Card(Rank.QUEEN, Suit.HEARTS), Card(Rank.JACK, Suit.HEARTS)),
					),
				),
				result = 49000.0,
				memo = "탑투페어로 체크레이즈 → 올인 콜, 상대 QJo",
			),
		)
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
private fun ResultSectionLosePreview() {
	ThemePreview {
		ResultSection(
			hand = HandRecord(
				id = "h2",
				tableId = "t1",
				createdAt = 0L,
				heroSeat = 3,
				heroStack = 50000.0,
				buttonSeat = 1,
				result = -25000.0,
				memo = "블러프 캐치 실패",
			),
		)
	}
}

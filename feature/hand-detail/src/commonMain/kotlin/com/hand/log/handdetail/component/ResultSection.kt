package com.hand.log.handdetail.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.hand.log.designsystem.etc.ThemePreview
import com.hand.log.designsystem.etc.ThemePreviews
import com.hand.log.designsystem.theme.HandyTheme
import com.hand.log.domain.model.Blinds
import com.hand.log.domain.model.Card
import com.hand.log.domain.model.FlopStreet
import com.hand.log.domain.model.HandRanking
import com.hand.log.domain.model.HandRecord
import com.hand.log.domain.model.HandStreets
import com.hand.log.domain.model.PocketCards
import com.hand.log.domain.model.PreflopStreet
import com.hand.log.domain.model.Rank
import com.hand.log.domain.model.RiverStreet
import com.hand.log.domain.model.ShowdownEntry
import com.hand.log.domain.model.ShowdownOutcome
import com.hand.log.domain.model.ShowdownResult
import com.hand.log.domain.model.Suit
import com.hand.log.domain.model.TurnStreet
import handylog.core.res.generated.resources.Res
import handylog.core.res.generated.resources.showdown_result
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun ResultSection(
	hand: HandRecord,
	onMarkPlayer: () -> Unit = {},
) {
	val colors = HandyTheme.colorScheme

	Column(
		modifier = Modifier
			.fillMaxWidth()
			.clip(RoundedCornerShape(12.dp))
			.background(colors.card)
			.padding(vertical = 16.dp, horizontal = 12.dp),
		verticalArrangement = Arrangement.spacedBy(12.dp),
	) {
		Text(
			text = stringResource(Res.string.showdown_result),
			style = HandyTheme.typography.bold16,
			color = colors.textPrimary,
		)

		if (hand.isFoldWin) {
			val winnerSeat = hand.winnerSeats.firstOrNull()
			if (winnerSeat != null) {
				val isHero = winnerSeat == hand.heroSeat
				ShowdownPlayerRow(
					positionName = hand.getPositionName(winnerSeat),
					entry = hand.heroShowdownEntry ?: ShowdownEntry(
						seat = winnerSeat,
						cards = PocketCards(Card(Rank.ACE, Suit.SPADES), Card(Rank.ACE, Suit.SPADES)),
					),
					result = ShowdownResult(
						seat = winnerSeat,
						ranking = HandRanking.HIGH_CARD,
						outcome = ShowdownOutcome.WIN,
					),
					isHero = isHero,
					isCardUnknown = !isHero || hand.heroHand == null,
				)
			}
		} else {
			// 히어로
			hand.heroShowdownEntry?.let { entry ->
				ShowdownPlayerRow(
					positionName = hand.getPositionName(hand.heroSeat),
					entry = entry,
					result = hand.getShowdownResult(hand.heroSeat),
					isHero = true,
				)
			}

			// 카드 공개 플레이어
			hand.showdown.filter { it.seat != hand.heroSeat }.forEach { entry ->
				ShowdownPlayerRow(
					positionName = hand.getPositionName(entry.seat),
					entry = entry,
					result = hand.getShowdownResult(entry.seat),
					isHero = false,
					playerName = hand.getPlayerName(entry.seat),
					isMarked = hand.isPlayerMarked(entry.seat),
					onMarkClick = if (!hand.isPlayerMarked(entry.seat)) onMarkPlayer else null,
				)
			}

			// 카드 미공개 플레이어
			hand.unknownCardSeats.forEach { seat ->
				ShowdownPlayerRow(
					positionName = hand.getPositionName(seat),
					entry = ShowdownEntry(
						seat = seat,
						cards = PocketCards(Card(Rank.ACE, Suit.SPADES), Card(Rank.ACE, Suit.SPADES)),
					),
					result = hand.getShowdownResult(seat),
					isHero = false,
					isCardUnknown = true,
					playerName = hand.getPlayerName(seat),
					isMarked = hand.isPlayerMarked(seat),
					onMarkClick = if (!hand.isPlayerMarked(seat)) onMarkPlayer else null,
				)
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

@ThemePreviews
@Composable
private fun ResultSectionSplitPreview() {
	ThemePreview {
		ResultSection(
			hand = HandRecord(
				id = "h3",
				tableId = "t1",
				createdAt = 0L,
				blinds = Blinds(sb = 500.0, bb = 1000.0),
				heroHand = PocketCards(Card(Rank.ACE, Suit.SPADES), Card(Rank.KING, Suit.HEARTS)),
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
					turn = TurnStreet(card = Card(Rank.KING, Suit.DIAMONDS)),
					river = RiverStreet(card = Card(Rank.TWO, Suit.CLUBS)),
				),
				showdown = listOf(
					ShowdownEntry(
						seat = 3,
						cards = PocketCards(Card(Rank.ACE, Suit.SPADES), Card(Rank.KING, Suit.HEARTS)),
					),
					ShowdownEntry(
						seat = 1,
						cards = PocketCards(Card(Rank.ACE, Suit.DIAMONDS), Card(Rank.KING, Suit.CLUBS)),
					),
				),
				showdownResults = listOf(
					ShowdownResult(seat = 3, ranking = HandRanking.TWO_PAIR, outcome = ShowdownOutcome.SPLIT),
					ShowdownResult(seat = 1, ranking = HandRanking.TWO_PAIR, outcome = ShowdownOutcome.SPLIT),
				),
				result = 0.0,
				memo = "AK vs AK 스플릿",
			),
		)
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

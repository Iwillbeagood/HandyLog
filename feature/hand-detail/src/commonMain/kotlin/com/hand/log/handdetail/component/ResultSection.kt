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
import com.hand.log.domain.model.HeroResultType
import com.hand.log.domain.model.HandStreets
import com.hand.log.domain.model.PocketCards
import com.hand.log.domain.model.PreflopStreet
import com.hand.log.domain.model.Rank
import com.hand.log.domain.model.RiverStreet
import com.hand.log.domain.model.HandPlayer
import com.hand.log.domain.model.ShowdownEntry
import com.hand.log.domain.model.ShowdownOutcome
import com.hand.log.domain.model.ShowdownResult
import com.hand.log.domain.model.Suit
import com.hand.log.domain.model.TurnStreet
import com.hand.log.ui.stringRes
import com.hand.log.ui.resultStringRes
import handylog.core.res.generated.resources.Res
import handylog.core.res.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun ResultSection(
	hand: HandRecord,
	onMarkPlayer: (Int) -> Unit = {},
	onEditHeroHand: () -> Unit = {},
	onEditShowdownHand: (Int) -> Unit = {},
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

		// 결과 텍스트 — HandRecord.resolvedHeroResultType 기반
		val resultType = hand.resolvedHeroResultType
		val ranking = hand.heroRanking?.let { stringResource(it.stringRes()) } ?: ""
		val resultRes = resultType.resultStringRes(ranking.isNotEmpty())
		Text(
			text = if (ranking.isNotEmpty()) {
				stringResource(
					resultRes,
					ranking,
				)
			} else {
				stringResource(resultRes)
			},
			style = HandyTheme.typography.bold20,
			color = when (resultType) {
				HeroResultType.SHOWDOWN_SPLIT -> colors.split
				HeroResultType.FOLD_WIN, HeroResultType.SHOWDOWN_WIN -> colors.primary
				HeroResultType.FOLD_LOSE, HeroResultType.SHOWDOWN_LOSE -> colors.error
			},
		)

		if (hand.isFoldWin) {
			val winnerSeat = hand.winnerSeats.firstOrNull()
			if (winnerSeat != null) {
				val isHeroWinner = winnerSeat == hand.heroSeat

				// 히어로가 승자가 아닌 경우에도 히어로 핸드 표시
				if (!isHeroWinner) {
					ShowdownPlayerRow(
						positionName = hand.getPositionName(hand.heroSeat),
						entry = hand.heroShowdownEntry ?: ShowdownEntry(
							seat = hand.heroSeat,
							cards = PocketCards(Card(Rank.ACE, Suit.SPADES), Card(Rank.ACE, Suit.SPADES)),
						),
						result = ShowdownResult(
							seat = hand.heroSeat,
							ranking = HandRanking.WIN_BY_FOLD,
							outcome = ShowdownOutcome.LOSE,
						),
						isHero = true,
						isCardUnknown = hand.heroHoleCards == null,
						onCardClick = onEditHeroHand,
					)
				}

				if (isHeroWinner) {
					ShowdownPlayerRow(
						positionName = hand.getPositionName(winnerSeat),
						entry = hand.heroShowdownEntry ?: ShowdownEntry(
							seat = winnerSeat,
							cards = PocketCards(Card(Rank.ACE, Suit.SPADES), Card(Rank.ACE, Suit.SPADES)),
						),
						result = ShowdownResult(
							seat = winnerSeat,
							ranking = HandRanking.WIN_BY_FOLD,
							outcome = ShowdownOutcome.WIN,
						),
						isHero = true,
						isCardUnknown = hand.heroHoleCards == null,
						onCardClick = onEditHeroHand,
					)
				} else {
					val winnerEntry = hand.showdown.find { it.seat == winnerSeat }
					ShowdownPlayerRow(
						positionName = hand.getPositionName(winnerSeat),
						entry = winnerEntry ?: ShowdownEntry(
							seat = winnerSeat,
							cards = PocketCards(Card(Rank.ACE, Suit.SPADES), Card(Rank.ACE, Suit.SPADES)),
						),
						result = ShowdownResult(
							seat = winnerSeat,
							ranking = HandRanking.WIN_BY_FOLD,
							outcome = ShowdownOutcome.WIN,
						),
						isHero = false,
						isCardUnknown = winnerEntry == null,
						playerName = hand.getPlayerName(winnerSeat),
						isMarked = hand.isPlayerMarked(winnerSeat),
						onMarkClick = if (!hand.isPlayerMarked(winnerSeat)) {
							{ onMarkPlayer(winnerSeat) }
						} else {
							null
						},
						onCardClick = { onEditShowdownHand(winnerSeat) },
					)
				}
			}
		} else {
			// 히어로 (핸드가 없어도 항상 표시)
			ShowdownPlayerRow(
				positionName = hand.getPositionName(hand.heroSeat),
				entry = hand.heroShowdownEntry ?: ShowdownEntry(
					seat = hand.heroSeat,
					cards = PocketCards(Card(Rank.ACE, Suit.SPADES), Card(Rank.ACE, Suit.SPADES)),
				),
				result = hand.getShowdownResult(hand.heroSeat),
				isHero = true,
				isCardUnknown = hand.heroHoleCards == null,
				onCardClick = onEditHeroHand,
			)

			// 카드 공개 플레이어
			hand.showdown.filter { it.seat != hand.heroSeat }.forEach { entry ->
				ShowdownPlayerRow(
					positionName = hand.getPositionName(entry.seat),
					entry = entry,
					result = hand.getShowdownResult(entry.seat),
					isHero = false,
					playerName = hand.getPlayerName(entry.seat),
					isMarked = hand.isPlayerMarked(entry.seat),
					onMarkClick = if (!hand.isPlayerMarked(entry.seat)) {
						{ onMarkPlayer(entry.seat) }
					} else {
						null
					},
					onCardClick = { onEditShowdownHand(entry.seat) },
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
					onMarkClick = if (!hand.isPlayerMarked(seat)) {
						{ onMarkPlayer(seat) }
					} else {
						null
					},
					onCardClick = { onEditShowdownHand(seat) },
				)
			}
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
				heroSeat = 3,
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
				players = listOf(
					HandPlayer(
						seat = 3,
						cards = PocketCards(Card(Rank.ACE, Suit.SPADES), Card(Rank.KING, Suit.SPADES)),
						initialStack = 50000.0,
						isHero = true,
					),
					HandPlayer(
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
				heroSeat = 3,
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
				players = listOf(
					HandPlayer(
						seat = 3,
						cards = PocketCards(Card(Rank.ACE, Suit.SPADES), Card(Rank.KING, Suit.HEARTS)),
						initialStack = 50000.0,
						ranking = HandRanking.TWO_PAIR,
						outcome = ShowdownOutcome.SPLIT,
						isHero = true,
					),
					HandPlayer(
						seat = 1,
						cards = PocketCards(Card(Rank.ACE, Suit.DIAMONDS), Card(Rank.KING, Suit.CLUBS)),
						ranking = HandRanking.TWO_PAIR,
						outcome = ShowdownOutcome.SPLIT,
					),
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
				buttonSeat = 1,
				players = listOf(
					HandPlayer(seat = 3, initialStack = 50000.0, isHero = true),
				),
				result = -25000.0,
				memo = "블러프 캐치 실패",
			),
		)
	}
}

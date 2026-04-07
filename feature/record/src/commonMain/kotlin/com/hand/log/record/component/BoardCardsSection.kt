package com.hand.log.record.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hand.log.designsystem.component.HandySectionLabel
import com.hand.log.designsystem.component.VerticalSpacer
import com.hand.log.designsystem.etc.ThemePreview
import com.hand.log.designsystem.etc.ThemePreviews
import com.hand.log.domain.model.Card
import com.hand.log.domain.model.FlopStreet
import com.hand.log.domain.model.HandStreets
import com.hand.log.domain.model.PreflopStreet
import com.hand.log.domain.model.Rank
import com.hand.log.domain.model.RiverStreet
import com.hand.log.domain.model.Street
import com.hand.log.domain.model.Suit
import com.hand.log.domain.model.TurnStreet
import com.hand.log.ui.poker.CardSize
import com.hand.log.ui.poker.PlayingCard
import handylog.core.res.generated.resources.Res
import handylog.core.res.generated.resources.board_cards
import org.jetbrains.compose.resources.stringResource

/**
 * 보드 카드 표시 섹션. StreetStepContent / StreetStepEditContent 에서 공용 사용.
 */
@Composable
internal fun BoardCardsSection(
	currentStreet: Street,
	streets: HandStreets,
	onSelectBoardCard: (Street) -> Unit,
) {
	if (currentStreet == Street.PREFLOP) return

	HandySectionLabel(stringResource(Res.string.board_cards))
	Row(
		horizontalArrangement = Arrangement.spacedBy(8.dp),
		verticalAlignment = Alignment.CenterVertically,
	) {
		// 이전 스트릿 카드 표시 (턴/리버에서)
		if (currentStreet == Street.TURN || currentStreet == Street.RIVER) {
			Row(
				horizontalArrangement = Arrangement.spacedBy(4.dp),
				modifier = Modifier.clickable { onSelectBoardCard(Street.FLOP) },
			) {
				streets.getCards(Street.FLOP).forEach { card ->
					PlayingCard(card = card, size = CardSize.MD)
				}
			}
		}
		if (currentStreet == Street.RIVER) {
			Row(
				modifier = Modifier.clickable { onSelectBoardCard(Street.TURN) },
			) {
				streets.getCards(Street.TURN).forEach { card ->
					PlayingCard(card = card, size = CardSize.MD)
				}
			}
		}

		// 현재 스트릿 카드
		val streetCards = streets.getCards(currentStreet)
		val cardCount = when (currentStreet) {
			Street.FLOP -> 3
			Street.TURN -> 1
			Street.RIVER -> 1
			else -> 0
		}
		Row(
			horizontalArrangement = Arrangement.spacedBy(4.dp),
			modifier = Modifier.clickable { onSelectBoardCard(currentStreet) },
		) {
			(0 until cardCount).forEach { index ->
				PlayingCard(card = streetCards.getOrNull(index), size = CardSize.MD)
			}
		}
	}
	VerticalSpacer(16.dp)
}

@ThemePreviews
@Composable
private fun BoardCardsFlopPreview() {
	ThemePreview {
		BoardCardsSection(
			currentStreet = Street.FLOP,
			streets = HandStreets(
				preflop = PreflopStreet(),
				flop = FlopStreet(
					card1 = Card(Rank.ACE, Suit.HEARTS),
					card2 = Card(Rank.KING, Suit.DIAMONDS),
					card3 = Card(Rank.QUEEN, Suit.CLUBS),
				),
			),
			onSelectBoardCard = {},
		)
	}
}

@ThemePreviews
@Composable
private fun BoardCardsTurnPreview() {
	ThemePreview {
		BoardCardsSection(
			currentStreet = Street.TURN,
			streets = HandStreets(
				preflop = PreflopStreet(),
				flop = FlopStreet(
					card1 = Card(Rank.ACE, Suit.HEARTS),
					card2 = Card(Rank.KING, Suit.DIAMONDS),
					card3 = Card(Rank.QUEEN, Suit.CLUBS),
				),
				turn = TurnStreet(card = Card(Rank.SEVEN, Suit.SPADES)),
			),
			onSelectBoardCard = {},
		)
	}
}

@ThemePreviews
@Composable
private fun BoardCardsRiverPreview() {
	ThemePreview {
		BoardCardsSection(
			currentStreet = Street.RIVER,
			streets = HandStreets(
				preflop = PreflopStreet(),
				flop = FlopStreet(
					card1 = Card(Rank.ACE, Suit.HEARTS),
					card2 = Card(Rank.KING, Suit.DIAMONDS),
					card3 = Card(Rank.QUEEN, Suit.CLUBS),
				),
				turn = TurnStreet(card = Card(Rank.SEVEN, Suit.SPADES)),
				river = RiverStreet(card = Card(Rank.TWO, Suit.CLUBS)),
			),
			onSelectBoardCard = {},
		)
	}
}

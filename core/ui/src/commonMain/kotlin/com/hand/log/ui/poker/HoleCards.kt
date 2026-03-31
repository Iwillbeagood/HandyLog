package com.hand.log.ui.poker

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hand.log.designsystem.etc.ThemePreview
import com.hand.log.designsystem.etc.ThemePreviews
import com.hand.log.domain.model.Card
import com.hand.log.domain.model.PocketCards
import com.hand.log.domain.model.Rank
import com.hand.log.domain.model.Suit

/**
 * 홀카드 2장 표시.
 *
 * @param cards 포켓 카드 (null이면 뒷면)
 * @param isUnknown 미공개 여부 (뒷면 표시)
 * @param size 카드 크기
 */
@Composable
fun HoleCards(
	cards: PocketCards?,
	isUnknown: Boolean = false,
	size: CardSize = CardSize.SM,
	modifier: Modifier = Modifier,
) {
	Row(
		horizontalArrangement = Arrangement.spacedBy(3.dp),
		modifier = modifier,
	) {
		if (isUnknown || cards == null) {
			PlayingCard(card = null, size = size, faceDown = true)
			PlayingCard(card = null, size = size, faceDown = true)
		} else {
			PlayingCard(card = cards.card1, size = size)
			PlayingCard(card = cards.card2, size = size)
		}
	}
}

@ThemePreviews
@Composable
private fun HoleCardsPreview() {
	ThemePreview {
		Row(
			horizontalArrangement = Arrangement.spacedBy(12.dp),
			modifier = Modifier.padding(16.dp),
		) {
			HoleCards(cards = PocketCards(Card(Rank.ACE, Suit.SPADES), Card(Rank.KING, Suit.HEARTS)))
			HoleCards(cards = null)
			HoleCards(
				cards = PocketCards(Card(Rank.QUEEN, Suit.DIAMONDS), Card(Rank.JACK, Suit.CLUBS)),
				isUnknown = true,
			)
		}
	}
}

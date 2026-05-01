package com.hand.log.handdetail.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.hand.log.designsystem.etc.ThemePreview
import com.hand.log.designsystem.etc.ThemePreviews
import com.hand.log.designsystem.theme.HandyTheme
import com.hand.log.domain.model.Card
import com.hand.log.domain.model.HandRanking
import com.hand.log.domain.model.PocketCards
import com.hand.log.domain.model.Rank
import com.hand.log.domain.model.ShowdownEntry
import com.hand.log.domain.model.ShowdownOutcome
import com.hand.log.domain.model.ShowdownResult
import com.hand.log.domain.model.Suit
import com.hand.log.ui.localizedLabel
import com.hand.log.ui.poker.CardSize
import com.hand.log.ui.poker.HoleCards
import com.hand.log.ui.poker.OutcomeBadge
import com.hand.log.ui.poker.PlayingCard
import com.hand.log.ui.poker.PositionBadge
import com.hand.log.ui.poker.outcomeColor

@Composable
internal fun ShowdownPlayerRow(
	positionName: String,
	entry: ShowdownEntry,
	result: ShowdownResult?,
	isHero: Boolean,
	isWinner: Boolean = result?.isWinner == true,
	isSplit: Boolean = result?.isSplit == true,
	isCardUnknown: Boolean = false,
	playerName: String? = null,
	isMarked: Boolean = false,
	onMarkClick: (() -> Unit)? = null,
	onCardClick: (() -> Unit)? = null,
) {
	val colors = HandyTheme.colorScheme
	val isLoser = (result != null && !isWinner && !isSplit) || isCardUnknown
	val outcome = result?.outcome

	Row(
		modifier = Modifier
			.fillMaxWidth()
			.clip(RoundedCornerShape(8.dp))
			.then(
				when {
					isWinner -> Modifier.border(1.dp, colors.gold, RoundedCornerShape(8.dp))
					isSplit -> Modifier.border(1.dp, colors.split, RoundedCornerShape(8.dp))
					isLoser -> Modifier.border(1.dp, colors.error.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
					else -> Modifier
				},
			)
			.background(
				when {
					isWinner -> colors.gold.copy(alpha = 0.1f)
					isSplit -> colors.split.copy(alpha = 0.1f)
					isLoser -> colors.error.copy(alpha = 0.05f)
					else -> colors.muted
				},
			)
			.padding(10.dp),
		verticalAlignment = Alignment.CenterVertically,
		horizontalArrangement = Arrangement.spacedBy(8.dp),
	) {
		PositionBadge(
			positionName = positionName,
			isHero = isHero,
			showMarkButton = !isHero && !isMarked && onMarkClick != null,
			onMarkClick = onMarkClick,
		)

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
				if (!isHero && playerName != null) {
					Text(
						text = playerName,
						style = HandyTheme.typography.bold12,
						color = colors.textPrimary,
					)
				}
				if (outcome != null) {
					OutcomeBadge(outcome = outcome)
				}
			}
			if (result != null) {
				Text(
					text = result.ranking.localizedLabel(),
					style = HandyTheme.typography.regular12,
					color = outcomeColor(outcome),
				)
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
		}

		HoleCards(
			cards = entry.cards,
			isUnknown = isCardUnknown,
			modifier = if (onCardClick != null) Modifier.clickable { onCardClick() } else Modifier,
		)
	}
}

@ThemePreviews
@Composable
private fun ShowdownPlayerRowWinPreview() {
	ThemePreview {
		ShowdownPlayerRow(
			positionName = "BB",
			entry = ShowdownEntry(
				seat = 3,
				cards = PocketCards(Card(Rank.ACE, Suit.SPADES), Card(Rank.KING, Suit.SPADES)),
			),
			result = ShowdownResult(seat = 3, ranking = HandRanking.TWO_PAIR, outcome = ShowdownOutcome.WIN),
			isHero = true,
		)
	}
}

@ThemePreviews
@Composable
private fun ShowdownPlayerRowSplitPreview() {
	ThemePreview {
		ShowdownPlayerRow(
			positionName = "BTN",
			entry = ShowdownEntry(
				seat = 1,
				cards = PocketCards(Card(Rank.ACE, Suit.DIAMONDS), Card(Rank.KING, Suit.CLUBS)),
			),
			result = ShowdownResult(
				seat = 1,
				ranking = HandRanking.TWO_PAIR,
				outcome = ShowdownOutcome.SPLIT,
			),
			isHero = false,
			playerName = "Fish",
			isMarked = true,
		)
	}
}

@ThemePreviews
@Composable
private fun ShowdownPlayerRowLosePreview() {
	ThemePreview {
		ShowdownPlayerRow(
			positionName = "CO",
			entry = ShowdownEntry(
				seat = 5,
				cards = PocketCards(Card(Rank.QUEEN, Suit.HEARTS), Card(Rank.JACK, Suit.HEARTS)),
			),
			result = ShowdownResult(
				seat = 5,
				ranking = HandRanking.HIGH_CARD,
				outcome = ShowdownOutcome.LOSE,
			),
			isHero = false,
		)
	}
}

@ThemePreviews
@Composable
private fun ShowdownPlayerRowMarkablePreview() {
	ThemePreview {
		ShowdownPlayerRow(
			positionName = "BTN",
			entry = ShowdownEntry(
				seat = 1,
				cards = PocketCards(Card(Rank.TEN, Suit.SPADES), Card(Rank.NINE, Suit.SPADES)),
			),
			result = ShowdownResult(
				seat = 1,
				ranking = HandRanking.ONE_PAIR,
				outcome = ShowdownOutcome.LOSE,
			),
			isHero = false,
			isMarked = false,
			onMarkClick = {},
		)
	}
}

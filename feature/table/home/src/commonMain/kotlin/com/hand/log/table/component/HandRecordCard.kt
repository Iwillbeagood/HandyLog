package com.hand.log.table.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.unit.dp
import com.hand.log.designsystem.theme.HandyTheme
import com.hand.log.domain.model.Blinds
import com.hand.log.domain.model.Card
import com.hand.log.domain.model.HandRecord
import com.hand.log.domain.model.Rank
import com.hand.log.domain.model.Street
import com.hand.log.domain.model.FlopStreet
import com.hand.log.domain.model.HandStreets
import com.hand.log.domain.model.HeroHand
import com.hand.log.domain.model.RiverStreet
import com.hand.log.domain.model.TurnStreet
import com.hand.log.domain.model.Suit
import com.hand.log.ui.poker.CardSize
import com.hand.log.ui.poker.PlayingCard
import com.hand.log.designsystem.etc.ThemePreview
import com.hand.log.designsystem.etc.ThemePreviews

@Composable
internal fun HandRecordCard(
	hand: HandRecord,
	index: Int = 0,
	onClick: () -> Unit,
	onDelete: () -> Unit,
	modifier: Modifier = Modifier,
) {
	val colors = HandyTheme.colorScheme

	Column(
		modifier = modifier
			.fillMaxWidth()
			.clip(RoundedCornerShape(12.dp))
			.background(colors.card)
			.border(1.dp, colors.border, RoundedCornerShape(12.dp))
			.clickable(onClick = onClick)
			.padding(12.dp),
	) {
		// Hand number + Result
		if (index > 0) {
			Row(
				modifier = Modifier.fillMaxWidth(),
				horizontalArrangement = Arrangement.SpaceBetween,
				verticalAlignment = Alignment.CenterVertically,
			) {
				Text(
					text = "#$index",
					style = HandyTheme.typography.regular12,
					color = colors.textSecondary,
				)
				hand.result?.let { result ->
					val isPositive = result >= 0
					Text(
						text = if (isPositive) "+${formatAmount(result)}" else formatAmount(result),
						style = HandyTheme.typography.bold14,
						color = if (isPositive) colors.primary else colors.error,
					)
				}
			}
			Spacer(modifier = Modifier.height(6.dp))
		}

		// Hero cards + details
		Row(
			verticalAlignment = Alignment.CenterVertically,
			horizontalArrangement = Arrangement.spacedBy(8.dp),
		) {
			// Hero cards
			Row(
				horizontalArrangement = Arrangement.spacedBy(4.dp),
			) {
				hand.heroCards.forEach { card ->
					PlayingCard(
						card = card,
						size = CardSize.SM,
					)
				}

				if (hand.heroCards.isEmpty()) {
					PlayingCard(card = null, size = CardSize.SM, faceDown = true)
					PlayingCard(card = null, size = CardSize.SM, faceDown = true)
				}
			}

			// Details column
			Column(
				modifier = Modifier.weight(1f),
			) {
				// Button seat + blinds
				Row(
					verticalAlignment = Alignment.CenterVertically,
				) {
					Text(
						text = "BTN: Seat ${hand.buttonSeat}",
						style = HandyTheme.typography.regular12,
						color = colors.textSecondary,
					)
					hand.blinds?.let { blinds ->
						Text(
							text = " • ${formatAmount(blinds.sb)}/${formatAmount(blinds.bb)}",
							style = HandyTheme.typography.regular12,
							color = colors.textSecondary,
						)
					}
				}

				// Board cards inline
				val boardStreets = listOf(Street.FLOP, Street.TURN, Street.RIVER)
				val hasBoard = boardStreets.any { hand.streets.getCards(it).isNotEmpty() }

				if (hasBoard) {
					Spacer(modifier = Modifier.height(4.dp))
					Row(
						horizontalArrangement = Arrangement.spacedBy(2.dp),
					) {
						boardStreets.forEach { street ->
							hand.streets.getCards(street).forEach { card ->
								PlayingCard(card = card, size = CardSize.XS)
							}
							if (street != Street.RIVER && hand.streets.getCards(street).isNotEmpty()) {
								Spacer(modifier = Modifier.width(2.dp))
							}
						}
					}
				}
			}
		}

		// Memo
		hand.memo?.let { memo ->
			Spacer(modifier = Modifier.height(6.dp))
			Text(
				text = memo,
				style = HandyTheme.typography.regular12,
				color = colors.textSecondary,
				maxLines = 2,
			)
		}

		// Created date
		Spacer(modifier = Modifier.height(4.dp))
		Text(
			text = formatTimestamp(hand.createdAt),
			style = HandyTheme.typography.regular10,
			color = colors.textSecondary.copy(alpha = 0.6f),
		)
	}
}

private fun formatAmount(amount: Double): String {
	return when {
		amount >= 1_000_000 || amount <= -1_000_000 -> {
			val v = amount / 1_000_000
			"${formatDecimal(v)}M"
		}
		amount >= 1_000 || amount <= -1_000 -> {
			val v = amount / 1_000
			"${formatDecimal(v)}K"
		}
		amount % 1.0 == 0.0 -> amount.toLong().toString()
		else -> amount.toLong().toString()
	}
}

private fun formatDecimal(value: Double): String {
	return if (value % 1.0 == 0.0) {
		value.toLong().toString()
	} else {
		((value * 10).toLong() / 10.0).toString()
	}
}

private fun formatTimestamp(timestamp: Long): String {
	val seconds = timestamp / 1000
	val minutes = seconds / 60
	val hours = minutes / 60

	val h = (hours % 24).toString().padStart(2, '0')
	val m = (minutes % 60).toString().padStart(2, '0')
	return "$h:$m"
}

@ThemePreviews
@Composable
private fun HandRecordCardPreview() {
	ThemePreview {
		HandRecordCard(
			hand = HandRecord(
				id = "h1",
				tableId = "1",
				createdAt = 1710000000000L,
				blinds = Blinds(sb = 500.0, bb = 1000.0),
				heroHand = HeroHand(Card(Rank.ACE, Suit.SPADES), Card(Rank.KING, Suit.SPADES)),
				heroStack = 62000.0,
				buttonSeat = 1,
				streets = HandStreets(
					flop = FlopStreet(
						card1 = Card(Rank.ACE, Suit.HEARTS),
						card2 = Card(Rank.KING, Suit.DIAMONDS),
						card3 = Card(Rank.QUEEN, Suit.CLUBS),
					),
					turn = TurnStreet(
						card = Card(Rank.TEN, Suit.SPADES),
					),
					river = RiverStreet(
						card = Card(Rank.TWO, Suit.HEARTS),
					),
				),
				result = 15000.0,
				memo = "탑투페어로 올인 콜",
			),
			onClick = {},
			onDelete = {},
		)
	}
}

@ThemePreviews
@Composable
private fun HandRecordCardNegativePreview() {
	ThemePreview {
		HandRecordCard(
			hand = HandRecord(
				id = "h2",
				tableId = "1",
				createdAt = 1709900000000L,
				blinds = Blinds(sb = 500.0, bb = 1000.0),
				heroHand = HeroHand(Card(Rank.JACK, Suit.HEARTS), Card(Rank.TEN, Suit.HEARTS)),
				heroStack = 50000.0,
				buttonSeat = 3,
				result = -8500.0,
			),
			onClick = {},
			onDelete = {},
		)
	}
}

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
import com.hand.log.domain.model.HandPlayer
import com.hand.log.domain.model.HandRecord
import com.hand.log.domain.model.Rank
import com.hand.log.domain.model.Street
import com.hand.log.domain.model.FlopStreet
import com.hand.log.domain.model.HandStreets
import com.hand.log.domain.model.PocketCards
import com.hand.log.domain.model.RiverStreet
import com.hand.log.domain.model.TurnStreet
import com.hand.log.domain.model.Suit
import com.hand.log.ui.poker.CardSize
import com.hand.log.ui.poker.formatWithComma
import com.hand.log.ui.poker.PlayingCard
import com.hand.log.designsystem.etc.ThemePreview
import com.hand.log.designsystem.etc.ThemePreviews
import handylog.core.res.generated.resources.Res
import handylog.core.res.generated.resources.hand_record_later
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun HandRecordCard(
	hand: HandRecord,
	tableDate: LocalDate,
	index: Int = 0,
	onClick: () -> Unit,
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
		// Hand number
		if (index > 0) {
			Text(
				text = "#$index",
				style = HandyTheme.typography.regular12,
				color = colors.textSecondary,
			)
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
				val pocket = hand.heroHoleCards
				if (pocket != null) {
					PlayingCard(card = pocket.card1, size = CardSize.SM)
					PlayingCard(card = pocket.card2, size = CardSize.SM)
				} else {
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
						text = hand.heroPosition.label,
						style = HandyTheme.typography.bold12,
						color = colors.textSecondary,
					)
					hand.blinds?.let { blinds ->
						Text(
							text = " • ${formatWithComma(blinds.sb.toLong())}/${formatWithComma(blinds.bb.toLong())}",
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
		val laterLabel = stringResource(Res.string.hand_record_later)
		Spacer(modifier = Modifier.height(4.dp))
		Text(
			text = formatTimestamp(hand.createdAt, tableDate, laterLabel),
			style = HandyTheme.typography.regular10,
			color = colors.textSecondary.copy(alpha = 0.6f),
		)
	}
}

@OptIn(kotlin.time.ExperimentalTime::class)
@Suppress("DEPRECATION")
private fun formatTimestamp(
	timestamp: Long,
	tableDate: LocalDate,
	laterLabel: String,
): String {
	val dateTime = kotlinx.datetime.Instant.fromEpochMilliseconds(timestamp)
		.toLocalDateTime(TimeZone.currentSystemDefault())
	val recordDate = dateTime.date
	val daysDiff = (recordDate.toEpochDays() - tableDate.toEpochDays()).toInt()

	val h = dateTime.hour.toString().padStart(2, '0')
	val m = dateTime.minute.toString().padStart(2, '0')

	return when {
		daysDiff <= 0 -> "$h:$m"
		daysDiff == 1 -> "${recordDate.month.number}/${recordDate.dayOfMonth} $h:$m"
		else -> laterLabel
	}
}

@ThemePreviews
@Composable
private fun HandRecordCardPreview() {
	ThemePreview {
		HandRecordCard(
			tableDate = LocalDate(2024, 3, 9),
			hand = HandRecord(
				id = "h1",
				tableId = "1",
				createdAt = 1710000000000L,
				blinds = Blinds(sb = 500.0, bb = 1000.0),
				heroSeat = 3,
				players = listOf(
					HandPlayer(
						seat = 3,
						cards = PocketCards(Card(Rank.ACE, Suit.SPADES), Card(Rank.KING, Suit.SPADES)),
						initialStack = 62000.0,
						isHero = true,
					),
				),
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
		)
	}
}

@ThemePreviews
@Composable
private fun HandRecordCardNegativePreview() {
	ThemePreview {
		HandRecordCard(
			tableDate = LocalDate(2024, 3, 8),
			hand = HandRecord(
				id = "h2",
				tableId = "1",
				createdAt = 1709900000000L,
				blinds = Blinds(sb = 500.0, bb = 1000.0),
				heroSeat = 3,
				players = listOf(
					HandPlayer(
						seat = 3,
						cards = PocketCards(Card(Rank.JACK, Suit.HEARTS), Card(Rank.TEN, Suit.HEARTS)),
						initialStack = 50000.0,
						isHero = true,
					),
				),
				buttonSeat = 3,
				result = -8500.0,
			),
			onClick = {},
		)
	}
}

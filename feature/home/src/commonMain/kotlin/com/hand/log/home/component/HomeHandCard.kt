package com.hand.log.home.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.hand.log.designsystem.etc.ThemePreview
import com.hand.log.designsystem.etc.ThemePreviews
import com.hand.log.designsystem.theme.HandyTheme
import com.hand.log.domain.model.Blinds
import com.hand.log.domain.model.Card
import com.hand.log.domain.model.GameType
import com.hand.log.domain.model.HandRecord
import com.hand.log.domain.model.HandWithTable
import com.hand.log.domain.model.HandPlayer
import com.hand.log.domain.model.PocketCards
import com.hand.log.domain.model.PokerTable
import com.hand.log.domain.model.Rank
import com.hand.log.domain.model.Street
import com.hand.log.domain.model.Suit
import com.hand.log.domain.model.FlopStreet
import com.hand.log.domain.model.HandStreets
import com.hand.log.domain.model.TurnStreet
import com.hand.log.domain.model.RiverStreet
import com.hand.log.ui.stringRes
import org.jetbrains.compose.resources.stringResource
import com.hand.log.ui.poker.CardSize
import com.hand.log.ui.poker.PlayingCard
import com.hand.log.ui.poker.formatWithComma
import handylog.core.res.generated.resources.Res
import handylog.core.res.generated.resources.*
import kotlinx.datetime.LocalDate
import androidx.compose.ui.graphics.Color
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

@Composable
internal fun HomeHandCard(
	item: HandWithTable,
	onClick: () -> Unit,
	modifier: Modifier = Modifier,
) {
	val colors = HandyTheme.colorScheme
	val hand = item.hand
	val table = item.table

	Column(
		modifier = modifier
			.fillMaxWidth()
			.clip(RoundedCornerShape(12.dp))
			.background(colors.card)
			.border(1.dp, colors.border, RoundedCornerShape(12.dp))
			.clickable(onClick = onClick)
			.padding(12.dp),
	) {
		if (table != null) {
			Row(
				modifier = Modifier.fillMaxWidth(),
				verticalAlignment = Alignment.CenterVertically,
			) {
				TableSourceRow(table = table, modifier = Modifier.weight(1f))
				ChevronIcon()
			}
			Spacer(modifier = Modifier.height(8.dp))
		}

		Row(
			verticalAlignment = Alignment.CenterVertically,
			horizontalArrangement = Arrangement.spacedBy(8.dp),
		) {
			// Hero cards
			Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
				val pocket = hand.heroHoleCards
				if (pocket != null) {
					PlayingCard(card = pocket.card1, size = CardSize.SM)
					PlayingCard(card = pocket.card2, size = CardSize.SM)
				} else {
					PlayingCard(card = null, size = CardSize.SM, faceDown = true)
					PlayingCard(card = null, size = CardSize.SM, faceDown = true)
				}
			}

			Column(modifier = Modifier.weight(1f)) {
				// Position + blinds
				Row(verticalAlignment = Alignment.CenterVertically) {
					Text(
						text = hand.heroPosition.label,
						style = HandyTheme.typography.bold12,
						color = colors.textSecondary,
					)
					item.displayBlinds?.let { blinds ->
						Text(
							text = " \u00B7 ${formatWithComma(
								blinds.sb.toLong(),
							)}/${formatWithComma(blinds.bb.toLong())}",
							style = HandyTheme.typography.regular12,
							color = colors.textSecondary,
						)
					}
				}

				// Board cards
				if (hand.streets.boardCards.isNotEmpty()) {
					Spacer(modifier = Modifier.height(4.dp))
					Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
						listOf(Street.FLOP, Street.TURN, Street.RIVER).forEach { street ->
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
			if (table == null) ChevronIcon()
		}

		// Date
		Spacer(modifier = Modifier.height(6.dp))
		Row(
			verticalAlignment = Alignment.CenterVertically,
			horizontalArrangement = Arrangement.spacedBy(4.dp),
		) {
			Icon(
				painter = painterResource(Res.drawable.calendar),
				contentDescription = null,
				tint = colors.textSecondary,
				modifier = Modifier.size(12.dp),
			)
			Text(
				text = item.date.toString(),
				style = HandyTheme.typography.regular10,
				color = colors.textSecondary,
			)
		}
	}
}

@Composable
private fun TableSourceRow(
	table: PokerTable,
	modifier: Modifier = Modifier,
) {
	val colors = HandyTheme.colorScheme
	Row(
		verticalAlignment = Alignment.CenterVertically,
		horizontalArrangement = Arrangement.spacedBy(8.dp),
		modifier = modifier,
	) {
		when (val gameType = table.gameType) {
			is GameType.Cash -> HandSourceBadge(
				icon = Res.drawable.dollar_sign,
				label = stringResource(gameType.stringRes()),
				color = colors.primary,
			)
			is GameType.Tournament -> HandSourceBadge(
				icon = Res.drawable.trophy,
				label = stringResource(gameType.stringRes()),
				color = colors.gold,
			)
		}
		table.location?.takeIf { it.isNotBlank() }?.let { location ->
			Row(
				verticalAlignment = Alignment.CenterVertically,
				horizontalArrangement = Arrangement.spacedBy(4.dp),
			) {
				Icon(
					painter = painterResource(Res.drawable.map_pin),
					contentDescription = null,
					tint = colors.textSecondary,
					modifier = Modifier.size(10.dp),
				)
				Text(
					text = location,
					style = HandyTheme.typography.regular10,
					color = colors.textSecondary,
				)
			}
		}
	}
}

@Composable
private fun ChevronIcon() {
	Icon(
		painter = painterResource(Res.drawable.chevron_right),
		contentDescription = null,
		tint = HandyTheme.colorScheme.textSecondary,
		modifier = Modifier.size(20.dp),
	)
}

@Composable
private fun HandSourceBadge(
	icon: DrawableResource,
	label: String,
	color: Color,
) {
	Row(
		verticalAlignment = Alignment.CenterVertically,
		horizontalArrangement = Arrangement.spacedBy(4.dp),
		modifier = Modifier
			.clip(RoundedCornerShape(50))
			.background(color.copy(alpha = 0.15f))
			.padding(horizontal = 8.dp, vertical = 3.dp),
	) {
		Icon(
			painter = painterResource(icon),
			contentDescription = null,
			tint = color,
			modifier = Modifier.size(12.dp),
		)
		Text(
			text = label,
			style = HandyTheme.typography.bold12,
			color = color,
		)
	}
}

@ThemePreviews
@Composable
private fun HomeHandCardPreview() {
	ThemePreview {
		HomeHandCard(
			item = HandWithTable(
				hand = HandRecord(
					id = "h1",
					tableId = "1",
					createdAt = 1710000000000L,
					blinds = Blinds(sb = 500.0, bb = 1000.0),
					buttonSeat = 1,
					streets = HandStreets(
						flop = FlopStreet(
							card1 = Card(Rank.ACE, Suit.HEARTS),
							card2 = Card(Rank.KING, Suit.DIAMONDS),
							card3 = Card(Rank.QUEEN, Suit.CLUBS),
						),
						turn = TurnStreet(card = Card(Rank.TEN, Suit.SPADES)),
						river = RiverStreet(card = Card(Rank.TWO, Suit.HEARTS)),
					),
					players = listOf(
						HandPlayer(
							seat = 5,
							cards = PocketCards(Card(Rank.ACE, Suit.SPADES), Card(Rank.KING, Suit.SPADES)),
							initialStack = 62000.0,
							isHero = true,
						),
					),
					result = 15000.0,
				),
				table = PokerTable(
					id = "1",
					date = LocalDate(2025, 3, 10),
					location = "강남 홀덤펍",
					gameType = GameType.Cash(sb = 500.0, bb = 1000.0),
					heroSeat = 5,
					createdAt = 1710000000000L,
				),
			),
			onClick = {},
		)
	}
}

@ThemePreviews
@Composable
private fun HomeHandCardWithoutTablePreview() {
	ThemePreview {
		HomeHandCard(
			item = HandWithTable(
				hand = HandRecord(
					id = "h2",
					tableId = null,
					createdAt = 1710000000000L,
					blinds = Blinds(sb = 0.5, bb = 1.0),
					buttonSeat = 3,
					players = listOf(
						HandPlayer(
							seat = 1,
							cards = PocketCards(Card(Rank.QUEEN, Suit.HEARTS), Card(Rank.QUEEN, Suit.CLUBS)),
							initialStack = 100.0,
							isHero = true,
						),
					),
				),
				table = null,
			),
			onClick = {},
		)
	}
}

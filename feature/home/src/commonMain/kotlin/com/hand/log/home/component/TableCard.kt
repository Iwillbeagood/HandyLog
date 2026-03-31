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
import com.hand.log.designsystem.theme.HandyTheme
import com.hand.log.domain.model.GameType
import com.hand.log.ui.localizedLabel
import com.hand.log.domain.model.PokerTable
import com.hand.log.domain.model.TableListItem
import handylog.core.res.generated.resources.Res
import handylog.core.res.generated.resources.*
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import com.hand.log.designsystem.etc.ThemePreview
import com.hand.log.designsystem.etc.ThemePreviews

@Composable
internal fun TableCard(
	item: TableListItem,
	onClick: () -> Unit,
	modifier: Modifier = Modifier,
) {
	val colors = HandyTheme.colorScheme
	val table = item.table

	Row(
		modifier = modifier
			.fillMaxWidth()
			.clip(RoundedCornerShape(12.dp))
			.background(colors.card)
			.border(1.dp, colors.border, RoundedCornerShape(12.dp))
			.clickable(onClick = onClick)
			.padding(16.dp),
		verticalAlignment = Alignment.Top,
	) {
		Column(
			modifier = Modifier.weight(1f),
		) {
			// Row 1: Game type badge + Location
			Row(
				verticalAlignment = Alignment.CenterVertically,
				horizontalArrangement = Arrangement.spacedBy(8.dp),
			) {
				// Game type badge with icon
				Row(
					verticalAlignment = Alignment.CenterVertically,
					horizontalArrangement = Arrangement.spacedBy(4.dp),
					modifier = Modifier
						.clip(RoundedCornerShape(50))
						.background(
							if (table.gameType is GameType.Cash) {
								colors.primary.copy(alpha = 0.15f)
							} else {
								colors.gold.copy(alpha = 0.15f)
							},
						)
						.padding(horizontal = 8.dp, vertical = 3.dp),
				) {
					val badgeColor = if (table.gameType is GameType.Cash) colors.primary else colors.gold
					Icon(
						painter = painterResource(
							if (table.gameType is GameType.Tournament) {
								Res.drawable.trophy
							} else {
								Res.drawable.dollar_sign
							},
						),
						contentDescription = null,
						tint = badgeColor,
						modifier = Modifier.size(12.dp),
					)
					Text(
						text = table.gameType.localizedLabel(),
						style = HandyTheme.typography.bold12,
						color = badgeColor,
					)
				}

				table.location?.let { location ->
					if (location.isNotBlank()) {
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

			Spacer(modifier = Modifier.height(6.dp))

			// Row 2: Date · Player count · Stack
			Row(
				verticalAlignment = Alignment.CenterVertically,
			) {
				Icon(
					painter = painterResource(Res.drawable.calendar),
					contentDescription = null,
					tint = colors.textPrimary,
					modifier = Modifier.size(12.dp),
				)
				Spacer(modifier = Modifier.width(4.dp))
				Text(
					text = table.date.toString(),
					style = HandyTheme.typography.regular12,
					color = colors.textPrimary,
				)
				Icon(
					painter = painterResource(Res.drawable.dot),
					contentDescription = null,
					tint = colors.textSecondary,
					modifier = Modifier.size(12.dp).padding(horizontal = 2.dp),
				)
				Text(
					text = stringResource(Res.string.home_player_count, table.playerCount),
					style = HandyTheme.typography.regular12,
					color = colors.textPrimary,
				)
			}

			// Row 3: Blinds (Cash only)
			(table.gameType as? GameType.Cash)?.let { cash ->
				Spacer(modifier = Modifier.height(2.dp))
				Text(
					text = buildString {
						append("${formatChip(cash.sb)}/${formatChip(cash.bb)}")
						cash.straddle?.let { straddle ->
							append(" (스트래들 ${formatChip(straddle)})")
						}
					},
					style = HandyTheme.typography.regular12,
					color = colors.textSecondary,
				)
			}

			// Row 4: Hand count
			Spacer(modifier = Modifier.height(8.dp))
			Text(
				text = stringResource(Res.string.home_hand_count, item.handCount),
				style = HandyTheme.typography.regular10,
				color = colors.textPrimary,
			)
		}

		// Chevron right
		Icon(
			painter = painterResource(Res.drawable.chevron_right),
			contentDescription = null,
			tint = colors.textSecondary,
			modifier = Modifier.size(20.dp),
		)
	}
}

private fun formatChip(value: Double): String {
	return if (value == value.toLong().toDouble()) {
		value.toLong().toString()
	} else {
		((value * 10).toLong() / 10.0).toString()
	}
}

@ThemePreviews
@Composable
private fun TableCardCashPreview() {
	ThemePreview {
		TableCard(
			item = TableListItem(
				table = PokerTable(
					id = "1",
					date = LocalDate(2025, 3, 10),
					location = "강남 홀덤펍",
					gameType = GameType.Cash(sb = 1000.0, bb = 2000.0, straddle = 4000.0),
					heroSeat = 5,
					createdAt = 1710000000000L,
				),
				handCount = 12,
			),
			onClick = {},
		)
	}
}

@ThemePreviews
@Composable
private fun TableCardTournamentPreview() {
	ThemePreview {
		TableCard(
			item = TableListItem(
				table = PokerTable(
					id = "2",
					date = LocalDate(2025, 3, 9),
					location = "WPT Korea",
					gameType = GameType.Tournament(),
					heroSeat = 3,
					createdAt = 1709900000000L,
				),
				handCount = 5,
			),
			onClick = {},
		)
	}
}

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
import com.hand.log.designsystem.component.Badge
import com.hand.log.designsystem.theme.HandLogTheme
import com.hand.log.designsystem.theme.HandyTheme
import com.hand.log.domain.model.Blinds
import com.hand.log.domain.model.GameType
import com.hand.log.domain.model.PokerTable
import com.hand.log.home.contract.TableListItem
import handylog.core.res.generated.resources.Res
import handylog.core.res.generated.resources.chevron_right
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview

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
				Badge(
					text = table.gameType.label,
					color = if (table.gameType == GameType.CASH) {
						colors.primary.copy(alpha = 0.15f)
					} else {
						colors.gold.copy(alpha = 0.15f)
					},
					textColor = if (table.gameType == GameType.CASH) colors.primary else colors.gold,
					pill = true,
				)
				table.location?.let { location ->
					if (location.isNotBlank()) {
						Text(
							text = location,
							style = HandyTheme.typography.regular12,
							color = colors.textSecondary,
						)
					}
				}
			}

			Spacer(modifier = Modifier.height(6.dp))

			// Row 2: Date · Player count · Stack
			Row(
				verticalAlignment = Alignment.CenterVertically,
			) {
				Text(
					text = table.date.toString(),
					style = HandyTheme.typography.regular12,
					color = colors.textSecondary,
				)
				DotSeparator()
				Text(
					text = "${table.playerCount}명",
					style = HandyTheme.typography.regular12,
					color = colors.textSecondary,
				)
				DotSeparator()
				Text(
					text = "${formatChip(table.startingStack)} 스택",
					style = HandyTheme.typography.regular12,
					color = colors.textSecondary,
				)
			}

			// Row 3: Blinds (Cash only)
			if (table.gameType == GameType.CASH) {
				table.blinds?.let { blinds ->
					Spacer(modifier = Modifier.height(2.dp))
					Text(
						text = buildString {
							append("${formatChip(blinds.sb)}/${formatChip(blinds.bb)}")
							blinds.straddle?.let { straddle ->
								append(" (스트래들 ${formatChip(straddle)})")
							}
						},
						style = HandyTheme.typography.regular12,
						color = colors.textSecondary,
					)
				}
			}

			// Row 4: Hand count
			Spacer(modifier = Modifier.height(8.dp))
			Text(
				text = "${item.handCount}개 핸드 기록",
				style = HandyTheme.typography.regular12,
				color = colors.textSecondary,
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

@Composable
private fun DotSeparator() {
	val colors = HandyTheme.colorScheme
	Spacer(modifier = Modifier.width(6.dp))
	Text(
		text = "•",
		style = HandyTheme.typography.regular12,
		color = colors.border,
	)
	Spacer(modifier = Modifier.width(6.dp))
}

private fun formatChip(value: Double): String {
	return if (value == value.toLong().toDouble()) {
		value.toLong().toString()
	} else {
		((value * 10).toLong() / 10.0).toString()
	}
}

@Preview
@Composable
private fun TableCardCashPreview() {
	HandLogTheme {
		TableCard(
			item = TableListItem(
				table = PokerTable(
					id = "1",
					date = LocalDate(2025, 3, 10),
					location = "강남 홀덤펍",
					gameType = GameType.CASH,
					startingStack = 200000.0,
					blinds = Blinds(sb = 1000.0, bb = 2000.0, straddle = 4000.0),
					playerCount = 9,
					heroSeat = 5,
					createdAt = 1710000000000L,
				),
				handCount = 12,
			),
			onClick = {},
		)
	}
}

@Preview
@Composable
private fun TableCardTournamentPreview() {
	HandLogTheme {
		TableCard(
			item = TableListItem(
				table = PokerTable(
					id = "2",
					date = LocalDate(2025, 3, 9),
					location = "WPT Korea",
					gameType = GameType.TOURNAMENT,
					startingStack = 50000.0,
					playerCount = 6,
					heroSeat = 3,
					createdAt = 1709900000000L,
				),
				handCount = 5,
			),
			onClick = {},
		)
	}
}

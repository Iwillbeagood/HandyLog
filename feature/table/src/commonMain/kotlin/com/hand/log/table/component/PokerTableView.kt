package com.hand.log.table.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.hand.log.designsystem.theme.HandLogTheme
import com.hand.log.designsystem.theme.HandyTheme
import com.hand.log.domain.model.Blinds
import com.hand.log.domain.model.GameType
import com.hand.log.domain.model.Player
import com.hand.log.domain.model.PlayerTendency
import com.hand.log.domain.model.PokerTable
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.ui.tooling.preview.Preview
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

@Composable
internal fun PokerTableView(
	table: PokerTable,
	modifier: Modifier = Modifier,
) {
	val colors = HandyTheme.colorScheme

	Box(
		modifier = modifier
			.clip(RoundedCornerShape(12.dp))
			.background(colors.card)
			.padding(16.dp),
	) {
		BoxWithConstraints(
			modifier = Modifier
				.fillMaxWidth()
				.aspectRatio(1.6f),
		) {
			val density = LocalDensity.current
			val containerWidthPx = with(density) { maxWidth.toPx() }
			val containerHeightPx = with(density) { maxHeight.toPx() }

			// Oval table background
			Box(
				modifier = Modifier
					.fillMaxWidth(0.80f)
					.aspectRatio(1.7f)
					.align(Alignment.Center)
					.clip(RoundedCornerShape(40))
					.background(colors.felt)
					.border(2.dp, colors.feltLight, RoundedCornerShape(40)),
				contentAlignment = Alignment.Center,
			) {
				// Center info
				Column(
					horizontalAlignment = Alignment.CenterHorizontally,
					verticalArrangement = Arrangement.spacedBy(2.dp),
				) {
					Text(
						text = "POKER TABLE",
						style = HandyTheme.typography.medium14,
						color = colors.feltLight.copy(alpha = 0.4f),
					)
				}
			}

			// Total positions = playerCount + 1 (dealer at bottom center)
			val totalPositions = table.playerCount + 1
			val radiusX = containerWidthPx * 0.44f
			val radiusY = containerHeightPx * 0.42f
			val centerX = containerWidthPx / 2f
			val centerY = containerHeightPx / 2f
			val seatSizeDp = 52.dp
			val seatSizePx = with(density) { seatSizeDp.toPx() }

			// Position 0 = Dealer (bottom center)
			for (posIndex in 0 until totalPositions) {
				val angle = (2 * kotlin.math.PI * posIndex / totalPositions) - (kotlin.math.PI / 2)
				val x = centerX + radiusX * cos(angle).toFloat() - seatSizePx / 2f
				val y = centerY + radiusY * sin(angle).toFloat() - seatSizePx / 2f

				if (posIndex == 0) {
					// Dealer position
					SeatView(
						seatNumber = 0,
						player = null,
						isHero = false,
						isDealer = true,
						modifier = Modifier
							.size(seatSizeDp)
							.offset { IntOffset(x.roundToInt(), y.roundToInt()) },
					)
				} else {
					// Player seats: position 1 → seat 1, position 2 → seat 2, ...
					val seatIndex = posIndex
					val player = table.players.find { it.seat == seatIndex }
					val isHero = seatIndex == table.heroSeat

					SeatView(
						seatNumber = seatIndex,
						player = player,
						isHero = isHero,
						isDealer = false,
						modifier = Modifier
							.size(seatSizeDp)
							.offset { IntOffset(x.roundToInt(), y.roundToInt()) },
					)
				}
			}
		}
	}
}

@Composable
private fun SeatView(
	seatNumber: Int,
	player: Player?,
	isHero: Boolean,
	isDealer: Boolean = false,
	modifier: Modifier = Modifier,
) {
	val colors = HandyTheme.colorScheme
	val borderColor = when {
		isDealer -> colors.accent
		isHero -> colors.gold
		player != null -> colors.primary
		else -> colors.border
	}
	val bgColor = when {
		isDealer -> colors.accent.copy(alpha = 0.15f)
		isHero -> colors.gold.copy(alpha = 0.15f)
		player != null -> colors.secondary
		else -> colors.muted
	}

	Column(
		modifier = modifier,
		horizontalAlignment = Alignment.CenterHorizontally,
		verticalArrangement = Arrangement.Center,
	) {
		// Seat circle
		Box(
			modifier = Modifier
				.size(32.dp)
				.clip(CircleShape)
				.background(bgColor)
				.border(
					width = if (isDealer || isHero || player != null) 2.dp else 1.dp,
					color = borderColor,
					shape = CircleShape,
				),
			contentAlignment = Alignment.Center,
		) {
			if (isDealer) {
				Text(
					text = "D",
					style = HandyTheme.typography.bold12,
					color = colors.accent,
					textAlign = TextAlign.Center,
				)
			} else {
				Text(
					text = player?.name?.take(1) ?: "$seatNumber",
					style = HandyTheme.typography.bold10,
					color = if (isHero) colors.gold else colors.textPrimary,
					textAlign = TextAlign.Center,
				)
			}
		}

		// Label
		if (isDealer) {
			Text(
				text = "Dealer",
				style = HandyTheme.typography.medium8,
				color = colors.accent,
				maxLines = 1,
			)
		} else if (player != null) {
			Text(
				text = formatChips(player.stack),
				style = HandyTheme.typography.medium8,
				color = if (isHero) colors.gold else colors.textSecondary,
				maxLines = 1,
				overflow = TextOverflow.Ellipsis,
			)
		}
	}
}

private fun formatChips(amount: Double): String {
	return when {
		amount >= 1_000_000 -> "${formatDecimal(amount / 1_000_000)}M"
		amount >= 1_000 -> "${formatDecimal(amount / 1_000)}K"
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

@Preview
@Composable
private fun PokerTableViewPreview() {
	HandLogTheme {
		PokerTableView(
			table = PokerTable(
				id = "1",
				date = LocalDate(2026, 3, 12),
				location = "강남",
				gameType = GameType.CASH,
				startingStack = 50000.0,
				blinds = Blinds(sb = 500.0, bb = 1000.0),
				playerCount = 9,
				heroSeat = 3,
				players = listOf(
					Player(seat = 1, stack = 45000.0, name = "Fish"),
					Player(seat = 2, stack = 52000.0, name = "Shark"),
					Player(seat = 3, stack = 62000.0, name = "Hero"),
					Player(seat = 5, stack = 38000.0, name = "Nit"),
					Player(seat = 7, stack = 71000.0, tendency = PlayerTendency.LAG),
				),
				createdAt = 1710000000000L,
			),
			modifier = Modifier
				.fillMaxWidth()
				.padding(16.dp),
		)
	}
}

@Preview
@Composable
private fun PokerTableView6MaxPreview() {
	HandLogTheme {
		PokerTableView(
			table = PokerTable(
				id = "2",
				date = LocalDate(2026, 3, 12),
				gameType = GameType.TOURNAMENT,
				startingStack = 10000.0,
				blinds = Blinds(sb = 100.0, bb = 200.0),
				playerCount = 6,
				heroSeat = 1,
				players = listOf(
					Player(seat = 1, stack = 12000.0, name = "Hero"),
					Player(seat = 4, stack = 8500.0, name = "Villain"),
				),
				createdAt = 1710000000000L,
			),
			modifier = Modifier
				.fillMaxWidth()
				.padding(16.dp),
		)
	}
}

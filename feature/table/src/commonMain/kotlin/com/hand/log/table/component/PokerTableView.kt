package com.hand.log.table.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
import handylog.core.res.generated.resources.Res
import handylog.core.res.generated.resources.crown
import handylog.core.res.generated.resources.user_round
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

private val TendencyColors = mapOf(
	PlayerTendency.TIGHT to Color(0xFF4A90D9),
	PlayerTendency.LOOSE to Color(0xFFE8943A),
	PlayerTendency.AGGRESSIVE to Color(0xFFE84040),
	PlayerTendency.PASSIVE to Color(0xFF7B8FA0),
	PlayerTendency.NIT to Color(0xFF6BC5E8),
	PlayerTendency.MANIAC to Color(0xFFD94ABB),
	PlayerTendency.UNKNOWN to Color(0xFF808897),
)

@Composable
internal fun PokerTableView(
	table: PokerTable,
	onSeatClick: (Int) -> Unit = {},
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

			val totalPositions = table.playerCount + 1
			val radiusX = containerWidthPx * 0.44f
			val radiusY = containerHeightPx * 0.42f
			val centerX = containerWidthPx / 2f
			val centerY = containerHeightPx / 2f
			val seatSizeDp = 52.dp
			val seatSizePx = with(density) { seatSizeDp.toPx() }

			for (posIndex in 0 until totalPositions) {
				val angle = (2 * kotlin.math.PI * posIndex / totalPositions) - (kotlin.math.PI / 2)
				val x = centerX + radiusX * cos(angle).toFloat() - seatSizePx / 2f
				val y = centerY + radiusY * sin(angle).toFloat() - seatSizePx / 2f

				if (posIndex == 0) {
					DealerView(
						modifier = Modifier
							.size(seatSizeDp)
							.offset { IntOffset(x.roundToInt(), y.roundToInt()) },
					)
				} else {
					val seatIndex = posIndex
					val player = table.players.find { it.seat == seatIndex }
					val isHero = seatIndex == table.heroSeat

					SeatView(
						seatNumber = seatIndex,
						player = player,
						isHero = isHero,
						onClick = { onSeatClick(seatIndex) },
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
private fun DealerView(
	modifier: Modifier = Modifier,
) {
	val colors = HandyTheme.colorScheme

	Column(
		modifier = modifier,
		horizontalAlignment = Alignment.CenterHorizontally,
		verticalArrangement = Arrangement.Center,
	) {
		Box(
			modifier = Modifier
				.size(32.dp)
				.clip(RoundedCornerShape(8.dp))
				.background(colors.secondary)
				.border(1.dp, colors.border, RoundedCornerShape(8.dp)),
			contentAlignment = Alignment.Center,
		) {
			Icon(
				painter = painterResource(Res.drawable.user_round),
				contentDescription = "Dealer",
				tint = colors.textSecondary,
				modifier = Modifier.size(16.dp),
			)
		}
		Text(
			text = "Dealer",
			style = HandyTheme.typography.medium8,
			color = colors.textSecondary,
			maxLines = 1,
		)
	}
}

@Composable
private fun SeatView(
	seatNumber: Int,
	player: Player?,
	isHero: Boolean,
	onClick: (() -> Unit)? = null,
	modifier: Modifier = Modifier,
) {
	val colors = HandyTheme.colorScheme

	val tendencyColor = player?.tendency?.let { TendencyColors[it] }

	val borderColor = when {
		isHero -> colors.gold
		tendencyColor != null -> tendencyColor
		player != null -> colors.primary
		else -> colors.border
	}
	val bgColor = when {
		isHero -> colors.secondary
		player != null -> colors.secondary
		else -> colors.muted
	}

	Column(
		modifier = modifier
			.clip(RoundedCornerShape(4.dp))
			.then(
				if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier,
			),
		horizontalAlignment = Alignment.CenterHorizontally,
		verticalArrangement = Arrangement.Center,
	) {
		Box(
			modifier = Modifier
				.size(32.dp)
				.clip(CircleShape)
				.background(bgColor)
				.border(
					width = if (isHero || player != null) 2.dp else 1.dp,
					color = borderColor,
					shape = CircleShape,
				),
			contentAlignment = Alignment.Center,
		) {
			if (isHero) {
				Icon(
					painter = painterResource(Res.drawable.crown),
					contentDescription = "Hero",
					tint = colors.gold,
					modifier = Modifier.size(14.dp),
				)
			} else {
				Text(
					text = player?.name?.take(1) ?: "$seatNumber",
					style = HandyTheme.typography.bold10,
					color = tendencyColor ?: colors.textPrimary,
					textAlign = TextAlign.Center,
				)
			}
		}

		if (isHero) {
			Text(
				text = formatChips(player?.stack ?: 0.0),
				style = HandyTheme.typography.medium8,
				color = colors.gold,
				maxLines = 1,
				overflow = TextOverflow.Ellipsis,
			)
		} else if (player != null) {
			Text(
				text = formatChips(player.stack),
				style = HandyTheme.typography.medium8,
				color = tendencyColor ?: colors.textSecondary,
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
					Player(seat = 1, stack = 45000.0, name = "Fish", tendency = PlayerTendency.LOOSE),
					Player(seat = 2, stack = 52000.0, name = "Shark", tendency = PlayerTendency.AGGRESSIVE),
					Player(seat = 3, stack = 62000.0, name = "Hero"),
					Player(seat = 5, stack = 38000.0, name = "Nit", tendency = PlayerTendency.NIT),
					Player(seat = 7, stack = 71000.0, tendency = PlayerTendency.MANIAC),
					Player(seat = 9, stack = 48000.0, name = "Tag", tendency = PlayerTendency.TIGHT),
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
					Player(seat = 4, stack = 8500.0, name = "Villain", tendency = PlayerTendency.PASSIVE),
				),
				createdAt = 1710000000000L,
			),
			modifier = Modifier
				.fillMaxWidth()
				.padding(16.dp),
		)
	}
}

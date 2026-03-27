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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.hand.log.designsystem.etc.ThemePreview
import com.hand.log.designsystem.etc.ThemePreviews
import com.hand.log.designsystem.theme.HandyTheme
import com.hand.log.designsystem.theme.nonScaledSp
import com.hand.log.domain.model.GameType
import com.hand.log.domain.model.Player
import com.hand.log.domain.model.PlayerTendency
import com.hand.log.domain.model.PokerTable
import com.hand.log.ui.color.tendencyColor
import handylog.core.res.generated.resources.Res
import handylog.core.res.generated.resources.arrow_down
import handylog.core.res.generated.resources.crown
import handylog.core.res.generated.resources.user_round
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.painterResource
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

@Composable
internal fun PokerTableView(
	table: PokerTable,
	onSeatClick: (Int) -> Unit = {},
	modifier: Modifier = Modifier,
) {
	val colors = HandyTheme.colorScheme
	val maxPlayers = table.maxPlayers.takeIf { it > 0 } ?: table.playerCount

	Box(
		modifier = modifier
			.clip(RoundedCornerShape(12.dp))
			.background(colors.card)
			.padding(16.dp),
	) {
		BoxWithConstraints(
			modifier = Modifier
				.fillMaxWidth()
				.aspectRatio(1.4f),
		) {
			val density = LocalDensity.current
			val containerWidthPx = with(density) { maxWidth.toPx() }
			val containerHeightPx = with(density) { maxHeight.toPx() }

			// 테이블 타원 배경
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
				Text(
					text = "POKER TABLE",
					style = HandyTheme.typography.medium14.nonScaledSp,
					color = colors.feltLight.copy(alpha = 0.4f),
				)
			}

			val centerX = containerWidthPx / 2f
			val centerY = containerHeightPx / 2f
			val tableWidth = containerWidthPx * 0.80f
			val tableHeight = tableWidth / 1.7f
			val tableRadiusX = tableWidth / 2f
			val tableRadiusY = tableHeight / 2f
			val seatSizeDp = 48.dp
			val seatSizePx = with(density) { seatSizeDp.toPx() }
			val gapPx = with(density) { 1.dp.toPx() }
			val seatHalf = seatSizePx / 2f

			// 딜러 + maxPlayers 좌석 배치
			val totalPositions = maxPlayers + 1
			for (posIndex in 0 until totalPositions) {
				val angle = (2 * kotlin.math.PI * posIndex / totalPositions) - (kotlin.math.PI / 2)
				val cosA = cos(angle).toFloat()
				val sinA = sin(angle).toFloat()
				val sx = centerX + (tableRadiusX + gapPx + seatHalf) * cosA - seatHalf
				val sy = centerY + (tableRadiusY + gapPx + seatHalf) * sinA - seatHalf

				if (posIndex == 0) {
					DealerView(
						modifier = Modifier
							.size(seatSizeDp)
							.offset { IntOffset(sx.roundToInt(), sy.roundToInt()) },
					)
				} else {
					val seatNum = posIndex
					val player = table.players.find { it.seat == seatNum }
					val isHero = seatNum == table.heroSeat
					val hasPlayer = player != null || isHero

					if (hasPlayer) {
						SeatView(
							seatNumber = seatNum,
							player = player,
							isHero = isHero,
							onClick = { onSeatClick(seatNum) },
							modifier = Modifier
								.size(seatSizeDp)
								.offset { IntOffset(sx.roundToInt(), sy.roundToInt()) },
						)
					} else {
						EmptySeatView(
							onClick = { onSeatClick(seatNum) },
							modifier = Modifier
								.size(seatSizeDp)
								.offset { IntOffset(sx.roundToInt(), sy.roundToInt()) },
						)
					}
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
				contentDescription = null,
				tint = colors.textSecondary,
				modifier = Modifier.size(16.dp),
			)
		}
		Text(
			text = "Dealer",
			style = HandyTheme.typography.medium8.nonScaledSp,
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
	val tendencyColor = player?.tendency?.tendencyColor()

	val borderColor = when {
		isHero -> colors.gold
		tendencyColor != null -> tendencyColor
		player != null -> colors.primary
		else -> colors.border
	}
	val bgColor = when {
		isHero -> colors.gold.copy(alpha = 0.15f)
		player != null -> colors.secondary
		else -> colors.muted
	}

	Column(
		modifier = modifier
			.clip(RoundedCornerShape(4.dp))
			.then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
		horizontalAlignment = Alignment.CenterHorizontally,
		verticalArrangement = Arrangement.Center,
	) {
		Box(
			modifier = Modifier
				.size(32.dp)
				.clip(CircleShape)
				.background(bgColor)
				.border(2.dp, borderColor, CircleShape),
			contentAlignment = Alignment.Center,
		) {
			if (isHero) {
				Icon(
					painter = painterResource(Res.drawable.crown),
					contentDescription = null,
					tint = colors.gold,
					modifier = Modifier.size(14.dp),
				)
			} else {
				Text(
					text = player?.name?.take(1) ?: "$seatNumber",
					style = HandyTheme.typography.bold10.nonScaledSp,
					color = tendencyColor ?: colors.textPrimary,
					textAlign = TextAlign.Center,
				)
			}
		}

	}
}

@Composable
private fun EmptySeatView(
	onClick: () -> Unit,
	modifier: Modifier = Modifier,
) {
	val colors = HandyTheme.colorScheme

	Column(
		modifier = modifier
			.clip(RoundedCornerShape(4.dp))
			.clickable(onClick = onClick),
		horizontalAlignment = Alignment.CenterHorizontally,
		verticalArrangement = Arrangement.Center,
	) {
		Box(
			modifier = Modifier
				.size(32.dp)
				.clip(CircleShape)
				.background(colors.muted.copy(alpha = 0.5f))
				.border(1.dp, colors.border.copy(alpha = 0.5f), CircleShape),
			contentAlignment = Alignment.Center,
		) {
			Icon(
				painter = painterResource(Res.drawable.arrow_down),
				contentDescription = null,
				tint = colors.textSecondary.copy(alpha = 0.5f),
				modifier = Modifier.size(14.dp),
			)
		}
	}
}

@ThemePreviews
@Composable
private fun PokerTableViewPreview() {
	ThemePreview {
		PokerTableView(
			table = PokerTable(
				id = "1",
				date = LocalDate(2026, 3, 12),
				location = "강남",
				gameType = GameType.Cash(sb = 500.0, bb = 1000.0),
				maxPlayers = 9,
				playerCount = 6,
				heroSeat = 3,
				players = listOf(
					Player(seat = 1, name = "Fish", tendency = PlayerTendency.LOOSE),
					Player(seat = 2, name = "Shark", tendency = PlayerTendency.AGGRESSIVE),
					Player(seat = 3, name = "Hero"),
					Player(seat = 5, name = "Nit", tendency = PlayerTendency.NIT),
					Player(seat = 7, tendency = PlayerTendency.MANIAC),
					Player(seat = 9, name = "Tag", tendency = PlayerTendency.TIGHT),
				),
				createdAt = 1710000000000L,
			),
			modifier = Modifier
				.fillMaxWidth()
				.padding(16.dp),
		)
	}
}

@ThemePreviews
@Composable
private fun PokerTableView6MaxPreview() {
	ThemePreview {
		PokerTableView(
			table = PokerTable(
				id = "2",
				date = LocalDate(2026, 3, 12),
				gameType = GameType.Tournament(),
				maxPlayers = 6,
				playerCount = 2,
				heroSeat = 1,
				players = listOf(
					Player(seat = 1, name = "Hero"),
					Player(seat = 4, name = "Villain", tendency = PlayerTendency.PASSIVE),
				),
				createdAt = 1710000000000L,
			),
			modifier = Modifier
				.fillMaxWidth(),
		)
	}
}

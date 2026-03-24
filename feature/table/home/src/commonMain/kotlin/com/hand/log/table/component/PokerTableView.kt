package com.hand.log.table.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.hand.log.designsystem.theme.HandyTheme
import com.hand.log.designsystem.theme.nonScaledSp
import com.hand.log.domain.model.Blinds
import com.hand.log.domain.model.GameType
import com.hand.log.domain.model.Player
import com.hand.log.domain.model.PlayerTendency
import com.hand.log.domain.model.PokerTable
import com.hand.log.ui.color.tendencyColor
import com.hand.log.ui.poker.PokerSeatCircle
import com.hand.log.ui.poker.PokerTableLayout
import com.hand.log.ui.poker.TableLayoutConfig
import com.hand.log.ui.poker.formatChips
import handylog.core.res.generated.resources.Res
import handylog.core.res.generated.resources.arrow_down
import handylog.core.res.generated.resources.user_round
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.painterResource
import com.hand.log.designsystem.etc.ThemePreview
import com.hand.log.designsystem.etc.ThemePreviews

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
		PokerTableLayout(
			playerCount = table.playerCount,
			modifier = Modifier.fillMaxWidth(),
			config = TableLayoutConfig(
				containerAspectRatio = 1.6f,
				tableWidthFraction = 0.80f,
				tableAspectRatio = 1.7f,
				seatSize = 52.dp,
			),
			tableContent = {
				Text(
					text = "POKER TABLE",
					style = HandyTheme.typography.medium14,
					color = colors.feltLight.copy(alpha = 0.4f),
				)
			},
			seatContent = { pos ->
				val player = table.players.find { it.seat == pos.seat }
				val isHero = pos.seat == table.heroSeat
				val isEmpty = player == null && !isHero
				val tColor = player?.tendency?.tendencyColor()

				val borderColor = when {
					isEmpty -> colors.border.copy(alpha = 0.5f)
					isHero -> colors.gold
					tColor != null -> tColor
					player != null -> colors.primary
					else -> colors.border
				}
				val bgColor = when {
					isEmpty -> colors.muted.copy(alpha = 0.5f)
					isHero -> colors.secondary
					player != null -> colors.secondary
					else -> colors.muted
				}

				PokerSeatCircle(
					text = player?.name?.take(1) ?: "${pos.seat}",
					isHero = isHero,
					borderColor = borderColor,
					bgColor = bgColor,
					textColor = tColor ?: colors.textPrimary,
					borderWidth = if (isEmpty) 1.dp else if (isHero || player != null) 2.dp else 1.dp,
					modifier = Modifier
						.clip(RoundedCornerShape(4.dp))
						.clickable { onSeatClick(pos.seat) },
				) {
					when {
						isEmpty -> {
							Icon(
								painter = painterResource(Res.drawable.arrow_down),
								contentDescription = null,
								tint = colors.textSecondary.copy(alpha = 0.5f),
								modifier = Modifier.size(10.dp),
							)
						}
						else -> {
							val stack = player?.stack ?: 0.0
							if (stack > 0) {
								Text(
									text = formatChips(stack),
									style = HandyTheme.typography.medium8.nonScaledSp,
									color = if (isHero) colors.gold else tColor ?: colors.textSecondary,
									maxLines = 1,
									overflow = TextOverflow.Ellipsis,
								)
							}
						}
					}
				}
			},
		)
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
				createdAt = 0L,
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
				gameType = GameType.TOURNAMENT,
				startingStack = 10000.0,
				blinds = Blinds(sb = 100.0, bb = 200.0),
				playerCount = 6,
				heroSeat = 1,
				players = listOf(
					Player(seat = 1, stack = 12000.0, name = "Hero"),
					Player(seat = 4, stack = 8500.0, name = "Villain", tendency = PlayerTendency.PASSIVE),
				),
				createdAt = 0L,
			),
			modifier = Modifier
				.fillMaxWidth()
				.padding(16.dp),
		)
	}
}

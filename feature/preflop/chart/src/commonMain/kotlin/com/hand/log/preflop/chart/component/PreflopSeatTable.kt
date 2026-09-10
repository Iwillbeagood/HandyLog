package com.hand.log.preflop.chart.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.hand.log.designsystem.etc.ThemePreview
import com.hand.log.designsystem.etc.ThemePreviews
import com.hand.log.designsystem.theme.HandyTheme
import com.hand.log.designsystem.theme.nonScaledSp
import com.hand.log.domain.model.Position
import com.hand.log.domain.model.preflop.PreflopPositions
import com.hand.log.domain.model.preflop.PreflopScenario
import handylog.core.res.generated.resources.Res
import handylog.core.res.generated.resources.crown
import handylog.core.res.generated.resources.poker_chip
import handylog.core.res.generated.resources.preflop_action_3bet
import handylog.core.res.generated.resources.preflop_action_limp
import handylog.core.res.generated.resources.preflop_action_raise
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

private val CHIP_SIZE = 18.dp
private const val CHIP_SEAT_GAP = 4f

@Composable
internal fun PreflopSeatTable(
	hero: Position,
	villain: Position?,
	scenario: PreflopScenario,
	positions: List<Position>,
	onSeatSelect: (Position) -> Unit,
	modifier: Modifier = Modifier,
) {
	SeatRing(
		hero = hero,
		villain = villain,
		villainActionLabel = villainActionLabel(scenario),
		positions = positions,
		onSeatSelect = onSeatSelect,
		modifier = modifier.fillMaxWidth(),
	)
}

@Composable
private fun SeatRing(
	hero: Position,
	villain: Position?,
	villainActionLabel: String?,
	positions: List<Position>,
	onSeatSelect: (Position) -> Unit,
	modifier: Modifier = Modifier,
) {
	val colors = HandyTheme.colorScheme

	Box(
		modifier = modifier
			.clip(RoundedCornerShape(12.dp))
			.background(colors.card)
			.padding(vertical = 12.dp),
	) {
		BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
			val ringW = maxWidth
			val ringH = ringW * 0.58f
			val cx = ringW / 2
			val cy = ringH / 2
			val rx = ringW * 0.40f
			val ry = ringH * 0.36f
			val seat = 30.dp

			Box(modifier = Modifier.fillMaxWidth().height(ringH)) {
				Box(
					modifier = Modifier
						.align(Alignment.Center)
						.size(width = ringW * 0.66f, height = ringH * 0.62f)
						.clip(RoundedCornerShape(percent = 50))
						.background(colors.felt)
						.border(2.dp, colors.feltLight, RoundedCornerShape(percent = 50)),
				)

				val heroIndex = positions.indexOf(hero).coerceAtLeast(0)
				for (i in positions.indices) {
					val pos = positions[(heroIndex + i) % positions.size]
					val angle = (90.0 + i * 360.0 / positions.size) * (PI / 180.0)
					val x = cx + rx * cos(angle).toFloat()
					val y = cy + ry * sin(angle).toFloat()

					val isHero = pos == hero
					val isVillain = pos == villain
					val isSelectable = !isHero
					val labelAbove = sin(angle) < 0

					Seat(
						positionName = pos.label,
						isHero = isHero,
						isVillain = isVillain,
						isSelectable = isSelectable,
						actionLabel = if (isVillain) villainActionLabel else null,
						labelAbove = labelAbove,
						size = seat,
						onClick = if (isSelectable) ({ onSeatSelect(pos) }) else null,
						modifier = Modifier.offset(x = x - seat / 2, y = y - seat / 2),
					)

					val dx = x - cx
					val dy = y - cy
					val dist = sqrt(dx.value * dx.value + dy.value * dy.value)
					val pull = seat.value / 2 + CHIP_SIZE.value / 2 + CHIP_SEAT_GAP
					val mx = x - (dx.value / dist * pull).dp
					val my = y - (dy.value / dist * pull).dp
					val markerMod = Modifier.offset(x = mx - CHIP_SIZE / 2, y = my - CHIP_SIZE / 2)
					when {
						isVillain && villainActionLabel != null -> BetChip(colors.accent, markerMod)
						pos == Position.BTN -> DealerMarker(markerMod)
						pos == Position.SB -> BlindMarker("SB", markerMod)
						pos == Position.BB -> BlindMarker("BB", markerMod)
					}
				}
			}
		}
	}
}

@Composable
private fun Seat(
	positionName: String,
	isHero: Boolean,
	isVillain: Boolean,
	isSelectable: Boolean,
	actionLabel: String?,
	labelAbove: Boolean,
	size: Dp,
	onClick: (() -> Unit)?,
	modifier: Modifier = Modifier,
) {
	val colors = HandyTheme.colorScheme
	val isDimmed = !isHero && !isVillain && !isSelectable
	val borderColor = when {
		isHero -> colors.gold
		isVillain -> colors.accent
		isSelectable -> colors.accent
		else -> colors.border.copy(alpha = 0.3f)
	}
	val bgColor = when {
		isHero -> colors.gold.copy(alpha = 0.15f)
		isVillain -> colors.accent.copy(alpha = 0.15f)
		isSelectable -> colors.accent.copy(alpha = 0.12f)
		else -> colors.muted.copy(alpha = 0.3f)
	}

	Column(
		modifier = modifier
			.let { if (onClick != null) it.clickable(onClick = onClick) else it },
		horizontalAlignment = Alignment.CenterHorizontally,
		verticalArrangement = Arrangement.spacedBy(2.dp),
	) {
		if (isHero) {
			Icon(
				painter = painterResource(Res.drawable.crown),
				contentDescription = null,
				tint = colors.gold,
				modifier = Modifier.size(12.dp),
			)
		}
		if (actionLabel != null && labelAbove) {
			ActionLabel(actionLabel)
		}
		Box(
			modifier = Modifier
				.size(size)
				.clip(CircleShape)
				.background(bgColor)
				.border(
					width = if (isHero || isVillain || isSelectable) 2.dp else 1.dp,
					color = borderColor,
					shape = CircleShape,
				),
			contentAlignment = Alignment.Center,
		) {
			Text(
				text = positionName,
				style = HandyTheme.typography.bold8.nonScaledSp,
				color = when {
					isHero -> colors.gold
					isVillain -> colors.accent
					isDimmed -> colors.textPrimary.copy(alpha = 0.4f)
					else -> colors.textPrimary
				},
				maxLines = 1,
				softWrap = false,
			)
		}
		if (actionLabel != null && !labelAbove) {
			ActionLabel(actionLabel)
		}
	}
}

@Composable
private fun ActionLabel(text: String) {
	Text(
		text = text,
		style = HandyTheme.typography.bold8,
		color = HandyTheme.colorScheme.accent,
	)
}

@Composable
private fun DealerMarker(modifier: Modifier = Modifier) {
	Box(
		modifier = modifier
			.size(CHIP_SIZE)
			.clip(CircleShape)
			.background(Color(0xFF3A3A3A)),
		contentAlignment = Alignment.Center,
	) {
		Text(
			text = "D",
			style = HandyTheme.typography.bold8,
			color = Color.White,
		)
	}
}

@Composable
private fun BlindMarker(text: String, modifier: Modifier = Modifier) {
	val colors = HandyTheme.colorScheme
	Box(
		modifier = modifier
			.size(CHIP_SIZE)
			.clip(CircleShape)
			.background(colors.gold)
			.border(1.dp, colors.border, CircleShape),
		contentAlignment = Alignment.Center,
	) {
		Text(
			text = text,
			style = HandyTheme.typography.bold8,
			color = Color(0xFF1A1A1A),
		)
	}
}

@Composable
private fun BetChip(color: Color, modifier: Modifier = Modifier) {
	Icon(
		painter = painterResource(Res.drawable.poker_chip),
		contentDescription = null,
		tint = color,
		modifier = modifier.size(CHIP_SIZE),
	)
}

@Composable
private fun villainActionLabel(scenario: PreflopScenario): String? = when (scenario) {
	PreflopScenario.RFI -> null
	PreflopScenario.FACING_RFI -> stringResource(Res.string.preflop_action_raise)
	PreflopScenario.VS_3BET -> stringResource(Res.string.preflop_action_3bet)
	PreflopScenario.VS_LIMP -> stringResource(Res.string.preflop_action_limp)
}

@ThemePreviews
@Composable
private fun PreflopSeatTableFacingRfiPreview() {
	ThemePreview {
		Box(modifier = Modifier.background(HandyTheme.colorScheme.background).padding(16.dp)) {
			PreflopSeatTable(
				hero = Position.BTN,
				villain = Position.CO,
				scenario = PreflopScenario.FACING_RFI,
				positions = PreflopPositions.order,
				onSeatSelect = {},
			)
		}
	}
}

@ThemePreviews
@Composable
private fun PreflopSeatTableVs3betPreview() {
	ThemePreview {
		Box(modifier = Modifier.background(HandyTheme.colorScheme.background).padding(16.dp)) {
			PreflopSeatTable(
				hero = Position.CO,
				villain = Position.BTN,
				scenario = PreflopScenario.VS_3BET,
				positions = PreflopPositions.order,
				onSeatSelect = {},
			)
		}
	}
}

@ThemePreviews
@Composable
private fun PreflopSeatTableRfiPreview() {
	ThemePreview {
		Box(modifier = Modifier.background(HandyTheme.colorScheme.background).padding(16.dp)) {
			PreflopSeatTable(
				hero = Position.UTG,
				villain = null,
				scenario = PreflopScenario.RFI,
				positions = PreflopPositions.order,
				onSeatSelect = {},
			)
		}
	}
}

@ThemePreviews
@Composable
private fun PreflopSeatTableOnline6MaxPreview() {
	ThemePreview {
		Box(modifier = Modifier.background(HandyTheme.colorScheme.background).padding(16.dp)) {
			PreflopSeatTable(
				hero = Position.HJ,
				villain = Position.UTG,
				scenario = PreflopScenario.FACING_RFI,
				positions = PreflopPositions.onlineOrder,
				onSeatSelect = {},
			)
		}
	}
}

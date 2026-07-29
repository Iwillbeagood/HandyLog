package com.hand.log.preflop.quiz.session.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.hand.log.designsystem.etc.ThemePreview
import com.hand.log.designsystem.etc.ThemePreviews
import com.hand.log.designsystem.theme.HandyTheme
import com.hand.log.domain.model.Position
import com.hand.log.domain.model.Rank
import com.hand.log.domain.model.preflop.HandShape
import com.hand.log.domain.model.preflop.PreflopHand
import com.hand.log.domain.model.preflop.PreflopPositions
import com.hand.log.domain.model.preflop.chartChar
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

private enum class SeatUiState { FOLDED, RAISER, HERO, LIVE }

/**
 * 프리플랍 스팟을 9-max 포커 테이블로 시각화한다.
 * 좌석은 [PreflopPositions.order] 순서로 타원 둘레에 균등 배치하고,
 * 히어로/레이저(빌런)/폴드/라이브 상태를 색으로 구분한다.
 */
@Composable
internal fun QuizPokerTable(
	hero: Position,
	villain: Position?,
	hand: PreflopHand,
	raiseInfo: String?,
	modifier: Modifier = Modifier,
) {
	val colors = HandyTheme.colorScheme
	val positions = PreflopPositions.order
	val heroIndex = positions.indexOf(hero)

	Box(
		modifier = modifier
			.fillMaxWidth()
			.clip(RoundedCornerShape(12.dp))
			.background(colors.card)
			.padding(vertical = 12.dp),
	) {
		BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
			val ringW = maxWidth
			val ringH = ringW * 0.6f
			val cx = ringW / 2
			val cy = ringH / 2
			val rx = ringW * 0.40f
			val ry = ringH * 0.44f
			val seat = 34.dp

			Box(modifier = Modifier.fillMaxWidth().height(ringH)) {
				Box(
					modifier = Modifier
						.align(Alignment.Center)
						.size(width = ringW * 0.66f, height = ringH * 0.62f)
						.clip(RoundedCornerShape(percent = 50))
						.background(colors.felt)
						.border(2.dp, colors.primary.copy(alpha = 0.25f), RoundedCornerShape(percent = 50)),
				)

				HeroCards(
					hand = hand,
					modifier = Modifier.align(Alignment.Center),
				)

				positions.forEachIndexed { i, pos ->
					val angle = (-90.0 + i * 360.0 / positions.size) * (PI / 180.0)
					val x = cx + rx * cos(angle).toFloat()
					val y = cy + ry * sin(angle).toFloat()
					val state = when {
						pos == hero -> SeatUiState.HERO
						pos == villain -> SeatUiState.RAISER
						positions.indexOf(pos) < heroIndex -> SeatUiState.FOLDED
						else -> SeatUiState.LIVE
					}
					Seat(
						label = pos.label,
						state = state,
						modifier = Modifier.offset(x = x - seat / 2, y = y - seat / 2),
						size = seat,
					)
				}

				if (raiseInfo != null) {
					RaiseBanner(
						text = raiseInfo,
						modifier = Modifier
							.align(Alignment.BottomCenter)
							.padding(bottom = ringH * 0.06f),
					)
				}
			}
		}
	}
}

@Composable
private fun Seat(
	label: String,
	state: SeatUiState,
	size: androidx.compose.ui.unit.Dp,
	modifier: Modifier = Modifier,
) {
	val colors = HandyTheme.colorScheme
	val accent = when (state) {
		SeatUiState.HERO -> colors.primary
		SeatUiState.RAISER -> colors.gold
		else -> colors.textSecondary
	}
	val glyph = when (state) {
		SeatUiState.HERO -> "★"
		SeatUiState.RAISER -> "R"
		SeatUiState.FOLDED -> "✕"
		SeatUiState.LIVE -> "•"
	}
	Column(
		modifier = modifier.alpha(if (state == SeatUiState.FOLDED) 0.4f else 1f),
		horizontalAlignment = Alignment.CenterHorizontally,
		verticalArrangement = Arrangement.spacedBy(2.dp),
	) {
		Box(
			modifier = Modifier
				.size(size)
				.clip(CircleShape)
				.background(
					when (state) {
						SeatUiState.HERO -> colors.primary.copy(alpha = 0.15f)
						SeatUiState.RAISER -> colors.gold.copy(alpha = 0.18f)
						else -> colors.muted
					},
				)
				.border(
					width = if (state == SeatUiState.HERO || state == SeatUiState.RAISER) 2.dp else 1.dp,
					color = if (state == SeatUiState.HERO || state == SeatUiState.RAISER) accent else colors.border,
					shape = CircleShape,
				),
			contentAlignment = Alignment.Center,
		) {
			Text(
				text = glyph,
				style = HandyTheme.typography.bold12,
				color = accent,
			)
		}
		Text(
			text = label,
			style = HandyTheme.typography.bold10,
			color = accent,
		)
	}
}

@Composable
private fun HeroCards(hand: PreflopHand, modifier: Modifier = Modifier) {
	val (first, second) = handCards(hand)
	Row(
		modifier = modifier,
		horizontalArrangement = Arrangement.spacedBy(4.dp),
	) {
		MiniCard(first.first, first.second)
		MiniCard(second.first, second.second)
	}
}

@Composable
private fun MiniCard(rank: Rank, red: Boolean) {
	val colors = HandyTheme.colorScheme
	Column(
		modifier = Modifier
			.size(width = 30.dp, height = 40.dp)
			.clip(RoundedCornerShape(4.dp))
			.background(Color.White),
		verticalArrangement = Arrangement.Center,
		horizontalAlignment = Alignment.CenterHorizontally,
	) {
		Text(
			text = rank.chartChar,
			style = HandyTheme.typography.bold14,
			color = if (red) colors.suitRed else colors.suitBlack,
		)
		Text(
			text = if (red) "♥" else "♠",
			style = HandyTheme.typography.regular10,
			color = if (red) colors.suitRed else colors.suitBlack,
		)
	}
}

@Composable
private fun RaiseBanner(text: String, modifier: Modifier = Modifier) {
	val colors = HandyTheme.colorScheme
	Row(
		modifier = modifier
			.clip(RoundedCornerShape(12.dp))
			.background(Color.Black.copy(alpha = 0.6f))
			.padding(horizontal = 10.dp, vertical = 4.dp),
		horizontalArrangement = Arrangement.spacedBy(4.dp),
		verticalAlignment = Alignment.CenterVertically,
	) {
		Box(
			modifier = Modifier
				.size(8.dp)
				.clip(CircleShape)
				.background(colors.gold),
		)
		Text(
			text = text,
			style = HandyTheme.typography.medium12,
			color = colors.textSecondary,
		)
	}
}

private fun handCards(hand: PreflopHand): Pair<Pair<Rank, Boolean>, Pair<Rank, Boolean>> = when (hand.shape) {
	HandShape.PAIR -> (hand.high to false) to (hand.high to true)
	HandShape.SUITED -> (hand.high to false) to (hand.low to false)
	HandShape.OFFSUIT -> (hand.high to false) to (hand.low to true)
}

@ThemePreviews
@Composable
private fun QuizPokerTableFacingRfiPreview() {
	ThemePreview {
		Box(modifier = Modifier.background(HandyTheme.colorScheme.background).padding(16.dp)) {
			QuizPokerTable(
				hero = Position.CO,
				villain = Position.HJ,
				hand = PreflopHand(Rank.ACE, Rank.KING, HandShape.SUITED),
				raiseInfo = "HJ 레이즈",
			)
		}
	}
}

@ThemePreviews
@Composable
private fun QuizPokerTableRfiPreview() {
	ThemePreview {
		Box(modifier = Modifier.background(HandyTheme.colorScheme.background).padding(16.dp)) {
			QuizPokerTable(
				hero = Position.BTN,
				villain = null,
				hand = PreflopHand(Rank.QUEEN, Rank.QUEEN, HandShape.PAIR),
				raiseInfo = null,
			)
		}
	}
}

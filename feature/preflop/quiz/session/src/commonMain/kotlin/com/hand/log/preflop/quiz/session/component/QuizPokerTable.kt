package com.hand.log.preflop.quiz.session.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import com.hand.log.domain.model.Rank
import com.hand.log.domain.model.preflop.HandShape
import com.hand.log.domain.model.preflop.PreflopHand
import com.hand.log.domain.model.preflop.PreflopPositions
import com.hand.log.ui.poker.CardSize
import handylog.core.res.generated.resources.Res
import handylog.core.res.generated.resources.crown
import handylog.core.res.generated.resources.poker_chip
import org.jetbrains.compose.resources.painterResource
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

private val CHIP_SIZE = 18.dp
private const val CHIP_SEAT_GAP = 4f

/**
 * 프리플랍 스팟을 9-max 포커 테이블로 시각화한다. 좌석·히어로(크라운)·액션(레이저)·딜러·블라인드
 * 표현은 [com.hand.log.record.component.ActionTableView] 의 방식을 그대로 따른다.
 *
 * @param villainAction 빌런(레이저)이 취한 액션 라벨(레이즈/3벳/4벳). null 이면 앞에 레이저 없음(RFI).
 * @param villainColor 빌런 좌석/액션 강조색. null 이면 ActionTableView 와 동일한 accent.
 */
@Composable
internal fun QuizPokerTable(
	hero: Position,
	villain: Position?,
	villainAction: String?,
	hand: PreflopHand,
	heroAction: String? = null,
	villainColor: Color? = null,
	modifier: Modifier = Modifier,
) {
	val colors = HandyTheme.colorScheme
	val actionColor = villainColor ?: colors.accent
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
			val ringH = ringW * 0.72f
			val cx = ringW / 2
			val cy = ringH / 2
			val rx = ringW * 0.40f
			// 세로 반경을 안쪽으로 당겨 좌석의 크라운/액션 텍스트가 상하로 잘리지 않게 여백 확보.
			val ry = ringH * 0.36f
			val seat = 34.dp

			Box(modifier = Modifier.fillMaxWidth().height(ringH)) {
				Box(
					modifier = Modifier
						.align(Alignment.Center)
						.size(width = ringW * 0.66f, height = ringH * 0.62f)
						.clip(RoundedCornerShape(percent = 50))
						.background(colors.felt)
						.border(2.dp, colors.feltLight, RoundedCornerShape(percent = 50)),
				)

				HandCards(
					hand = hand,
					size = CardSize.XS,
					modifier = Modifier.align(Alignment.Center),
				)

				positions.forEachIndexed { i, pos ->
					val angle = (-90.0 + i * 360.0 / positions.size) * (PI / 180.0)
					val x = cx + rx * cos(angle).toFloat()
					val y = cy + ry * sin(angle).toFloat()

					val isHero = pos == hero
					val isVillain = pos == villain
					val action = when {
						isHero -> heroAction
						isVillain -> villainAction
						else -> null
					}
					val seatActionColor = if (isHero) colors.accent else actionColor
					val isFolded = !isHero && !isVillain && positions.indexOf(pos) < heroIndex
					// 액션 라벨은 좌석 바깥쪽(테이블 중앙 반대편)에 둔다 — 상단 좌석은 위, 하단 좌석은 아래.
					// 그래야 중앙 쪽으로 배치되는 베팅 칩과 겹치지 않는다.
					val labelAbove = sin(angle) < 0

					Seat(
						positionName = pos.label,
						isHero = isHero,
						isFolded = isFolded,
						actionLabel = action,
						actionColor = seatActionColor,
						labelAbove = labelAbove,
						size = seat,
						modifier = Modifier.offset(x = x - seat / 2, y = y - seat / 2),
					)

					// 칩을 좌석 중심에서 테이블 중앙 방향으로 (좌석 반지름 + 칩 반지름 + 여백)만큼 당겨,
					// 화면 크기·좌석 위치와 무관하게 좌석 원과 항상 일정 간격을 둔다.
					val dx = x - cx
					val dy = y - cy
					val dist = sqrt(dx.value * dx.value + dy.value * dy.value)
					val pull = seat.value / 2 + CHIP_SIZE.value / 2 + CHIP_SEAT_GAP
					val mx = x - (dx.value / dist * pull).dp
					val my = y - (dy.value / dist * pull).dp
					val markerMod = Modifier.offset(x = mx - CHIP_SIZE / 2, y = my - CHIP_SIZE / 2)
					when {
						isVillain && action != null -> BetChip(actionColor, markerMod)
						isHero && action != null -> BetChip(colors.accent, markerMod)
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
	isFolded: Boolean,
	actionLabel: String?,
	actionColor: Color,
	labelAbove: Boolean,
	size: Dp,
	modifier: Modifier = Modifier,
) {
	val colors = HandyTheme.colorScheme
	val hasAction = actionLabel != null
	val textAlpha = if (isFolded) 0.4f else 1f
	val borderColor = when {
		isFolded -> colors.border.copy(alpha = 0.3f)
		isHero -> colors.gold
		hasAction -> actionColor
		else -> colors.border
	}
	val bgColor = when {
		isFolded -> colors.muted.copy(alpha = 0.3f)
		isHero -> colors.gold.copy(alpha = 0.15f)
		else -> colors.muted
	}

	Column(
		modifier = modifier,
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
			ActionLabel(actionLabel, actionColor)
		}
		Box(
			modifier = Modifier
				.size(size)
				.clip(CircleShape)
				.background(bgColor)
				.border(
					width = if (isHero || hasAction) 2.dp else 1.dp,
					color = borderColor,
					shape = CircleShape,
				),
			contentAlignment = Alignment.Center,
		) {
			Text(
				text = positionName,
				style = HandyTheme.typography.bold8.nonScaledSp,
				color = if (isHero) colors.gold else colors.textPrimary.copy(alpha = textAlpha),
				maxLines = 1,
				softWrap = false,
			)
		}
		if (actionLabel != null && !labelAbove) {
			ActionLabel(actionLabel, actionColor)
		}
	}
}

@Composable
private fun ActionLabel(text: String, color: Color) {
	Text(
		text = text,
		style = HandyTheme.typography.bold8,
		color = color,
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

@ThemePreviews
@Composable
private fun QuizPokerTableFacingRfiPreview() {
	ThemePreview {
		Box(modifier = Modifier.background(HandyTheme.colorScheme.background).padding(16.dp)) {
			QuizPokerTable(
				hero = Position.BTN,
				villain = Position.CO,
				villainAction = "레이즈",
				hand = PreflopHand(Rank.ACE, Rank.KING, HandShape.SUITED),
			)
		}
	}
}

@ThemePreviews
@Composable
private fun QuizPokerTableVs3betPreview() {
	ThemePreview {
		Box(modifier = Modifier.background(HandyTheme.colorScheme.background).padding(16.dp)) {
			QuizPokerTable(
				hero = Position.CO,
				villain = Position.UTG,
				villainAction = "3벳",
				heroAction = "레이즈",
				hand = PreflopHand(Rank.ACE, Rank.ACE, HandShape.PAIR),
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
				hero = Position.CO,
				villain = null,
				villainAction = null,
				hand = PreflopHand(Rank.QUEEN, Rank.QUEEN, HandShape.PAIR),
			)
		}
	}
}

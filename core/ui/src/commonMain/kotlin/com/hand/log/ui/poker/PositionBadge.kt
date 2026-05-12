package com.hand.log.ui.poker

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import com.hand.log.designsystem.etc.ThemePreview
import com.hand.log.designsystem.etc.ThemePreviews
import com.hand.log.designsystem.theme.HandyTheme
import handylog.core.res.generated.resources.Res
import handylog.core.res.generated.resources.plus
import org.jetbrains.compose.resources.painterResource

/**
 * 포지션 원형 뱃지 + 마킹 버튼 오버레이.
 *
 * @param positionName 포지션 이름 (BTN, SB, BB 등)
 * @param isHero 히어로 여부 (금색 스타일)
 * @param showMarkButton 마킹 버튼 표시 여부
 * @param onMarkClick 마킹 버튼 클릭 콜백
 * @param circleSize 포지션 원 크기
 */
@Composable
fun PositionBadge(
	positionName: String,
	isHero: Boolean = false,
	showMarkButton: Boolean = false,
	onMarkClick: (() -> Unit)? = null,
	circleSize: Dp = 36.dp,
	modifier: Modifier = Modifier,
) {
	val colors = HandyTheme.colorScheme
	val canMark = showMarkButton && onMarkClick != null

	Box(
		modifier = modifier
			.then(if (canMark) Modifier.clickable(onClick = onMarkClick) else Modifier),
	) {
		val circleColor = if (isHero) colors.gold else colors.primary

		Box(
			modifier = Modifier
				.padding(4.dp)
				.size(circleSize)
				.align(Alignment.Center)
				.clip(CircleShape)
				.background(circleColor.copy(alpha = 0.15f))
				.then(
					if (canMark) {
						Modifier.dashedCircleBorder(circleColor)
					} else {
						Modifier
					},
				),
			contentAlignment = Alignment.Center,
		) {
			Text(
				text = positionName,
				style = HandyTheme.typography.bold10,
				color = circleColor,
				maxLines = 1,
			)
		}

		if (canMark) {
			Box(
				modifier = Modifier
					.size(14.dp)
					.align(Alignment.BottomEnd)
					.clip(CircleShape)
					.background(colors.primary),
				contentAlignment = Alignment.Center,
			) {
				Icon(
					painter = painterResource(Res.drawable.plus),
					contentDescription = null,
					modifier = Modifier.size(10.dp),
					tint = colors.onPrimary,
				)
			}
		}
	}
}

@ThemePreviews
@Composable
private fun PositionBadgePreview() {
	ThemePreview {
		Row(
			horizontalArrangement = Arrangement.spacedBy(12.dp),
			modifier = Modifier.padding(16.dp),
		) {
			PositionBadge(positionName = "BTN")
			PositionBadge(positionName = "BB", isHero = true)
			PositionBadge(positionName = "CO", showMarkButton = true, onMarkClick = {})
		}
	}
}

private fun Modifier.dashedCircleBorder(color: Color): Modifier = drawBehind {
	val strokeWidth = 1.dp.toPx()
	drawCircle(
		color = color,
		radius = size.minDimension / 2f - strokeWidth / 2f,
		style = Stroke(
			width = strokeWidth,
			pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 4f)),
		),
	)
}

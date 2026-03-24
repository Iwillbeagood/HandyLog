package com.hand.log.ui.poker

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.hand.log.designsystem.theme.HandyTheme
import com.hand.log.designsystem.theme.nonScaledSp
import handylog.core.res.generated.resources.Res
import handylog.core.res.generated.resources.crown
import org.jetbrains.compose.resources.painterResource

/**
 * 포커 테이블의 좌석 원형 컴포넌트.
 *
 * @param text 원 안에 표시할 텍스트 (포지션 이름, 좌석 번호 등)
 * @param isHero 히어로 여부 (왕관 아이콘 표시)
 * @param borderColor 테두리 색상
 * @param bgColor 배경 색상
 * @param textColor 텍스트 색상
 * @param borderWidth 테두리 두께
 * @param circleSize 원 크기
 * @param label 원 아래에 표시할 라벨 (액션, 스택 등)
 */
@Composable
fun PokerSeatCircle(
	text: String,
	isHero: Boolean = false,
	borderColor: Color = HandyTheme.colorScheme.border,
	bgColor: Color = HandyTheme.colorScheme.muted,
	textColor: Color = HandyTheme.colorScheme.textPrimary,
	borderWidth: Dp = 1.dp,
	circleSize: Dp = 32.dp,
	modifier: Modifier = Modifier,
	label: @Composable (() -> Unit)? = null,
) {
	val colors = HandyTheme.colorScheme

	Column(
		modifier = modifier,
		horizontalAlignment = Alignment.CenterHorizontally,
	) {
		Box(
			modifier = Modifier
				.requiredSize(circleSize)
				.clip(CircleShape)
				.background(bgColor)
				.border(borderWidth, borderColor, CircleShape),
			contentAlignment = Alignment.Center,
		) {
			if (isHero) {
				Icon(
					painter = painterResource(Res.drawable.crown),
					contentDescription = null,
					tint = colors.gold,
					modifier = Modifier.requiredSize(circleSize * 0.44f),
				)
			} else {
				Text(
					text = text,
					style = HandyTheme.typography.bold8.nonScaledSp,
					color = textColor,
					textAlign = TextAlign.Center,
					maxLines = 1,
					overflow = TextOverflow.Ellipsis,
				)
			}
		}
		label?.invoke()
	}
}

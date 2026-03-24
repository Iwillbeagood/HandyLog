package com.hand.log.ui.poker

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.hand.log.designsystem.theme.HandyTheme
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * 테이블 레이아웃 설정
 */
@Immutable
data class TableLayoutConfig(
	val containerAspectRatio: Float = 1.3f,
	val tableWidthFraction: Float = 0.75f,
	val tableAspectRatio: Float = 1.8f,
	val seatSize: Dp = 48.dp,
	val seatGap: Dp = 1.dp,
)

/**
 * 좌석의 계산된 위치 정보
 */
data class SeatPosition(
	val seat: Int,
	val angle: Double,
	val cosA: Float,
	val sinA: Float,
	val centerX: Float,
	val centerY: Float,
	val offsetX: Float,
	val offsetY: Float,
)

/**
 * 포커 테이블 타원 배경 + 좌석 배치를 처리하는 공통 레이아웃.
 *
 * @param playerCount 플레이어 수
 * @param config 레이아웃 설정
 * @param tableContent 테이블 중앙에 표시할 콘텐츠 (팟, 블라인드 등)
 * @param seatContent 각 좌석에 표시할 콘텐츠
 * @param extraContent 추가 콘텐츠 (딜러 버튼, 칩 등) - BoxWithConstraintsScope + SeatPositions 제공
 */
@Composable
fun PokerTableLayout(
	playerCount: Int,
	modifier: Modifier = Modifier,
	config: TableLayoutConfig = TableLayoutConfig(),
	tableContent: @Composable () -> Unit = {},
	seatContent: @Composable (SeatPosition) -> Unit,
	extraContent: @Composable BoxWithConstraintsScope.(TableLayoutInfo) -> Unit = {},
) {
	val colors = HandyTheme.colorScheme

	BoxWithConstraints(
		modifier = modifier.aspectRatio(config.containerAspectRatio),
	) {
		val density = LocalDensity.current
		val containerWidthPx = with(density) { maxWidth.toPx() }
		val containerHeightPx = with(density) { maxHeight.toPx() }

		val tableWidth = containerWidthPx * config.tableWidthFraction
		val tableHeight = tableWidth / config.tableAspectRatio
		val tableRadiusX = tableWidth / 2f
		val tableRadiusY = tableHeight / 2f
		val centerX = containerWidthPx / 2f
		val centerY = containerHeightPx / 2f
		val seatSizePx = with(density) { config.seatSize.toPx() }
		val gapPx = with(density) { config.seatGap.toPx() }
		val seatHalf = seatSizePx / 2f

		// 테이블 타원 배경
		Box(
			modifier = Modifier
				.fillMaxWidth(config.tableWidthFraction)
				.aspectRatio(config.tableAspectRatio)
				.align(Alignment.Center)
				.clip(RoundedCornerShape(40))
				.background(colors.felt)
				.border(2.dp, colors.feltLight, RoundedCornerShape(40)),
			contentAlignment = Alignment.Center,
		) {
			tableContent()
		}

		// 좌석 위치 계산
		val seatPositions = (1..playerCount).map { seat ->
			val angle = (2 * kotlin.math.PI * (seat - 1) / playerCount) - (kotlin.math.PI / 2)
			val cosA = cos(angle).toFloat()
			val sinA = sin(angle).toFloat()
			val seatCenterX = centerX + (tableRadiusX + gapPx + seatHalf) * cosA
			val seatCenterY = centerY + (tableRadiusY + gapPx + seatHalf) * sinA

			SeatPosition(
				seat = seat,
				angle = angle,
				cosA = cosA,
				sinA = sinA,
				centerX = seatCenterX,
				centerY = seatCenterY,
				offsetX = seatCenterX - seatHalf,
				offsetY = seatCenterY - seatHalf,
			)
		}

		val layoutInfo = TableLayoutInfo(
			centerX = centerX,
			centerY = centerY,
			tableRadiusX = tableRadiusX,
			tableRadiusY = tableRadiusY,
			seatPositions = seatPositions,
			density = density,
		)

		// 추가 콘텐츠 (딜러 버튼, 칩 등 — 좌석보다 먼저 그려서 아래에 위치)
		extraContent(layoutInfo)

		// 좌석 배치
		seatPositions.forEach { pos ->
			Box(
				modifier = Modifier
					.size(config.seatSize)
					.offset { IntOffset(pos.offsetX.roundToInt(), pos.offsetY.roundToInt()) },
			) {
				seatContent(pos)
			}
		}
	}
}

/**
 * 테이블 레이아웃 정보 (extraContent에서 사용)
 */
data class TableLayoutInfo(
	val centerX: Float,
	val centerY: Float,
	val tableRadiusX: Float,
	val tableRadiusY: Float,
	val seatPositions: List<SeatPosition>,
	val density: androidx.compose.ui.unit.Density,
)

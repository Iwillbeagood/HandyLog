package com.hand.log.ui.poker

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import handylog.core.res.generated.resources.Res
import handylog.core.res.generated.resources.allin_marker
import org.jetbrains.compose.resources.painterResource

private val AllInColor = Color(0xFFE84040)

/**
 * 올인 마커 - 빨간 삼각형 아이콘
 *
 * @param size 마커 크기
 * @param amount 올인 금액 (null이면 금액 미표시)
 */
@Composable
fun AllInMarker(
	modifier: Modifier = Modifier,
	size: Dp = 16.dp,
	amount: String? = null,
) {
	Column(
		modifier = modifier,
		horizontalAlignment = Alignment.CenterHorizontally,
	) {
		Icon(
			painter = painterResource(Res.drawable.allin_marker),
			contentDescription = "All-in",
			modifier = Modifier.size(size),
			tint = Color.Unspecified,
		)
		if (amount != null) {
			Text(
				text = "$amount",
				style = HandyTheme.typography.bold8.nonScaledSp,
				color = Color.White,
				modifier = Modifier
					.clip(RoundedCornerShape(4.dp))
					.background(AllInColor.copy(alpha = 0.8f))
					.padding(horizontal = 3.dp, vertical = 1.dp),
			)
		}
	}
}

@ThemePreviews
@Composable
private fun AllInMarkerPreview() {
	ThemePreview {
		Row(
			horizontalArrangement = Arrangement.spacedBy(16.dp),
			modifier = Modifier.padding(16.dp),
		) {
			AllInMarker()
			AllInMarker(amount = "50000")
			AllInMarker(size = 20.dp, amount = "12500")
		}
	}
}

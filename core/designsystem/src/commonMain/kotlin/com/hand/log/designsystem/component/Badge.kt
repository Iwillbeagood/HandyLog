package com.hand.log.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hand.log.designsystem.theme.HandLogTheme
import com.hand.log.designsystem.theme.HandyTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun Badge(
	text: String,
	color: Color,
	textColor: Color,
	pill: Boolean = false,
) {
	val shape = if (pill) RoundedCornerShape(50) else RoundedCornerShape(4.dp)
	val hPadding = if (pill) 8.dp else 6.dp
	val vPadding = if (pill) 3.dp else 2.dp

	Text(
		text = text,
		fontSize = 11.sp,
		fontWeight = FontWeight.Bold,
		color = textColor,
		modifier = Modifier
			.clip(shape)
			.background(color)
			.padding(horizontal = hPadding, vertical = vPadding),
	)
}

@Preview
@Composable
private fun BadgePreview() {
	HandLogTheme {
		val colors = HandyTheme.colorScheme
		Row(
			horizontalArrangement = Arrangement.spacedBy(6.dp),
			verticalAlignment = Alignment.CenterVertically,
		) {
			Badge(text = "9인", color = colors.secondary, textColor = colors.onSecondary)
			Badge(text = "Seat 5", color = colors.primary, textColor = colors.onPrimary)
			Badge(
				text = "캐시",
				color = colors.primary.copy(alpha = 0.15f),
				textColor = colors.primary,
				pill = true,
			)
			Badge(
				text = "토너먼트",
				color = colors.gold.copy(alpha = 0.15f),
				textColor = colors.gold,
				pill = true,
			)
		}
	}
}

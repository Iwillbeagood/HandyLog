package com.hand.log.designsystem.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.VerticalDivider
import com.hand.log.designsystem.theme.HandyTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun HmHorizontalDivider(
	modifier: Modifier = Modifier,
	lineColor: Color = HandyTheme.colorScheme.border,
	thickness: Dp = 1.dp,
) {
	HorizontalDivider(
		modifier = modifier,
		color = lineColor,
		thickness = thickness,
	)
}

@Composable
fun HmVerticalDivider(
	modifier: Modifier = Modifier,
	lineColor: Color = HandyTheme.colorScheme.border,
) {
	VerticalDivider(
		modifier = modifier,
		color = lineColor,
		thickness = 1.dp,
	)
}

@Preview
@Composable
private fun HmHorizontalDividerPreview() {
	HmHorizontalDivider(
		modifier = Modifier.fillMaxWidth(),
	)
}

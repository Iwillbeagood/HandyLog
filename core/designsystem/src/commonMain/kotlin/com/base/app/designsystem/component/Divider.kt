package com.hand.log.designsystem.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.hand.log.designsystem.etc.ThemePreviews

@Composable
fun HmHorizontalDivider(
	modifier: Modifier = Modifier,
	lineColor: Color = MaterialTheme.colorScheme.outlineVariant,
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
	lineColor: Color = MaterialTheme.colorScheme.outlineVariant,
) {
	VerticalDivider(
		modifier = modifier,
		color = lineColor,
		thickness = 1.dp,
	)
}

@ThemePreviews
@Composable
private fun HmHorizontalDividerPreview() {
	HmHorizontalDivider(
		modifier = Modifier.fillMaxWidth(),
	)
}

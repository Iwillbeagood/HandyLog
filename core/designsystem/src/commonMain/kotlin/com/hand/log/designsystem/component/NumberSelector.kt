package com.hand.log.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.hand.log.designsystem.theme.HandLogTheme
import com.hand.log.designsystem.theme.HandyTheme
import com.hand.log.designsystem.theme.nonScaledSp
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun HandyNumberSelector(
	range: IntRange,
	selected: Int,
	onSelect: (Int) -> Unit,
	modifier: Modifier = Modifier,
	selectedColor: Color = HandyTheme.colorScheme.primary,
	selectedContentColor: Color = HandyTheme.colorScheme.onPrimary,
) {
	val colors = HandyTheme.colorScheme
	val typography = HandyTheme.typography

	LazyRow(
		modifier = modifier,
		horizontalArrangement = Arrangement.spacedBy(4.dp),
	) {
		items(range.toList()) { number ->
			val isSelected = number == selected
			Box(
				modifier = Modifier
					.size(40.dp)
					.clip(RoundedCornerShape(8.dp))
					.background(if (isSelected) selectedColor else colors.muted)
					.border(
						width = 1.dp,
						color = if (isSelected) selectedColor else colors.inputBorder,
						shape = RoundedCornerShape(8.dp),
					)
					.clickable { onSelect(number) },
				contentAlignment = Alignment.Center,
			) {
				Text(
					text = "$number",
					style = typography.medium14.nonScaledSp,
					color = if (isSelected) selectedContentColor else colors.textPrimary,
				)
			}
		}
	}
}

@Preview
@Composable
private fun HandyNumberSelectorPreview() {
	HandLogTheme {
		HandyNumberSelector(
			range = 2..10,
			selected = 6,
			onSelect = {},
			modifier = Modifier.padding(16.dp),
		)
	}
}

@Preview
@Composable
private fun HandyNumberSelectorGoldPreview() {
	HandLogTheme {
		HandyNumberSelector(
			range = 1..9,
			selected = 3,
			onSelect = {},
			selectedColor = HandyTheme.colorScheme.gold,
			selectedContentColor = HandyTheme.colorScheme.card,
			modifier = Modifier.padding(16.dp),
		)
	}
}

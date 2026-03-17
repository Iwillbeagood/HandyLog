package com.hand.log.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.hand.log.designsystem.theme.HandyTheme
import com.hand.log.designsystem.etc.ThemePreview
import com.hand.log.designsystem.etc.ThemePreviews

@Composable
fun <T> HandyToggleGroup(
	options: List<T>,
	selected: T,
	onSelect: (T) -> Unit,
	label: (T) -> String,
	modifier: Modifier = Modifier,
) {
	val colors = HandyTheme.colorScheme
	val typography = HandyTheme.typography

	Row(
		modifier = modifier.fillMaxWidth(),
		horizontalArrangement = Arrangement.spacedBy(8.dp),
	) {
		options.forEach { option ->
			val isSelected = option == selected
			Box(
				modifier = Modifier
					.weight(1f)
					.clip(RoundedCornerShape(8.dp))
					.background(if (isSelected) colors.primary else colors.muted)
					.clickable { onSelect(option) }
					.padding(vertical = 12.dp),
				contentAlignment = Alignment.Center,
			) {
				Text(
					text = label(option),
					style = typography.medium14,
					color = if (isSelected) colors.onPrimary else colors.textSecondary,
				)
			}
		}
	}
}

@ThemePreviews
@Composable
private fun HandyToggleGroupPreview() {
	ThemePreview {
		HandyToggleGroup(
			options = listOf("토너먼트", "캐시"),
			selected = "캐시",
			onSelect = {},
			label = { it },
			modifier = Modifier.padding(16.dp),
		)
	}
}

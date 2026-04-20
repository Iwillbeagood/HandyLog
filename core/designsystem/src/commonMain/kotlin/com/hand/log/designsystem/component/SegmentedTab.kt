package com.hand.log.designsystem.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hand.log.designsystem.etc.ThemePreview
import com.hand.log.designsystem.etc.ThemePreviews
import com.hand.log.designsystem.theme.HandyTheme

@Composable
fun <T> HandySegmentedTab(
	options: List<T>,
	selected: T,
	onSelect: (T) -> Unit,
	label: @Composable (T) -> String,
	modifier: Modifier = Modifier,
) {
	val colors = HandyTheme.colorScheme

	SingleChoiceSegmentedButtonRow(
		modifier = modifier.fillMaxWidth(),
	) {
		options.forEachIndexed { index, option ->
			val isSelected = option == selected
			SegmentedButton(
				selected = isSelected,
				onClick = { onSelect(option) },
				shape = SegmentedButtonDefaults.itemShape(
					index = index,
					count = options.size,
					baseShape = RoundedCornerShape(8.dp),
				),
				colors = SegmentedButtonDefaults.colors(
					activeContainerColor = colors.secondary,
					activeContentColor = colors.textPrimary,
					inactiveContainerColor = colors.muted,
					inactiveContentColor = colors.textSecondary,
					activeBorderColor = colors.secondary,
					inactiveBorderColor = colors.muted,
				),
				icon = {},
			) {
				Text(
					text = label(option),
					style = HandyTheme.typography.medium14,
				)
			}
		}
	}
}

@ThemePreviews
@Composable
private fun HandySegmentedTabPreview() {
	ThemePreview {
		HandySegmentedTab(
			options = listOf("테이블", "핸드 기록"),
			selected = "테이블",
			onSelect = {},
			label = { it },
			modifier = Modifier.padding(16.dp),
		)
	}
}

package com.hand.log.preflop.quiz.session.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.hand.log.designsystem.etc.ThemePreview
import com.hand.log.designsystem.etc.ThemePreviews
import com.hand.log.designsystem.theme.HandyTheme
import com.hand.log.preflop.quiz.common.PreflopQuizQuestion

@Composable
internal fun ReviewSelector(
	questions: List<PreflopQuizQuestion>,
	selectedIndex: Int,
	onSelect: (Int) -> Unit,
	modifier: Modifier = Modifier,
) {
	Row(
		modifier = modifier
			.fillMaxWidth()
			.horizontalScroll(rememberScrollState())
			.padding(horizontal = 16.dp, vertical = 8.dp),
		horizontalArrangement = Arrangement.spacedBy(8.dp),
	) {
		questions.forEachIndexed { index, question ->
			ReviewSelectorChip(
				label = question.hand.notation,
				selected = index == selectedIndex,
				onClick = { onSelect(index) },
			)
		}
	}
}

@Composable
private fun ReviewSelectorChip(
	label: String,
	selected: Boolean,
	onClick: () -> Unit,
) {
	val colors = HandyTheme.colorScheme
	val shape = RoundedCornerShape(10.dp)
	Box(
		modifier = Modifier
			.clip(shape)
			.background(if (selected) colors.primary else colors.muted)
			.border(1.dp, if (selected) colors.primary else colors.border, shape)
			.clickable(onClick = onClick)
			.padding(horizontal = 14.dp, vertical = 8.dp),
	) {
		Text(
			text = label,
			style = HandyTheme.typography.bold14,
			color = if (selected) colors.onPrimary else colors.textSecondary,
		)
	}
}

@ThemePreviews
@Composable
private fun ReviewSelectorPreview() {
	ThemePreview {
		Box(modifier = Modifier.background(HandyTheme.colorScheme.background)) {
			ReviewSelector(
				questions = listOf(
					previewQuestion,
					previewFacingQuestion,
					previewVs3betQuestion,
					previewSbLimpQuestion,
				),
				selectedIndex = 1,
				onSelect = {},
			)
		}
	}
}

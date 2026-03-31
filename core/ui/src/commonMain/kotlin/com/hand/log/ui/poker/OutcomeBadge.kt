package com.hand.log.ui.poker

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.hand.log.designsystem.etc.ThemePreview
import com.hand.log.designsystem.etc.ThemePreviews
import com.hand.log.designsystem.theme.HandyTheme
import com.hand.log.domain.model.ShowdownOutcome

@Composable
fun OutcomeBadge(
	outcome: ShowdownOutcome,
	modifier: Modifier = Modifier,
) {
	val (text, color) = outcomeTextAndColor(outcome)

	Text(
		text = text,
		style = HandyTheme.typography.bold10,
		color = color,
		modifier = modifier
			.clip(RoundedCornerShape(4.dp))
			.background(color.copy(alpha = 0.15f))
			.padding(horizontal = 4.dp, vertical = 1.dp),
	)
}

@Composable
fun outcomeTextAndColor(outcome: ShowdownOutcome): Pair<String, Color> {
	val colors = HandyTheme.colorScheme
	return when (outcome) {
		ShowdownOutcome.WIN -> "WIN" to colors.gold
		ShowdownOutcome.SPLIT -> "SPLIT" to colors.split
		ShowdownOutcome.LOSE -> "LOSE" to colors.error
	}
}

@Composable
fun outcomeColor(outcome: ShowdownOutcome?): Color {
	val colors = HandyTheme.colorScheme
	return when (outcome) {
		ShowdownOutcome.WIN -> colors.gold
		ShowdownOutcome.SPLIT -> colors.split
		ShowdownOutcome.LOSE -> colors.error
		null -> colors.textSecondary
	}
}

@ThemePreviews
@Composable
private fun OutcomeBadgePreview() {
	ThemePreview {
		Row(
			horizontalArrangement = Arrangement.spacedBy(8.dp),
			modifier = Modifier.padding(16.dp),
		) {
			OutcomeBadge(outcome = ShowdownOutcome.WIN)
			OutcomeBadge(outcome = ShowdownOutcome.SPLIT)
			OutcomeBadge(outcome = ShowdownOutcome.LOSE)
		}
	}
}

package com.hand.log.preflop.quiz.session.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.hand.log.designsystem.etc.ThemePreview
import com.hand.log.designsystem.etc.ThemePreviews
import com.hand.log.designsystem.theme.HandyTheme
import com.hand.log.preflop.quiz.common.PreflopQuizQuestion
import com.hand.log.preflop.quiz.session.contract.ReviewOutcome

@Composable
internal fun ReviewSelector(
	questions: List<PreflopQuizQuestion>,
	outcomes: List<ReviewOutcome>,
	selectedIndex: Int,
	onSelect: (Int) -> Unit,
	modifier: Modifier = Modifier,
) {
	val listState = rememberLazyListState()
	LaunchedEffect(selectedIndex) {
		listState.animateScrollToItem(selectedIndex)
	}
	LazyRow(
		state = listState,
		modifier = modifier.fillMaxWidth(),
		contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
		horizontalArrangement = Arrangement.spacedBy(8.dp),
	) {
		itemsIndexed(questions) { index, question ->
			ReviewSelectorChip(
				label = question.hand.notation,
				outcome = outcomes.getOrElse(index) { ReviewOutcome.SKIPPED },
				selected = index == selectedIndex,
				onClick = { onSelect(index) },
			)
		}
	}
}

@Composable
private fun ReviewSelectorChip(
	label: String,
	outcome: ReviewOutcome,
	selected: Boolean,
	onClick: () -> Unit,
) {
	val colors = HandyTheme.colorScheme
	val shape = RoundedCornerShape(10.dp)
	val outcomeColor = when (outcome) {
		ReviewOutcome.CORRECT -> colors.primary
		ReviewOutcome.WRONG -> colors.error
		ReviewOutcome.SKIPPED -> colors.textSecondary
	}
	val background by animateColorAsState(if (selected) outcomeColor else colors.muted)
	val contentColor by animateColorAsState(if (selected) colors.onPrimary else outcomeColor)
	Box(
		modifier = Modifier
			.clip(shape)
			.background(background)
			.border(1.dp, outcomeColor, shape)
			.clickable(onClick = onClick)
			.padding(horizontal = 14.dp, vertical = 8.dp),
	) {
		Text(
			text = label,
			style = HandyTheme.typography.bold14,
			color = contentColor,
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
				outcomes = listOf(
					ReviewOutcome.CORRECT,
					ReviewOutcome.WRONG,
					ReviewOutcome.SKIPPED,
					ReviewOutcome.CORRECT,
				),
				selectedIndex = 1,
				onSelect = {},
			)
		}
	}
}

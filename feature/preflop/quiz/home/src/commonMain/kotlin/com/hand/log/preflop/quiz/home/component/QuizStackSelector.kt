package com.hand.log.preflop.quiz.home.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.hand.log.designsystem.etc.ThemePreview
import com.hand.log.designsystem.etc.ThemePreviews
import com.hand.log.designsystem.theme.HandyTheme
import com.hand.log.domain.model.preflop.PreflopStack
import handylog.core.res.generated.resources.Res
import handylog.core.res.generated.resources.quiz_stack_all
import handylog.core.res.generated.resources.quiz_stack_select
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun QuizStackSelector(
	options: List<PreflopStack?>,
	selected: PreflopStack?,
	onSelect: (PreflopStack?) -> Unit,
) {
	val colors = HandyTheme.colorScheme
	val allLabel = stringResource(Res.string.quiz_stack_all)

	Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
		Text(
			text = stringResource(Res.string.quiz_stack_select),
			style = HandyTheme.typography.bold12,
			color = colors.textSecondary,
		)
		FlowRow(
			horizontalArrangement = Arrangement.spacedBy(8.dp),
			verticalArrangement = Arrangement.spacedBy(8.dp),
		) {
			options.forEach { option ->
				val isSelected = option == selected
				Box(
					modifier = Modifier
						.clip(RoundedCornerShape(8.dp))
						.background(if (isSelected) colors.primary.copy(alpha = 0.15f) else colors.muted)
						.clickable { onSelect(option) }
						.padding(horizontal = 12.dp, vertical = 8.dp),
				) {
					Text(
						text = option?.label ?: allLabel,
						style = if (isSelected) HandyTheme.typography.bold12 else HandyTheme.typography.medium12,
						color = if (isSelected) colors.primary else colors.textSecondary,
					)
				}
			}
		}
	}
}

@ThemePreviews
@Composable
private fun QuizStackSelectorPreview() {
	ThemePreview {
		QuizStackSelector(
			options = listOf(null) + PreflopStack.entries,
			selected = PreflopStack.BB25,
			onSelect = {},
		)
	}
}

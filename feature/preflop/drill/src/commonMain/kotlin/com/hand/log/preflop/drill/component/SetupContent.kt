package com.hand.log.preflop.drill.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.hand.log.designsystem.component.RegularButton
import com.hand.log.designsystem.etc.ThemePreview
import com.hand.log.designsystem.etc.ThemePreviews
import com.hand.log.designsystem.theme.HandyTheme
import com.hand.log.domain.model.preflop.PreflopStack
import handylog.core.res.generated.resources.Res
import handylog.core.res.generated.resources.crown
import handylog.core.res.generated.resources.preflop_drill_setup_desc
import handylog.core.res.generated.resources.preflop_drill_setup_title
import handylog.core.res.generated.resources.preflop_drill_start
import handylog.core.res.generated.resources.quiz_stack_select
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun SetupContent(
	stackOptions: List<PreflopStack>,
	selectedStack: PreflopStack,
	lockedStacks: Set<PreflopStack>,
	isLoading: Boolean,
	onStackSelect: (PreflopStack) -> Unit,
	onStart: () -> Unit,
) {
	val colors = HandyTheme.colorScheme
	Column(modifier = Modifier.fillMaxSize()) {
		Column(
			modifier = Modifier
				.weight(1f)
				.fillMaxWidth()
				.verticalScroll(rememberScrollState())
				.padding(horizontal = 16.dp, vertical = 20.dp),
			verticalArrangement = Arrangement.spacedBy(24.dp),
		) {
			Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
				Text(
					text = stringResource(Res.string.preflop_drill_setup_title),
					style = HandyTheme.typography.bold20,
					color = colors.textPrimary,
				)
				Text(
					text = stringResource(Res.string.preflop_drill_setup_desc),
					style = HandyTheme.typography.regular14,
					color = colors.textSecondary,
				)
			}
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
					stackOptions.forEach { stack ->
						StackChip(
							stack = stack,
							selected = stack == selectedStack,
							locked = stack in lockedStacks,
							onClick = { onStackSelect(stack) },
						)
					}
				}
			}
		}
		RegularButton(
			onClick = onStart,
			text = stringResource(Res.string.preflop_drill_start),
			loading = isLoading,
			modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
		)
	}
}

@Composable
private fun StackChip(
	stack: PreflopStack,
	selected: Boolean,
	locked: Boolean,
	onClick: () -> Unit,
) {
	val colors = HandyTheme.colorScheme
	Row(
		modifier = Modifier
			.clip(RoundedCornerShape(8.dp))
			.background(if (selected) colors.primary.copy(alpha = 0.15f) else colors.muted)
			.clickable(onClick = onClick)
			.padding(horizontal = 12.dp, vertical = 8.dp),
		horizontalArrangement = Arrangement.spacedBy(4.dp),
		verticalAlignment = Alignment.CenterVertically,
	) {
		Text(
			text = stack.label,
			style = if (selected) HandyTheme.typography.bold12 else HandyTheme.typography.medium12,
			color = if (selected) colors.primary else colors.textSecondary,
		)
		if (locked) {
			Icon(
				painter = painterResource(Res.drawable.crown),
				contentDescription = null,
				tint = colors.gold,
				modifier = Modifier.size(12.dp),
			)
		}
	}
}

@ThemePreviews
@Composable
private fun SetupContentFreePreview() {
	ThemePreview {
		SetupContent(
			stackOptions = PreflopStack.entries,
			selectedStack = PreflopStack.FREE,
			lockedStacks = PreflopStack.entries.filterTo(mutableSetOf()) { it != PreflopStack.FREE },
			isLoading = false,
			onStackSelect = {},
			onStart = {},
		)
	}
}

@ThemePreviews
@Composable
private fun SetupContentProPreview() {
	ThemePreview {
		SetupContent(
			stackOptions = PreflopStack.entries,
			selectedStack = PreflopStack.BB40,
			lockedStacks = emptySet(),
			isLoading = false,
			onStackSelect = {},
			onStart = {},
		)
	}
}

@ThemePreviews
@Composable
private fun SetupContentLoadingPreview() {
	ThemePreview {
		SetupContent(
			stackOptions = PreflopStack.entries,
			selectedStack = PreflopStack.FREE,
			lockedStacks = emptySet(),
			isLoading = true,
			onStackSelect = {},
			onStart = {},
		)
	}
}

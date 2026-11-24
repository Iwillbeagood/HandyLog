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
import com.hand.log.designsystem.component.HandyToggleGroup
import com.hand.log.designsystem.component.RegularButton
import com.hand.log.designsystem.etc.ThemePreview
import com.hand.log.designsystem.etc.ThemePreviews
import com.hand.log.designsystem.theme.HandyTheme
import com.hand.log.domain.model.preflop.PreflopStack
import com.hand.log.preflop.drill.contract.DrillMode
import handylog.core.res.generated.resources.Res
import handylog.core.res.generated.resources.crown
import handylog.core.res.generated.resources.preflop_drill_mode_random
import handylog.core.res.generated.resources.preflop_drill_mode_select
import handylog.core.res.generated.resources.preflop_drill_mode_sequential
import handylog.core.res.generated.resources.preflop_drill_setup_title
import handylog.core.res.generated.resources.preflop_drill_start
import handylog.core.res.generated.resources.quiz_stack_select
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun SetupContent(
	stackOptions: List<PreflopStack>,
	selectedStack: PreflopStack,
	lockedStacks: Set<PreflopStack>,
	modeOptions: List<DrillMode>,
	selectedMode: DrillMode,
	isLoading: Boolean,
	onStackSelect: (PreflopStack) -> Unit,
	onModeSelect: (DrillMode) -> Unit,
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
			Text(
				text = stringResource(Res.string.preflop_drill_setup_title),
				style = HandyTheme.typography.bold20,
				color = colors.textPrimary,
			)
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
			Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
				Text(
					text = stringResource(Res.string.preflop_drill_mode_select),
					style = HandyTheme.typography.bold12,
					color = colors.textSecondary,
				)
				HandyToggleGroup(
					options = modeOptions,
					selected = selectedMode,
					onSelect = onModeSelect,
					label = { stringResource(it.labelRes()) },
				)
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

private fun DrillMode.labelRes(): StringResource = when (this) {
	DrillMode.RANDOM -> Res.string.preflop_drill_mode_random
	DrillMode.SEQUENTIAL -> Res.string.preflop_drill_mode_sequential
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
			modeOptions = DrillMode.entries,
			selectedMode = DrillMode.RANDOM,
			isLoading = false,
			onStackSelect = {},
			onModeSelect = {},
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
			modeOptions = DrillMode.entries,
			selectedMode = DrillMode.RANDOM,
			isLoading = false,
			onStackSelect = {},
			onModeSelect = {},
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
			modeOptions = DrillMode.entries,
			selectedMode = DrillMode.RANDOM,
			isLoading = true,
			onStackSelect = {},
			onModeSelect = {},
			onStart = {},
		)
	}
}

@ThemePreviews
@Composable
private fun SetupContentSequentialPreview() {
	ThemePreview {
		SetupContent(
			stackOptions = PreflopStack.entries,
			selectedStack = PreflopStack.BB100,
			lockedStacks = emptySet(),
			modeOptions = DrillMode.entries,
			selectedMode = DrillMode.SEQUENTIAL,
			isLoading = false,
			onStackSelect = {},
			onModeSelect = {},
			onStart = {},
		)
	}
}

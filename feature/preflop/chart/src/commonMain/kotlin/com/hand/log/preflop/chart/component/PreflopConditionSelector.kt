package com.hand.log.preflop.chart.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.hand.log.designsystem.etc.ThemePreview
import com.hand.log.designsystem.etc.ThemePreviews
import com.hand.log.designsystem.theme.HandyTheme
import com.hand.log.domain.model.preflop.PreflopScenario
import com.hand.log.domain.model.preflop.PreflopStack
import com.hand.log.preflop.chart.contract.PreflopChartState
import handylog.core.res.generated.resources.Res
import handylog.core.res.generated.resources.chevron_down
import handylog.core.res.generated.resources.crown
import handylog.core.res.generated.resources.preflop_scenario_facing
import handylog.core.res.generated.resources.preflop_scenario_label
import handylog.core.res.generated.resources.preflop_scenario_rfi
import handylog.core.res.generated.resources.preflop_scenario_vs3bet
import handylog.core.res.generated.resources.preflop_scenario_vslimp
import handylog.core.res.generated.resources.preflop_stack_label
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/** 스택·시나리오 드롭다운 한 줄. 차트 화면과 테이블 화면이 공유한다. */
@Composable
internal fun PreflopConditionSelector(
	state: PreflopChartState,
	onStackSelect: (PreflopStack) -> Unit,
	onScenarioSelect: (PreflopScenario) -> Unit,
	modifier: Modifier = Modifier,
	showScenario: Boolean = true,
) {
	Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
		DropdownField(
			label = stringResource(Res.string.preflop_stack_label),
			options = state.stackOptions,
			selected = state.stack,
			optionLabel = { it.label },
			onSelect = onStackSelect,
			lockedOptions = state.lockedStacks,
			modifier = Modifier.weight(1f),
		)
		if (showScenario) {
			val scenarioLabels = PreflopScenario.entries.associateWith { scenarioLabel(it) }
			DropdownField(
				label = stringResource(Res.string.preflop_scenario_label),
				options = state.scenarioOptions,
				selected = state.scenario,
				optionLabel = { scenarioLabels[it] ?: it.name },
				onSelect = onScenarioSelect,
				modifier = Modifier.weight(1f),
			)
		}
	}
}

@Composable
internal fun scenarioLabel(scenario: PreflopScenario): String = when (scenario) {
	PreflopScenario.RFI -> stringResource(Res.string.preflop_scenario_rfi)
	PreflopScenario.FACING_RFI -> stringResource(Res.string.preflop_scenario_facing)
	PreflopScenario.VS_3BET -> stringResource(Res.string.preflop_scenario_vs3bet)
	PreflopScenario.VS_LIMP -> stringResource(Res.string.preflop_scenario_vslimp)
}

@Composable
private fun <T> DropdownField(
	label: String,
	options: List<T>,
	selected: T,
	optionLabel: (T) -> String,
	onSelect: (T) -> Unit,
	modifier: Modifier = Modifier,
	lockedOptions: Set<T> = emptySet(),
) {
	val colors = HandyTheme.colorScheme
	var expanded by remember { mutableStateOf(false) }

	Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
		Text(
			text = label,
			style = HandyTheme.typography.medium12,
			color = colors.textSecondary,
		)
		Box {
			Row(
				modifier = Modifier
					.fillMaxWidth()
					.clip(RoundedCornerShape(8.dp))
					.background(colors.muted)
					.clickable { expanded = true }
					.padding(horizontal = 8.dp, vertical = 6.dp),
				verticalAlignment = Alignment.CenterVertically,
			) {
				Text(
					text = optionLabel(selected),
					style = HandyTheme.typography.bold12,
					color = colors.textPrimary,
					modifier = Modifier.weight(1f),
				)
				Icon(
					painter = painterResource(Res.drawable.chevron_down),
					contentDescription = null,
					modifier = Modifier.size(16.dp),
					tint = colors.textSecondary,
				)
			}
			DropdownMenu(
				expanded = expanded,
				onDismissRequest = { expanded = false },
				containerColor = colors.card,
				shape = RoundedCornerShape(8.dp),
			) {
				options.forEach { option ->
					val isSelected = option == selected
					val isLocked = option in lockedOptions
					Row(
						modifier = Modifier
							.fillMaxWidth()
							.clickable {
								expanded = false
								onSelect(option)
							}
							.padding(horizontal = 16.dp, vertical = 10.dp),
						horizontalArrangement = Arrangement.spacedBy(6.dp),
						verticalAlignment = Alignment.CenterVertically,
					) {
						Text(
							text = optionLabel(option),
							style = if (isSelected) HandyTheme.typography.bold12 else HandyTheme.typography.medium12,
							color = when {
								isLocked -> colors.textSecondary
								isSelected -> colors.primary
								else -> colors.textPrimary
							},
							modifier = Modifier.weight(1f),
						)
						if (isLocked) {
							Icon(
								painter = painterResource(Res.drawable.crown),
								contentDescription = null,
								modifier = Modifier.size(14.dp),
								tint = colors.gold,
							)
						}
					}
				}
			}
		}
	}
}

@ThemePreviews
@Composable
private fun PreflopConditionSelectorPreview() {
	ThemePreview {
		Box(modifier = Modifier.background(HandyTheme.colorScheme.background).padding(16.dp)) {
			PreflopConditionSelector(
				state = PreflopChartState(scenario = PreflopScenario.FACING_RFI, isLoading = false),
				onStackSelect = {},
				onScenarioSelect = {},
			)
		}
	}
}

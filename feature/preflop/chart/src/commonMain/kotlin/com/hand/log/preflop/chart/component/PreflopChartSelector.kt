package com.hand.log.preflop.chart.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.hand.log.designsystem.etc.ThemePreview
import com.hand.log.designsystem.etc.ThemePreviews
import com.hand.log.designsystem.theme.HandyTheme
import com.hand.log.domain.model.Position
import com.hand.log.domain.model.preflop.PreflopScenario
import com.hand.log.domain.model.preflop.PreflopStack
import com.hand.log.preflop.chart.contract.PreflopChartState
import handylog.core.res.generated.resources.Res
import handylog.core.res.generated.resources.chevron_down
import handylog.core.res.generated.resources.crown
import handylog.core.res.generated.resources.preflop_hero
import handylog.core.res.generated.resources.preflop_scenario_label
import handylog.core.res.generated.resources.preflop_scenario_facing
import handylog.core.res.generated.resources.preflop_scenario_rfi
import handylog.core.res.generated.resources.preflop_scenario_vs3bet
import handylog.core.res.generated.resources.preflop_scenario_vslimp
import handylog.core.res.generated.resources.preflop_stack_label
import handylog.core.res.generated.resources.preflop_villain_label
import handylog.core.res.generated.resources.preflop_vslimp_caption
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/**
 * 프리플랍 차트 화면의 조건 선택 영역.
 * 스택·시나리오는 드롭다운, 포지션(히어로·상대)은 칩으로 선택한다.
 */
@Composable
internal fun PreflopChartSelector(
	state: PreflopChartState,
	onStackSelect: (PreflopStack) -> Unit,
	onScenarioSelect: (PreflopScenario) -> Unit,
	onHeroSelect: (Position) -> Unit,
	onVillainSelect: (Position) -> Unit,
) {
	Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
		Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
			DropdownField(
				label = stringResource(Res.string.preflop_stack_label),
				options = state.stackOptions,
				selected = state.stack,
				optionLabel = { it.label },
				onSelect = onStackSelect,
				lockedOptions = state.lockedStacks,
				modifier = Modifier.weight(1f),
			)
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

		if (state.isFixedMatchup) {
			MatchupCaption(text = stringResource(Res.string.preflop_vslimp_caption))
			return@Column
		}

		ChipGroup(
			label = stringResource(Res.string.preflop_hero),
			options = state.heroOptions,
			selected = state.selectedHeroGroup,
			optionLabel = { group -> group.joinToString("/") { it.label } },
			selectedColor = HandyTheme.colorScheme.primary,
			onSelect = { group -> group.firstOrNull()?.let(onHeroSelect) },
		)

		if (state.showVillainChips) {
			ChipGroup(
				label = stringResource(Res.string.preflop_villain_label),
				options = state.villainOptions,
				selected = state.selectedVillainGroup,
				optionLabel = { group -> group.joinToString("/") { it.label } },
				selectedColor = HandyTheme.colorScheme.accent,
				onSelect = { group -> group.firstOrNull()?.let(onVillainSelect) },
			)
		}
	}
}

@Composable
private fun MatchupCaption(text: String) {
	val colors = HandyTheme.colorScheme
	Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
		Text(
			text = stringResource(Res.string.preflop_hero),
			style = HandyTheme.typography.medium12,
			color = colors.textSecondary,
		)
		Box(
			modifier = Modifier
				.clip(RoundedCornerShape(8.dp))
				.background(colors.accent.copy(alpha = 0.15f))
				.padding(horizontal = 12.dp, vertical = 8.dp),
		) {
			Text(
				text = text,
				style = HandyTheme.typography.bold12,
				color = colors.accent,
			)
		}
	}
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun <T> ChipGroup(
	label: String,
	options: List<T>,
	selected: T,
	optionLabel: (T) -> String,
	onSelect: (T) -> Unit,
	selectedColor: Color = HandyTheme.colorScheme.textPrimary,
) {
	val colors = HandyTheme.colorScheme

	Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
		Text(
			text = label,
			style = HandyTheme.typography.medium12,
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
						.background(if (isSelected) selectedColor.copy(alpha = 0.15f) else colors.muted)
						.clickable { onSelect(option) }
						.padding(horizontal = 8.dp, vertical = 6.dp),
				) {
					Text(
						text = optionLabel(option),
						style = if (isSelected) HandyTheme.typography.bold12 else HandyTheme.typography.medium12,
						color = if (isSelected) selectedColor else colors.textSecondary,
					)
				}
			}
		}
	}
}

@Composable
private fun scenarioLabel(scenario: PreflopScenario): String = when (scenario) {
	PreflopScenario.RFI -> stringResource(Res.string.preflop_scenario_rfi)
	PreflopScenario.FACING_RFI -> stringResource(Res.string.preflop_scenario_facing)
	PreflopScenario.VS_3BET -> stringResource(Res.string.preflop_scenario_vs3bet)
	PreflopScenario.VS_LIMP -> stringResource(Res.string.preflop_scenario_vslimp)
}

@ThemePreviews
@Composable
private fun PreflopChartSelectorPreview() {
	ThemePreview {
		PreflopChartSelector(
			state = PreflopChartState(
				scenario = PreflopScenario.FACING_RFI,
				hero = Position.BTN,
				villain = Position.UTG,
				villainOptions = listOf(
					listOf(Position.UTG, Position.UTG1),
					listOf(Position.UTG2),
					listOf(Position.LJ, Position.HJ),
				),
			),
			onStackSelect = {},
			onScenarioSelect = {},
			onHeroSelect = {},
			onVillainSelect = {},
		)
	}
}

@ThemePreviews
@Composable
private fun PreflopChartSelectorVsLimpPreview() {
	ThemePreview {
		PreflopChartSelector(
			state = PreflopChartState(
				scenario = PreflopScenario.VS_LIMP,
				hero = Position.BB,
				villain = Position.SB,
			),
			onStackSelect = {},
			onScenarioSelect = {},
			onHeroSelect = {},
			onVillainSelect = {},
		)
	}
}

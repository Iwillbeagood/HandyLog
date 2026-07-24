package com.hand.log.preflop.chart

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.hand.log.designsystem.component.BaseScaffold
import com.hand.log.designsystem.component.HandyTopAppbar
import com.hand.log.designsystem.component.TopAppbarType
import com.hand.log.designsystem.etc.ThemePreview
import com.hand.log.designsystem.etc.ThemePreviews
import com.hand.log.designsystem.theme.HandyTheme
import com.hand.log.domain.model.Position
import com.hand.log.domain.model.preflop.PreflopAction
import com.hand.log.domain.model.preflop.PreflopScenario
import com.hand.log.domain.model.preflop.PreflopStack
import com.hand.log.preflop.chart.component.PreflopChartGrid
import com.hand.log.preflop.chart.component.actionColors
import com.hand.log.preflop.chart.component.actionLabelRes
import com.hand.log.preflop.chart.contract.PreflopChartState
import handylog.core.res.generated.resources.Res
import handylog.core.res.generated.resources.arrow_down
import handylog.core.res.generated.resources.preflop_empty
import handylog.core.res.generated.resources.preflop_hero
import handylog.core.res.generated.resources.preflop_scenario_facing
import handylog.core.res.generated.resources.preflop_scenario_rfi
import handylog.core.res.generated.resources.preflop_scenario_vs3bet
import handylog.core.res.generated.resources.preflop_stack_label
import handylog.core.res.generated.resources.preflop_title
import handylog.core.res.generated.resources.preflop_villain_label
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun PreflopChartScreen(
	state: PreflopChartState,
	onBack: () -> Unit,
	onStackSelect: (PreflopStack) -> Unit,
	onScenarioSelect: (PreflopScenario) -> Unit,
	onHeroSelect: (Position) -> Unit,
	onVillainSelect: (Position) -> Unit,
) {
	BaseScaffold(
		topBar = {
			HandyTopAppbar(
				title = stringResource(Res.string.preflop_title),
				navigationType = TopAppbarType.Default,
				onBackEvent = onBack,
			)
		},
	) {
		Column(
			modifier = Modifier
				.fillMaxSize()
				.verticalScroll(rememberScrollState())
				.padding(horizontal = 16.dp, vertical = 16.dp),
			verticalArrangement = Arrangement.spacedBy(16.dp),
		) {
			SelectionChips(
				state = state,
				onStackSelect = onStackSelect,
				onHeroSelect = onHeroSelect,
				onVillainSelect = onVillainSelect,
			)

			ScenarioTabs(
				selected = state.scenario,
				onSelect = onScenarioSelect,
			)

			if (state.chart.presentActions.isNotEmpty()) {
				Legend(actions = state.chart.presentActions)
			}

			if (state.chart.isEmpty) {
				EmptyBanner()
			}

			PreflopChartGrid(chart = state.chart)
		}
	}
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SelectionChips(
	state: PreflopChartState,
	onStackSelect: (PreflopStack) -> Unit,
	onHeroSelect: (Position) -> Unit,
	onVillainSelect: (Position) -> Unit,
) {
	FlowRow(
		horizontalArrangement = Arrangement.spacedBy(8.dp),
		verticalArrangement = Arrangement.spacedBy(8.dp),
	) {
		SelectChip(
			label = stringResource(Res.string.preflop_stack_label),
			value = state.stack.label,
			valueColor = HandyTheme.colorScheme.textPrimary,
			options = state.stackOptions,
			optionLabel = { it.label },
			onSelect = onStackSelect,
		)
		SelectChip(
			label = stringResource(Res.string.preflop_hero),
			value = state.hero.label,
			valueColor = HandyTheme.colorScheme.primary,
			options = state.heroOptions,
			optionLabel = { it.label },
			onSelect = onHeroSelect,
		)
		val villain = state.villain
		if (villain != null && state.villainOptions.isNotEmpty()) {
			SelectChip(
				label = stringResource(Res.string.preflop_villain_label),
				value = villain.label,
				valueColor = HandyTheme.colorScheme.primary,
				options = state.villainOptions,
				optionLabel = { it.label },
				onSelect = onVillainSelect,
			)
		}
	}
}

@Composable
private fun <T> SelectChip(
	label: String,
	value: String,
	valueColor: Color,
	options: List<T>,
	optionLabel: (T) -> String,
	onSelect: (T) -> Unit,
) {
	val colors = HandyTheme.colorScheme
	var expanded by remember { mutableStateOf(false) }

	Box {
		Row(
			modifier = Modifier
				.clip(RoundedCornerShape(8.dp))
				.background(colors.muted)
				.clickable { expanded = true }
				.padding(horizontal = 12.dp, vertical = 8.dp),
			horizontalArrangement = Arrangement.spacedBy(6.dp),
			verticalAlignment = Alignment.CenterVertically,
		) {
			Text(
				text = label,
				style = HandyTheme.typography.regular12,
				color = colors.textSecondary,
			)
			Text(
				text = value,
				style = HandyTheme.typography.bold14,
				color = valueColor,
			)
			Icon(
				painter = painterResource(Res.drawable.arrow_down),
				contentDescription = null,
				modifier = Modifier.size(14.dp),
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
				DropdownMenuItem(
					text = {
						Text(
							text = optionLabel(option),
							style = HandyTheme.typography.medium14,
							color = colors.textPrimary,
						)
					},
					onClick = {
						expanded = false
						onSelect(option)
					},
				)
			}
		}
	}
}

@Composable
private fun ScenarioTabs(
	selected: PreflopScenario,
	onSelect: (PreflopScenario) -> Unit,
) {
	val colors = HandyTheme.colorScheme

	Row(modifier = Modifier.fillMaxWidth()) {
		PreflopScenario.entries.forEach { scenario ->
			val active = scenario == selected
			Column(
				modifier = Modifier
					.weight(1f)
					.clickable { onSelect(scenario) },
				horizontalAlignment = Alignment.CenterHorizontally,
			) {
				Text(
					text = stringResource(scenarioLabelRes(scenario)),
					style = HandyTheme.typography.medium14,
					color = if (active) colors.textPrimary else colors.textSecondary,
					modifier = Modifier.padding(vertical = 10.dp),
				)
				Box(
					modifier = Modifier
						.fillMaxWidth()
						.height(if (active) 2.dp else 1.dp)
						.background(if (active) colors.primary else colors.border),
				)
			}
		}
	}
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Legend(actions: List<PreflopAction>) {
	FlowRow(
		horizontalArrangement = Arrangement.spacedBy(16.dp),
		verticalArrangement = Arrangement.spacedBy(8.dp),
	) {
		actions.forEach { action ->
			LegendItem(action = action)
		}
	}
}

@Composable
private fun LegendItem(action: PreflopAction) {
	val (color, _) = actionColors(action)
	Row(
		horizontalArrangement = Arrangement.spacedBy(6.dp),
		verticalAlignment = Alignment.CenterVertically,
	) {
		Box(
			modifier = Modifier
				.size(14.dp)
				.clip(RoundedCornerShape(3.dp))
				.background(color),
		)
		Text(
			text = stringResource(actionLabelRes(action)),
			style = HandyTheme.typography.medium12,
			color = HandyTheme.colorScheme.textSecondary,
		)
	}
}

@Composable
private fun EmptyBanner() {
	Box(
		modifier = Modifier
			.fillMaxWidth()
			.clip(RoundedCornerShape(8.dp))
			.background(HandyTheme.colorScheme.muted)
			.padding(12.dp),
		contentAlignment = Alignment.Center,
	) {
		Text(
			text = stringResource(Res.string.preflop_empty),
			style = HandyTheme.typography.medium12,
			color = HandyTheme.colorScheme.textSecondary,
			textAlign = TextAlign.Center,
		)
	}
}

private fun scenarioLabelRes(scenario: PreflopScenario): StringResource = when (scenario) {
	PreflopScenario.RFI -> Res.string.preflop_scenario_rfi
	PreflopScenario.FACING_RFI -> Res.string.preflop_scenario_facing
	PreflopScenario.VS_3BET -> Res.string.preflop_scenario_vs3bet
}

@ThemePreviews
@Composable
private fun PreflopChartScreenPreview() {
	ThemePreview {
		PreflopChartScreen(
			state = PreflopChartState(),
			onBack = {},
			onStackSelect = {},
			onScenarioSelect = {},
			onHeroSelect = {},
			onVillainSelect = {},
		)
	}
}

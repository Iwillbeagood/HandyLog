package com.hand.log.preflop.chart

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hand.log.designsystem.component.BaseScaffold
import com.hand.log.designsystem.component.HandyHorizontalDivider
import com.hand.log.designsystem.component.HandyTopAppbar
import com.hand.log.designsystem.component.IconButton
import com.hand.log.designsystem.component.TopAppbarType
import com.hand.log.designsystem.etc.ThemePreview
import com.hand.log.designsystem.etc.ThemePreviews
import com.hand.log.designsystem.theme.HandyTheme
import com.hand.log.domain.model.Position
import com.hand.log.domain.model.preflop.PreflopScenario
import com.hand.log.domain.model.preflop.PreflopStack
import com.hand.log.preflop.chart.component.PreflopChartGrid
import com.hand.log.preflop.chart.component.PreflopChartLegend
import com.hand.log.preflop.chart.component.PreflopChartSelector
import com.hand.log.preflop.chart.component.PreflopStackSheet
import com.hand.log.preflop.chart.contract.PreflopChartState
import handylog.core.res.generated.resources.Res
import handylog.core.res.generated.resources.preflop_table_button
import handylog.core.res.generated.resources.preflop_title
import handylog.core.res.generated.resources.users
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun PreflopChartScreen(
	state: PreflopChartState,
	onBack: () -> Unit,
	onTableStackSelect: (PreflopStack) -> Unit,
	onStackSelect: (PreflopStack) -> Unit,
	onScenarioSelect: (PreflopScenario) -> Unit,
	onHeroSelect: (Position) -> Unit,
	onVillainSelect: (Position) -> Unit,
) {
	var showStackSheet by remember { mutableStateOf(false) }

	BaseScaffold(
		topBar = {
			HandyTopAppbar(
				title = stringResource(Res.string.preflop_title),
				navigationType = TopAppbarType.Close,
				onBackEvent = onBack,
				iconButton = IconButton(
					text = stringResource(Res.string.preflop_table_button),
					icon = Res.drawable.users,
					onClick = { showStackSheet = true },
				),
			)
		},
	) {
		if (showStackSheet) {
			PreflopStackSheet(
				stacks = state.stackOptions,
				lockedStacks = state.lockedStacks,
				onDismiss = { showStackSheet = false },
				onSelect = { stack ->
					showStackSheet = false
					onTableStackSelect(stack)
				},
			)
		}

		if (state.isLoading) {
			LoadingContent()
			return@BaseScaffold
		}

		Column(modifier = Modifier.fillMaxSize()) {
			Column(
				modifier = Modifier
					.weight(1f)
					.fillMaxWidth()
					.verticalScroll(rememberScrollState())
					.padding(horizontal = 16.dp, vertical = 16.dp),
				verticalArrangement = Arrangement.spacedBy(16.dp),
			) {
				PreflopChartSelector(
					state = state,
					onStackSelect = onStackSelect,
					onScenarioSelect = onScenarioSelect,
					onHeroSelect = onHeroSelect,
					onVillainSelect = onVillainSelect,
				)
			}

			HandyHorizontalDivider(modifier = Modifier.fillMaxWidth())

			Column(
				modifier = Modifier
					.fillMaxWidth()
					.background(HandyTheme.colorScheme.card)
					.padding(horizontal = 16.dp, vertical = 16.dp),
				verticalArrangement = Arrangement.spacedBy(12.dp),
			) {
				if (state.chart.presentActions.isNotEmpty()) {
					PreflopChartLegend(actions = state.chart.presentActions)
				}

				PreflopChartGrid(chart = state.chart)
			}
		}
	}
}

@Composable
private fun LoadingContent() {
	Box(
		modifier = Modifier.fillMaxSize(),
		contentAlignment = Alignment.Center,
	) {
		CircularProgressIndicator(color = HandyTheme.colorScheme.primary)
	}
}

@ThemePreviews
@Composable
private fun PreflopChartScreenPreview() {
	ThemePreview {
		PreflopChartScreen(
			state = PreflopChartState(),
			onBack = {},
			onTableStackSelect = {},
			onStackSelect = {},
			onScenarioSelect = {},
			onHeroSelect = {},
			onVillainSelect = {},
		)
	}
}

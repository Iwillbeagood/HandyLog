package com.hand.log.preflop.chart

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.hand.log.designsystem.component.BaseScaffold
import com.hand.log.designsystem.component.HandyHorizontalDivider
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
import com.hand.log.preflop.chart.component.PreflopChartSelector
import com.hand.log.preflop.chart.component.actionColors
import com.hand.log.preflop.chart.component.actionLabelRes
import com.hand.log.preflop.chart.contract.PreflopChartState
import handylog.core.res.generated.resources.Res
import handylog.core.res.generated.resources.preflop_title
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
				navigationType = TopAppbarType.Close,
				onBackEvent = onBack,
			)
		},
	) {
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
					Legend(actions = state.chart.presentActions)
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

package com.hand.log.preflop.chart

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hand.log.designsystem.component.BaseScaffold
import com.hand.log.designsystem.component.HandyHorizontalDivider
import com.hand.log.designsystem.component.HandyTopAppbar
import com.hand.log.designsystem.component.RegularButton
import com.hand.log.designsystem.component.TopAppbarType
import com.hand.log.designsystem.etc.ThemePreview
import com.hand.log.designsystem.etc.ThemePreviews
import com.hand.log.designsystem.theme.HandyTheme
import com.hand.log.domain.model.Position
import com.hand.log.domain.model.preflop.PreflopScenario
import com.hand.log.preflop.chart.component.PreflopChartGrid
import com.hand.log.preflop.chart.component.PreflopChartLegend
import com.hand.log.preflop.chart.component.PreflopSeatTable
import com.hand.log.preflop.chart.component.PreflopStackBadge
import com.hand.log.preflop.chart.component.scenarioLabel
import com.hand.log.preflop.chart.contract.PreflopChartState
import handylog.core.res.generated.resources.Res
import handylog.core.res.generated.resources.btn_next
import handylog.core.res.generated.resources.btn_prev
import handylog.core.res.generated.resources.preflop_situation_facing
import handylog.core.res.generated.resources.preflop_situation_rfi
import handylog.core.res.generated.resources.preflop_situation_vs3bet
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun PreflopChartTableScreen(
	state: PreflopChartState,
	onBack: () -> Unit,
	onSeatSelect: (Position) -> Unit,
	onPrev: () -> Unit,
	onNext: () -> Unit,
) {
	BaseScaffold(
		topBar = {
			HandyTopAppbar(
				title = situationText(state),
				navigationType = TopAppbarType.Close,
				onBackEvent = onBack,
				endContent = { PreflopStackBadge(stack = state.stack) },
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
				ChartSection(state = state)

				HandyHorizontalDivider(modifier = Modifier.fillMaxWidth())

				PreflopSeatTable(
					hero = state.hero,
					villain = state.villain,
					scenario = state.scenario,
					positions = state.tableSeatOrder,
					onSeatSelect = onSeatSelect,
				)
			}

			NavButtons(
				enabled = state.canRotateHero,
				onPrev = onPrev,
				onNext = onNext,
				modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
			)
		}
	}
}

@Composable
private fun ColumnScope.ChartSection(state: PreflopChartState) {
	if (state.chart.presentActions.isNotEmpty()) {
		PreflopChartLegend(actions = state.chart.presentActions)
	}

	PreflopChartGrid(chart = state.chart)
}

@Composable
private fun situationText(state: PreflopChartState): String {
	val hero = state.hero.label
	val villain = state.villain?.label
	return when {
		state.scenario == PreflopScenario.FACING_RFI && villain != null ->
			stringResource(Res.string.preflop_situation_facing, hero, villain)
		state.scenario == PreflopScenario.VS_3BET && villain != null ->
			stringResource(Res.string.preflop_situation_vs3bet, hero, villain)
		state.scenario == PreflopScenario.RFI ->
			stringResource(Res.string.preflop_situation_rfi, hero)
		else -> scenarioLabel(state.scenario)
	}
}

@Composable
private fun NavButtons(
	enabled: Boolean,
	onPrev: () -> Unit,
	onNext: () -> Unit,
	modifier: Modifier = Modifier,
) {
	Row(
		modifier = modifier,
		horizontalArrangement = Arrangement.spacedBy(12.dp),
	) {
		RegularButton(
			onClick = onPrev,
			text = stringResource(Res.string.btn_prev),
			outlined = true,
			enabled = enabled,
			modifier = Modifier.weight(1f),
		)
		RegularButton(
			onClick = onNext,
			text = stringResource(Res.string.btn_next),
			enabled = enabled,
			modifier = Modifier.weight(1f),
		)
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
private fun PreflopChartTableScreenPreview() {
	ThemePreview {
		PreflopChartTableScreen(
			state = PreflopChartState(
				scenario = PreflopScenario.FACING_RFI,
				hero = Position.BTN,
				villain = Position.CO,
				isLoading = false,
				openRaiserSeats = listOf(
					Position.UTG,
					Position.UTG1,
					Position.LJ,
					Position.HJ,
					Position.CO,
				),
				threeBettorSeats = listOf(Position.SB, Position.BB),
			),
			onBack = {},
			onSeatSelect = {},
			onPrev = {},
			onNext = {},
		)
	}
}

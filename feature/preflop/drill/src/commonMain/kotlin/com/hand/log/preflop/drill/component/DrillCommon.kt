package com.hand.log.preflop.drill.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.hand.log.designsystem.etc.ThemePreview
import com.hand.log.designsystem.etc.ThemePreviews
import com.hand.log.designsystem.theme.HandyTheme
import com.hand.log.domain.model.Position
import com.hand.log.domain.model.preflop.PreflopAction
import com.hand.log.domain.model.preflop.PreflopChart
import com.hand.log.domain.model.preflop.PreflopGrid
import com.hand.log.domain.model.preflop.PreflopStack
import com.hand.log.domain.model.preflop.gradeChartDrill
import com.hand.log.preflop.chart.component.PreflopStackBadge
import com.hand.log.preflop.drill.contract.DrillCell
import com.hand.log.preflop.drill.contract.DrillPhase
import com.hand.log.preflop.drill.contract.DrillSpot
import com.hand.log.preflop.drill.contract.DrillStrokePreview
import com.hand.log.preflop.drill.contract.PreflopChartDrillState
import handylog.core.res.generated.resources.Res
import handylog.core.res.generated.resources.crown
import org.jetbrains.compose.resources.painterResource

@Composable
internal fun DrillSpotHeader(
	spot: DrillSpot,
	description: String,
	onStackClick: () -> Unit,
) {
	Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
		Row(
			horizontalArrangement = Arrangement.spacedBy(8.dp),
			verticalAlignment = Alignment.CenterVertically,
		) {
			PreflopStackBadge(stack = spot.stack, onClick = onStackClick)
			HeroChip(position = spot.hero)
		}
		Text(
			text = description,
			style = HandyTheme.typography.regular12,
			color = HandyTheme.colorScheme.textSecondary,
		)
	}
}

@Composable
private fun HeroChip(position: Position) {
	val colors = HandyTheme.colorScheme
	val shape = RoundedCornerShape(10.dp)
	Row(
		modifier = Modifier
			.clip(shape)
			.background(colors.gold.copy(alpha = 0.15f))
			.border(1.dp, colors.gold, shape)
			.padding(horizontal = 12.dp, vertical = 6.dp),
		horizontalArrangement = Arrangement.spacedBy(4.dp),
		verticalAlignment = Alignment.CenterVertically,
	) {
		Icon(
			painter = painterResource(Res.drawable.crown),
			contentDescription = null,
			tint = colors.gold,
			modifier = Modifier.size(14.dp),
		)
		Text(
			text = position.label,
			style = HandyTheme.typography.bold14,
			color = colors.gold,
		)
	}
}

internal val previewAnswer = PreflopChart(
	mapOf(
		"AA" to PreflopAction.RAISE,
		"KK" to PreflopAction.RAISE,
		"AKs" to PreflopAction.RAISE,
		"AKo" to PreflopAction.RAISE,
		"KQs" to PreflopAction.RAISE,
		"A5s" to PreflopAction.RAISE_BLUFF,
		"A4s" to PreflopAction.RAISE_BLUFF,
	),
)

internal val previewSpot = DrillSpot(
	stack = PreflopStack.BB100,
	hero = Position.CO,
	answer = previewAnswer,
)

internal val previewPainted = mapOf(
	"AA" to PreflopAction.RAISE,
	"KK" to PreflopAction.RAISE,
	"AKs" to PreflopAction.RAISE,
	"A5s" to PreflopAction.RAISE,
	"72o" to PreflopAction.RAISE_BLUFF,
)

internal val previewStrokePreview = DrillStrokePreview(
	hand = PreflopGrid.rows[0][1],
	row = 0,
	col = 1,
	action = PreflopAction.RAISE,
)

internal val previewPaintedCells = previewPainted.mapValues { DrillCell(it.value) }

internal val previewResult = gradeChartDrill(previewAnswer.actions, previewPainted)

internal val previewGradedCells = PreflopChartDrillState(
	phase = DrillPhase.Graded(previewSpot, previewResult),
	painted = previewPainted,
).gridCells

@ThemePreviews
@Composable
private fun DrillSpotHeaderPreview() {
	ThemePreview {
		DrillSpotHeader(
			spot = previewSpot,
			description = "오픈 레인지를 기억해서 칠해보세요",
			onStackClick = {},
		)
	}
}

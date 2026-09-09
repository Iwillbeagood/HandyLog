package com.hand.log.preflop.chart.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.hand.log.designsystem.etc.ThemePreview
import com.hand.log.designsystem.etc.ThemePreviews
import com.hand.log.designsystem.theme.HandyTheme
import com.hand.log.domain.model.preflop.PreflopAction
import org.jetbrains.compose.resources.stringResource

/** 차트에 등장하는 액션의 색·라벨 범례. 차트 화면과 테이블 화면이 공유한다. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun PreflopChartLegend(actions: List<PreflopAction>) {
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
private fun PreflopChartLegendPreview() {
	ThemePreview {
		Box(modifier = Modifier.background(HandyTheme.colorScheme.background).padding(16.dp)) {
			PreflopChartLegend(
				actions = listOf(
					PreflopAction.RAISE,
					PreflopAction.CALL,
					PreflopAction.FOLD,
				),
			)
		}
	}
}

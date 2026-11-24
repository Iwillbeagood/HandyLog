package com.hand.log.preflop.drill.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.hand.log.preflop.chart.component.PreflopStackBadge
import com.hand.log.preflop.drill.contract.DrillPhase
import com.hand.log.preflop.drill.contract.DrillSummaryItem
import handylog.core.res.generated.resources.Res
import handylog.core.res.generated.resources.preflop_drill_average_accuracy
import handylog.core.res.generated.resources.preflop_drill_change_setup
import handylog.core.res.generated.resources.preflop_drill_restart
import handylog.core.res.generated.resources.preflop_drill_summary_title
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun SummaryContent(
	summary: DrillPhase.Summary,
	onChangeSetup: () -> Unit,
	onRestart: () -> Unit,
) {
	val colors = HandyTheme.colorScheme
	Column(modifier = Modifier.fillMaxSize()) {
		Column(
			modifier = Modifier
				.weight(1f)
				.fillMaxWidth()
				.verticalScroll(rememberScrollState())
				.padding(horizontal = 16.dp, vertical = 16.dp),
			verticalArrangement = Arrangement.spacedBy(16.dp),
		) {
			Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
				PreflopStackBadge(stack = summary.stack)
				Text(
					text = stringResource(Res.string.preflop_drill_summary_title),
					style = HandyTheme.typography.bold20,
					color = colors.textPrimary,
				)
			}
			Column(
				modifier = Modifier
					.fillMaxWidth()
					.clip(RoundedCornerShape(12.dp))
					.background(colors.card)
					.padding(16.dp),
				horizontalAlignment = Alignment.CenterHorizontally,
			) {
				Text(
					text = "${summary.averageAccuracyPct}%",
					style = HandyTheme.typography.bold32,
					color = colors.primary,
				)
				Text(
					text = stringResource(Res.string.preflop_drill_average_accuracy),
					style = HandyTheme.typography.regular12,
					color = colors.textSecondary,
				)
			}
			Column(
				modifier = Modifier
					.fillMaxWidth()
					.clip(RoundedCornerShape(12.dp))
					.background(colors.card)
					.padding(16.dp),
				verticalArrangement = Arrangement.spacedBy(12.dp),
			) {
				summary.items.forEach { SummaryRow(it) }
			}
		}
		Row(
			modifier = Modifier
				.fillMaxWidth()
				.padding(horizontal = 16.dp, vertical = 12.dp),
			horizontalArrangement = Arrangement.spacedBy(8.dp),
		) {
			RegularButton(
				onClick = onChangeSetup,
				text = stringResource(Res.string.preflop_drill_change_setup),
				textStyle = HandyTheme.typography.bold16,
				containerColor = colors.muted,
				contentColor = colors.textPrimary,
				modifier = Modifier.weight(1f),
			)
			RegularButton(
				onClick = onRestart,
				text = stringResource(Res.string.preflop_drill_restart),
				textStyle = HandyTheme.typography.bold16,
				modifier = Modifier.weight(1f),
			)
		}
	}
}

@Composable
private fun SummaryRow(item: DrillSummaryItem) {
	val colors = HandyTheme.colorScheme
	Row(
		horizontalArrangement = Arrangement.spacedBy(12.dp),
		verticalAlignment = Alignment.CenterVertically,
	) {
		Text(
			text = item.hero.label,
			style = HandyTheme.typography.bold14,
			color = colors.textPrimary,
			modifier = Modifier.widthIn(min = 52.dp),
		)
		Box(
			modifier = Modifier
				.weight(1f)
				.height(6.dp)
				.clip(RoundedCornerShape(3.dp))
				.background(colors.muted),
		) {
			Box(
				modifier = Modifier
					.fillMaxWidth(item.accuracyRatio)
					.height(6.dp)
					.clip(RoundedCornerShape(3.dp))
					.background(colors.primary),
			)
		}
		Text(
			text = "${item.accuracyPct}%",
			style = HandyTheme.typography.bold14,
			color = colors.textPrimary,
		)
	}
}

@ThemePreviews
@Composable
private fun SummaryContentPreview() {
	ThemePreview {
		SummaryContent(
			summary = DrillPhase.Summary(PreflopStack.BB100, previewSummaryItems),
			onChangeSetup = {},
			onRestart = {},
		)
	}
}

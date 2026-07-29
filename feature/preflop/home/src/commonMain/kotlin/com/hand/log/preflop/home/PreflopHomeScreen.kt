package com.hand.log.preflop.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.hand.log.preflop.home.component.PreflopCardVariant
import com.hand.log.preflop.home.component.PreflopFeatureCard
import handylog.core.res.generated.resources.Res
import handylog.core.res.generated.resources.preflop_badge_new
import handylog.core.res.generated.resources.preflop_chart_card_cta
import handylog.core.res.generated.resources.preflop_chart_card_desc
import handylog.core.res.generated.resources.preflop_main_title
import handylog.core.res.generated.resources.preflop_quiz_cta
import handylog.core.res.generated.resources.preflop_quiz_desc
import handylog.core.res.generated.resources.preflop_quiz_title
import handylog.core.res.generated.resources.preflop_recent_empty
import handylog.core.res.generated.resources.preflop_recent_title
import handylog.core.res.generated.resources.preflop_title
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun PreflopHomeScreen(
	onChartClick: () -> Unit,
	onQuizClick: () -> Unit,
) {
	BaseScaffold(
		applyNavigationBarsPadding = false,
		topBar = {
			HandyTopAppbar(
				title = stringResource(Res.string.preflop_main_title),
				navigationType = TopAppbarType.Main,
			)
			HandyHorizontalDivider()
		},
	) {
		Column(
			modifier = Modifier
				.fillMaxSize()
				.verticalScroll(rememberScrollState())
				.padding(horizontal = 16.dp, vertical = 20.dp),
			verticalArrangement = Arrangement.spacedBy(16.dp),
		) {
			PreflopFeatureCard(
				variant = PreflopCardVariant.CHART,
				title = stringResource(Res.string.preflop_title),
				description = stringResource(Res.string.preflop_chart_card_desc),
				ctaText = stringResource(Res.string.preflop_chart_card_cta),
				ctaColor = HandyTheme.colorScheme.primary,
				onClick = onChartClick,
			)
			PreflopFeatureCard(
				variant = PreflopCardVariant.QUIZ,
				title = stringResource(Res.string.preflop_quiz_title),
				description = stringResource(Res.string.preflop_quiz_desc),
				ctaText = stringResource(Res.string.preflop_quiz_cta),
				ctaColor = HandyTheme.colorScheme.gold,
				badge = stringResource(Res.string.preflop_badge_new),
				onClick = onQuizClick,
			)
			RecentQuizCard()
		}
	}
}

@Composable
private fun RecentQuizCard() {
	val colors = HandyTheme.colorScheme
	Column(
		modifier = Modifier
			.fillMaxWidth()
			.clip(RoundedCornerShape(12.dp))
			.background(colors.card)
			.padding(16.dp),
		verticalArrangement = Arrangement.spacedBy(12.dp),
	) {
		Text(
			text = stringResource(Res.string.preflop_recent_title),
			style = HandyTheme.typography.medium14,
			color = colors.textPrimary,
		)
		Text(
			text = stringResource(Res.string.preflop_recent_empty),
			style = HandyTheme.typography.regular12,
			color = colors.textSecondary,
		)
	}
}

@ThemePreviews
@Composable
private fun PreflopHomeScreenPreview() {
	ThemePreview {
		PreflopHomeScreen(
			onChartClick = {},
			onQuizClick = {},
		)
	}
}

package com.hand.log.preflop.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.hand.log.designsystem.component.BaseScaffold
import com.hand.log.designsystem.component.HandyHorizontalDivider
import com.hand.log.designsystem.component.HandyTopAppbar
import com.hand.log.designsystem.component.TopAppbarType
import com.hand.log.designsystem.etc.ThemePreview
import com.hand.log.designsystem.etc.ThemePreviews
import com.hand.log.designsystem.theme.HandyTheme
import handylog.core.res.generated.resources.Res
import handylog.core.res.generated.resources.chevron_right
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
import org.jetbrains.compose.resources.painterResource
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
			FeatureCard(
				variant = CardVariant.CHART,
				title = stringResource(Res.string.preflop_title),
				description = stringResource(Res.string.preflop_chart_card_desc),
				ctaText = stringResource(Res.string.preflop_chart_card_cta),
				ctaColor = HandyTheme.colorScheme.primary,
				onClick = onChartClick,
			)
			FeatureCard(
				variant = CardVariant.QUIZ,
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

private enum class CardVariant { CHART, QUIZ }

@Composable
private fun FeatureCard(
	variant: CardVariant,
	title: String,
	description: String,
	ctaText: String,
	ctaColor: Color,
	onClick: () -> Unit,
	badge: String? = null,
) {
	val colors = HandyTheme.colorScheme
	val tint = if (variant == CardVariant.CHART) colors.primary else colors.gold

	Box(
		modifier = Modifier
			.fillMaxWidth()
			.clip(RoundedCornerShape(16.dp))
			.background(colors.card)
			.background(Brush.linearGradient(listOf(tint.copy(alpha = 0.32f), Color.Transparent)))
			.clickable(onClick = onClick),
	) {
		Box(
			modifier = Modifier
				.align(Alignment.CenterEnd)
				.offset(x = 24.dp)
				.alpha(if (variant == CardVariant.CHART) 0.3f else 0.5f)
				.rotate(if (variant == CardVariant.CHART) -10f else -15f),
		) {
			if (variant == CardVariant.CHART) MiniGridDecoration() else QuizCardsDecoration()
		}

		Column(
			modifier = Modifier
				.align(Alignment.CenterStart)
				.padding(start = 20.dp, end = 116.dp, top = 16.dp, bottom = 16.dp),
			verticalArrangement = Arrangement.spacedBy(6.dp),
		) {
			if (badge != null) {
				Box(
					modifier = Modifier
						.clip(RoundedCornerShape(12.dp))
						.background(colors.gold)
						.padding(horizontal = 8.dp, vertical = 3.dp),
				) {
					Text(
						text = badge,
						style = HandyTheme.typography.bold10,
						color = colors.onPrimary,
					)
				}
			}
			Text(
				text = title,
				style = HandyTheme.typography.bold20,
				color = colors.textPrimary,
			)
			Text(
				text = description,
				style = HandyTheme.typography.regular12,
				color = colors.textSecondary,
			)
			Row(
				modifier = Modifier.padding(top = 4.dp),
				horizontalArrangement = Arrangement.spacedBy(4.dp),
				verticalAlignment = Alignment.CenterVertically,
			) {
				Text(
					text = ctaText,
					style = HandyTheme.typography.bold14,
					color = ctaColor,
				)
				Icon(
					painter = painterResource(Res.drawable.chevron_right),
					contentDescription = null,
					modifier = Modifier.size(14.dp),
					tint = ctaColor,
				)
			}
		}
	}
}

@Composable
private fun MiniGridDecoration() {
	val colors = HandyTheme.colorScheme
	val palette = listOf(colors.primary, colors.gold, colors.secondary, colors.muted)
	val pattern = listOf(
		0, 1, 2, 3,
		0, 1, 3, 0,
		2, 3, 3, 3,
		1, 0, 3, 3,
	)
	Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
		repeat(4) { row ->
			Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
				repeat(4) { col ->
					Box(
						modifier = Modifier
							.size(26.dp)
							.clip(RoundedCornerShape(4.dp))
							.background(palette[pattern[row * 4 + col]]),
					)
				}
			}
		}
	}
}

@Composable
private fun QuizCardsDecoration() {
	Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
		DecoCard("A", "♠", HandyTheme.colorScheme.suitBlack)
		DecoCard("K", "♥", HandyTheme.colorScheme.suitRed)
	}
}

@Composable
private fun DecoCard(rank: String, suit: String, color: Color) {
	Column(
		modifier = Modifier
			.size(width = 46.dp, height = 64.dp)
			.clip(RoundedCornerShape(6.dp))
			.background(Color.White),
		verticalArrangement = Arrangement.Center,
		horizontalAlignment = Alignment.CenterHorizontally,
	) {
		Text(text = rank, style = HandyTheme.typography.bold20, color = color)
		Text(text = suit, style = HandyTheme.typography.regular12, color = color)
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

package com.hand.log.preflop.home.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.hand.log.designsystem.etc.ThemePreview
import com.hand.log.designsystem.etc.ThemePreviews
import com.hand.log.designsystem.theme.HandyTheme
import com.hand.log.domain.model.Card
import com.hand.log.domain.model.Rank
import com.hand.log.domain.model.Suit
import com.hand.log.ui.poker.CardSize
import com.hand.log.ui.poker.PlayingCard
import handylog.core.res.generated.resources.Res
import handylog.core.res.generated.resources.chevron_right
import handylog.core.res.generated.resources.crown
import org.jetbrains.compose.resources.painterResource

internal enum class PreflopCardVariant { CHART, QUIZ }

@Composable
internal fun PreflopFeatureCard(
	variant: PreflopCardVariant,
	title: String,
	description: String,
	ctaText: String,
	ctaColor: Color,
	onClick: () -> Unit,
	modifier: Modifier = Modifier,
	locked: Boolean = false,
) {
	val colors = HandyTheme.colorScheme
	val tint = if (variant == PreflopCardVariant.CHART) colors.primary else colors.gold

	Box(modifier = modifier.fillMaxWidth()) {
		Box(
			modifier = Modifier
				.fillMaxWidth()
				.clip(RoundedCornerShape(16.dp))
				.background(colors.card)
				.background(Brush.linearGradient(listOf(tint.copy(alpha = 0.32f), Color.Transparent)))
				.clickable(onClick = onClick),
		) {
			Column(
				modifier = Modifier
					.align(Alignment.CenterStart)
					.padding(start = 20.dp, end = 116.dp, top = 16.dp, bottom = 16.dp),
				verticalArrangement = Arrangement.spacedBy(6.dp),
			) {
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

		// 데코는 clip 밖에 배치해 회전 시 모서리가 잘리지 않도록 한다.
		Box(
			modifier = Modifier
				.align(Alignment.CenterEnd)
				.offset(x = (-8).dp)
				.alpha(if (variant == PreflopCardVariant.CHART) 0.3f else 0.5f)
				.rotate(if (variant == PreflopCardVariant.CHART) -10f else -15f),
		) {
			if (variant == PreflopCardVariant.CHART) MiniGridDecoration() else QuizCardsDecoration()
		}

		if (locked) {
			Box(
				modifier = Modifier
					.align(Alignment.TopEnd)
					.padding(10.dp)
					.clip(RoundedCornerShape(8.dp))
					.background(colors.gold.copy(alpha = 0.18f))
					.padding(horizontal = 8.dp, vertical = 5.dp),
			) {
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

@Composable
private fun MiniGridDecoration() {
	val colors = HandyTheme.colorScheme
	val palette = listOf(colors.primary, colors.gold, colors.accent, colors.goldMuted)
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
							.size(18.dp)
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
		PlayingCard(card = Card(Rank.ACE, Suit.SPADES), size = CardSize.MD)
		PlayingCard(card = Card(Rank.KING, Suit.HEARTS), size = CardSize.MD)
	}
}

@ThemePreviews
@Composable
private fun PreflopFeatureCardChartPreview() {
	ThemePreview {
		Box(
			modifier = Modifier
				.background(HandyTheme.colorScheme.background)
				.padding(16.dp),
		) {
			PreflopFeatureCard(
				variant = PreflopCardVariant.CHART,
				title = "프리플랍 차트",
				description = "포지션별 오프닝 레인지와\n최적의 액션을 확인하세요",
				ctaText = "차트 보기",
				ctaColor = HandyTheme.colorScheme.primary,
				onClick = {},
			)
		}
	}
}

@ThemePreviews
@Composable
private fun PreflopFeatureCardQuizPreview() {
	ThemePreview {
		Box(
			modifier = Modifier
				.background(HandyTheme.colorScheme.background)
				.padding(16.dp),
		) {
			PreflopFeatureCard(
				variant = PreflopCardVariant.QUIZ,
				title = "프리플랍 퀴즈",
				description = "실력을 테스트해보세요",
				ctaText = "퀴즈 시작",
				ctaColor = HandyTheme.colorScheme.gold,
				onClick = {},
			)
		}
	}
}

@ThemePreviews
@Composable
private fun PreflopFeatureCardQuizLockedPreview() {
	ThemePreview {
		Box(
			modifier = Modifier
				.background(HandyTheme.colorScheme.background)
				.padding(16.dp),
		) {
			PreflopFeatureCard(
				variant = PreflopCardVariant.QUIZ,
				title = "프리플랍 퀴즈",
				description = "실력을 테스트해보세요",
				ctaText = "퀴즈 시작",
				ctaColor = HandyTheme.colorScheme.gold,
				onClick = {},
				locked = true,
			)
		}
	}
}

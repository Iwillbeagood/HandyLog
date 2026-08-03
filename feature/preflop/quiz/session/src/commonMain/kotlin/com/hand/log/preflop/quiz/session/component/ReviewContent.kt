package com.hand.log.preflop.quiz.session.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.hand.log.designsystem.component.FadeAnimatedContent
import com.hand.log.designsystem.component.HandyHorizontalDivider
import com.hand.log.designsystem.etc.ThemePreview
import com.hand.log.designsystem.etc.ThemePreviews
import com.hand.log.designsystem.theme.HandyTheme
import com.hand.log.domain.model.Position
import com.hand.log.domain.model.preflop.PreflopAction
import com.hand.log.domain.model.preflop.PreflopScenario
import com.hand.log.domain.model.preflop.PreflopStack
import com.hand.log.preflop.quiz.session.contract.PreflopQuizSessionState
import com.hand.log.preflop.quiz.session.contract.QuizPhase
import com.hand.log.preflop.quiz.session.contract.ReviewStatus
import handylog.core.res.generated.resources.Res
import handylog.core.res.generated.resources.grid_3x3
import handylog.core.res.generated.resources.quiz_progress
import handylog.core.res.generated.resources.quiz_review_correct_answer
import handylog.core.res.generated.resources.quiz_review_cta
import handylog.core.res.generated.resources.quiz_review_error
import handylog.core.res.generated.resources.quiz_review_exit
import handylog.core.res.generated.resources.quiz_review_loading
import handylog.core.res.generated.resources.quiz_review_next
import handylog.core.res.generated.resources.quiz_review_prev
import handylog.core.res.generated.resources.quiz_review_skipped
import handylog.core.res.generated.resources.quiz_review_title
import handylog.core.res.generated.resources.quiz_review_your_answer
import handylog.core.res.generated.resources.quiz_view_chart
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun ReviewContent(
	state: PreflopQuizSessionState,
	onReviewPrev: () -> Unit,
	onReviewNext: () -> Unit,
	onExitReview: () -> Unit,
	onRequestReview: () -> Unit,
	onViewChart: (PreflopStack, PreflopScenario, Position, Position?) -> Unit,
) {
	val colors = HandyTheme.colorScheme
	val question = state.reviewQuestion ?: return

	LaunchedEffect(state.reviewIndex) { onRequestReview() }

	val villainAction = villainActionLabel(question.scenario)

	Column(modifier = Modifier.fillMaxSize()) {
		Column(
			modifier = Modifier
				.weight(1f)
				.fillMaxWidth()
				.verticalScroll(rememberScrollState())
				.padding(16.dp),
			verticalArrangement = Arrangement.spacedBy(16.dp),
		) {
			Text(
				text = stringResource(Res.string.quiz_progress, state.reviewIndex + 1, state.reviewTotal),
				style = HandyTheme.typography.medium12,
				color = colors.textSecondary,
			)

			SpotChip(question.stack.label)

			QuizPokerTable(
				hero = question.hero,
				villain = question.villain,
				villainAction = villainAction,
				heroAction = heroActionLabel(question.scenario),
				hand = question.hand,
				villainColor = colors.error,
			)

			Column(
				modifier = Modifier
					.fillMaxWidth()
					.clip(RoundedCornerShape(12.dp))
					.background(colors.card)
					.padding(16.dp),
				verticalArrangement = Arrangement.spacedBy(8.dp),
			) {
				val userAnswer = state.reviewUserAnswer
				val userText = userAnswer?.let { answerReviewText(it) }
					?: stringResource(Res.string.quiz_review_skipped)
				Row(
					modifier = Modifier.fillMaxWidth(),
					horizontalArrangement = Arrangement.SpaceBetween,
				) {
					Text(
						text = stringResource(Res.string.quiz_review_your_answer),
						style = HandyTheme.typography.regular12,
						color = colors.textSecondary,
					)
					Text(
						text = userText,
						style = HandyTheme.typography.bold14,
						color = colors.error,
					)
				}
				Row(
					modifier = Modifier.fillMaxWidth(),
					horizontalArrangement = Arrangement.SpaceBetween,
				) {
					Text(
						text = stringResource(Res.string.quiz_review_correct_answer),
						style = HandyTheme.typography.regular12,
						color = colors.textSecondary,
					)
					Text(
						text = answerReviewText(question.correct),
						style = HandyTheme.typography.bold14,
						color = colors.primary,
					)
				}
			}

			FadeAnimatedContent(state.reviewStatus) { status ->
				ReviewSection(
					status = status,
					text = state.reviewText,
					onRequestReview = onRequestReview,
				)
			}

			ChartButton(
				onClick = {
					onViewChart(question.stack, question.scenario, question.hero, question.villain)
				},
			)
		}

		Column(modifier = Modifier.fillMaxWidth().background(colors.card)) {
			HandyHorizontalDivider()
			Row(
				modifier = Modifier
					.fillMaxWidth()
					.padding(horizontal = 16.dp, vertical = 8.dp),
				horizontalArrangement = Arrangement.spacedBy(8.dp),
			) {
				FadeAnimatedContent(state.reviewIndex > 0, modifier = Modifier.weight(1f)) { canPrev ->
					if (canPrev) {
						Box(
							modifier = Modifier
								.fillMaxWidth()
								.clip(RoundedCornerShape(10.dp))
								.background(colors.muted)
								.clickable(onClick = onReviewPrev)
								.padding(vertical = 10.dp),
							contentAlignment = Alignment.Center,
						) {
							Text(
								text = stringResource(Res.string.quiz_review_prev),
								style = HandyTheme.typography.bold14,
								color = colors.textPrimary,
							)
						}
					} else {
						Spacer(Modifier.fillMaxWidth())
					}
				}
				FadeAnimatedContent(
					state.reviewIndex < state.reviewTotal - 1,
					modifier = Modifier.weight(1f),
				) { hasNext ->
					Box(
						modifier = Modifier
							.fillMaxWidth()
							.clip(RoundedCornerShape(10.dp))
							.background(colors.primary)
							.clickable(onClick = if (hasNext) onReviewNext else onExitReview)
							.padding(vertical = 10.dp),
						contentAlignment = Alignment.Center,
					) {
						Text(
							text = stringResource(
								if (hasNext) Res.string.quiz_review_next else Res.string.quiz_review_exit,
							),
							style = HandyTheme.typography.bold14,
							color = colors.onPrimary,
						)
					}
				}
			}
		}
	}
}

@Composable
private fun ReviewSection(
	status: ReviewStatus,
	text: String,
	onRequestReview: () -> Unit,
) {
	val colors = HandyTheme.colorScheme
	when (status) {
		ReviewStatus.IDLE, ReviewStatus.LOADING -> Row(
			modifier = Modifier
				.fillMaxWidth()
				.clip(RoundedCornerShape(12.dp))
				.background(colors.card)
				.padding(16.dp),
			horizontalArrangement = Arrangement.spacedBy(10.dp),
			verticalAlignment = Alignment.CenterVertically,
		) {
			CircularProgressIndicator(
				modifier = Modifier.size(16.dp),
				color = colors.primary,
				strokeWidth = 2.dp,
			)
			Text(
				text = stringResource(Res.string.quiz_review_loading),
				style = HandyTheme.typography.medium14,
				color = colors.textSecondary,
			)
		}

		ReviewStatus.LOADED -> Column(
			modifier = Modifier
				.fillMaxWidth()
				.clip(RoundedCornerShape(12.dp))
				.background(colors.card)
				.padding(16.dp),
			verticalArrangement = Arrangement.spacedBy(8.dp),
		) {
			Text(
				text = stringResource(Res.string.quiz_review_title),
				style = HandyTheme.typography.bold14,
				color = colors.primary,
			)
			Text(
				text = text,
				style = HandyTheme.typography.regular14,
				color = colors.textPrimary,
			)
		}

		ReviewStatus.ERROR -> Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
			Text(
				text = stringResource(Res.string.quiz_review_error),
				style = HandyTheme.typography.medium12,
				color = colors.error,
				textAlign = TextAlign.Center,
				modifier = Modifier.fillMaxWidth(),
			)
			ReviewCtaButton(
				text = stringResource(Res.string.quiz_review_cta),
				onClick = onRequestReview,
			)
		}
	}
}

/** 리뷰 중인 스팟을 프리플랍 차트에서 여는 버튼. */
@Composable
private fun ChartButton(onClick: () -> Unit) {
	val colors = HandyTheme.colorScheme
	Row(
		modifier = Modifier
			.fillMaxWidth()
			.clip(RoundedCornerShape(10.dp))
			.border(1.dp, colors.primary, RoundedCornerShape(10.dp))
			.clickable(onClick = onClick)
			.padding(vertical = 12.dp),
		horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
		verticalAlignment = Alignment.CenterVertically,
	) {
		Icon(
			painter = painterResource(Res.drawable.grid_3x3),
			contentDescription = null,
			tint = colors.primary,
			modifier = Modifier.size(18.dp),
		)
		Text(
			text = stringResource(Res.string.quiz_view_chart),
			style = HandyTheme.typography.bold14,
			color = colors.primary,
		)
	}
}

@Composable
private fun ReviewCtaButton(text: String, onClick: () -> Unit) {
	val colors = HandyTheme.colorScheme
	Box(
		modifier = Modifier
			.fillMaxWidth()
			.clip(RoundedCornerShape(10.dp))
			.border(1.dp, colors.primary, RoundedCornerShape(10.dp))
			.clickable(onClick = onClick)
			.padding(vertical = 12.dp),
		contentAlignment = Alignment.Center,
	) {
		Text(
			text = text,
			style = HandyTheme.typography.bold14,
			color = colors.primary,
		)
	}
}

@ThemePreviews
@Composable
private fun ReviewContentPreview() {
	ThemePreview {
		Box(modifier = Modifier.background(HandyTheme.colorScheme.background)) {
			ReviewContent(
				state = PreflopQuizSessionState(
					phase = QuizPhase.REVIEW,
					questions = listOf(previewQuestion, previewQuestion),
					answers = listOf(PreflopAction.FOLD, null),
					reviewIndex = 0,
				),
				onReviewPrev = {},
				onReviewNext = {},
				onExitReview = {},
				onRequestReview = {},
				onViewChart = { _, _, _, _ -> },
			)
		}
	}
}

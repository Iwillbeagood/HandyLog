package com.hand.log.preflop.quiz.session

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.hand.log.designsystem.component.BaseScaffold
import com.hand.log.designsystem.component.HandyTopAppbar
import com.hand.log.designsystem.component.TopAppbarType
import com.hand.log.designsystem.etc.ThemePreview
import com.hand.log.designsystem.etc.ThemePreviews
import com.hand.log.designsystem.theme.HandyTheme
import com.hand.log.domain.model.Position
import com.hand.log.domain.model.Rank
import com.hand.log.domain.model.preflop.HandShape
import com.hand.log.domain.model.preflop.PreflopHand
import com.hand.log.domain.model.preflop.PreflopScenario
import com.hand.log.domain.model.preflop.PreflopStack
import com.hand.log.domain.model.preflop.chartChar
import com.hand.log.preflop.quiz.common.PreflopQuizQuestion
import com.hand.log.preflop.quiz.common.QuizAnswer
import com.hand.log.preflop.quiz.common.answerOptions
import com.hand.log.preflop.quiz.session.contract.PreflopQuizSessionState
import com.hand.log.preflop.quiz.session.contract.QuizPhase
import com.hand.log.preflop.quiz.session.contract.QuizResult
import com.hand.log.preflop.quiz.session.contract.ReviewStatus
import handylog.core.res.generated.resources.Res
import handylog.core.res.generated.resources.preflop_action_3bet
import handylog.core.res.generated.resources.preflop_action_4bet
import handylog.core.res.generated.resources.preflop_action_call
import handylog.core.res.generated.resources.preflop_action_fold
import handylog.core.res.generated.resources.preflop_action_raise
import handylog.core.res.generated.resources.preflop_quiz_title
import handylog.core.res.generated.resources.quiz_progress
import handylog.core.res.generated.resources.quiz_prompt
import handylog.core.res.generated.resources.quiz_result_accuracy
import handylog.core.res.generated.resources.quiz_result_avgtime
import handylog.core.res.generated.resources.quiz_result_avgtime_value
import handylog.core.res.generated.resources.quiz_result_desc
import handylog.core.res.generated.resources.quiz_result_fair
import handylog.core.res.generated.resources.quiz_result_good
import handylog.core.res.generated.resources.quiz_result_great
import handylog.core.res.generated.resources.quiz_result_streak
import handylog.core.res.generated.resources.quiz_result_streak_value
import handylog.core.res.generated.resources.quiz_retry
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
import handylog.core.res.generated.resources.quiz_skip
import handylog.core.res.generated.resources.quiz_start_review
import handylog.core.res.generated.resources.quiz_view_chart
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun PreflopQuizSessionScreen(
	state: PreflopQuizSessionState,
	onAnswer: (QuizAnswer) -> Unit,
	onSkip: () -> Unit,
	onRetry: () -> Unit,
	onBack: () -> Unit,
	onViewChart: () -> Unit,
	onStartReview: () -> Unit,
	onReviewPrev: () -> Unit,
	onReviewNext: () -> Unit,
	onExitReview: () -> Unit,
	onRequestReview: () -> Unit,
) {
	BaseScaffold(
		topBar = {
			HandyTopAppbar(
				title = stringResource(Res.string.preflop_quiz_title),
				navigationType = TopAppbarType.Default,
				onBackEvent = onBack,
			)
		},
	) {
		when (state.phase) {
			QuizPhase.LOADING -> LoadingContent()
			QuizPhase.PLAYING -> state.current?.let {
				QuestionContent(state = state, onAnswer = onAnswer, onSkip = onSkip)
			}
			QuizPhase.RESULT -> state.result?.let {
				ResultContent(
					result = it,
					reviewTotal = state.reviewTotal,
					onRetry = onRetry,
					onViewChart = onViewChart,
					onStartReview = onStartReview,
				)
			}
			QuizPhase.REVIEW -> ReviewContent(
				state = state,
				onReviewPrev = onReviewPrev,
				onReviewNext = onReviewNext,
				onExitReview = onExitReview,
				onRequestReview = onRequestReview,
			)
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

@Composable
private fun QuestionContent(
	state: PreflopQuizSessionState,
	onAnswer: (QuizAnswer) -> Unit,
	onSkip: () -> Unit,
) {
	val colors = HandyTheme.colorScheme
	val question = state.current ?: return

	Column(
		modifier = Modifier
			.fillMaxSize()
			.padding(16.dp),
		verticalArrangement = Arrangement.spacedBy(16.dp),
	) {
		Box(
			modifier = Modifier
				.fillMaxWidth()
				.height(4.dp)
				.clip(RoundedCornerShape(2.dp))
				.background(colors.muted),
		) {
			Box(
				modifier = Modifier
					.fillMaxWidth((state.index + 1).toFloat() / state.total)
					.height(4.dp)
					.clip(RoundedCornerShape(2.dp))
					.background(colors.primary),
			)
		}
		Text(
			text = stringResource(Res.string.quiz_progress, state.index + 1, state.total),
			style = HandyTheme.typography.medium12,
			color = colors.textSecondary,
		)

		Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
			SpotChip(question.stack.label)
			SpotChip(question.hero.label)
			question.villain?.let { SpotChip("vs ${it.label}") }
		}

		Box(
			modifier = Modifier
				.fillMaxWidth()
				.clip(RoundedCornerShape(16.dp))
				.background(colors.felt)
				.padding(vertical = 28.dp),
			contentAlignment = Alignment.Center,
		) {
			HandCards(question.hand)
		}

		Text(
			text = stringResource(Res.string.quiz_prompt),
			style = HandyTheme.typography.medium14,
			color = colors.textSecondary,
			textAlign = TextAlign.Center,
			modifier = Modifier.fillMaxWidth(),
		)

		Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
			question.options.forEach { answer ->
				AnswerButton(answer, question.scenario, onAnswer, Modifier.weight(1f))
			}
		}

		Text(
			text = stringResource(Res.string.quiz_skip),
			style = HandyTheme.typography.medium14,
			color = colors.textSecondary,
			textAlign = TextAlign.Center,
			modifier = Modifier
				.fillMaxWidth()
				.clickable(onClick = onSkip)
				.padding(8.dp),
		)
	}
}

@Composable
private fun AnswerButton(
	answer: QuizAnswer,
	scenario: PreflopScenario,
	onAnswer: (QuizAnswer) -> Unit,
	modifier: Modifier = Modifier,
) {
	val colors = HandyTheme.colorScheme
	Box(
		modifier = modifier
			.clip(RoundedCornerShape(12.dp))
			.background(colors.muted)
			.border(1.dp, colors.border, RoundedCornerShape(12.dp))
			.clickable { onAnswer(answer) }
			.padding(horizontal = 8.dp, vertical = 16.dp),
		contentAlignment = Alignment.Center,
	) {
		Text(
			text = stringResource(answerLabel(answer, scenario)),
			style = HandyTheme.typography.bold14,
			color = colors.textPrimary,
		)
	}
}

@Composable
private fun ReviewContent(
	state: PreflopQuizSessionState,
	onReviewPrev: () -> Unit,
	onReviewNext: () -> Unit,
	onExitReview: () -> Unit,
	onRequestReview: () -> Unit,
) {
	val colors = HandyTheme.colorScheme
	val question = state.reviewQuestion ?: return

	Column(
		modifier = Modifier
			.fillMaxSize()
			.verticalScroll(rememberScrollState())
			.padding(16.dp),
		verticalArrangement = Arrangement.spacedBy(16.dp),
	) {
		Text(
			text = stringResource(Res.string.quiz_progress, state.reviewIndex + 1, state.reviewTotal),
			style = HandyTheme.typography.medium12,
			color = colors.textSecondary,
		)

		Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
			SpotChip(question.stack.label)
			SpotChip(question.hero.label)
			question.villain?.let { SpotChip("vs ${it.label}") }
		}

		Box(
			modifier = Modifier
				.fillMaxWidth()
				.clip(RoundedCornerShape(16.dp))
				.background(colors.felt)
				.padding(vertical = 28.dp),
			contentAlignment = Alignment.Center,
		) {
			HandCards(question.hand)
		}

		Column(
			modifier = Modifier
				.fillMaxWidth()
				.clip(RoundedCornerShape(12.dp))
				.background(colors.card)
				.padding(16.dp),
			verticalArrangement = Arrangement.spacedBy(8.dp),
		) {
			val userAnswer = state.reviewUserAnswer
			val userText = userAnswer?.let { answerText(it, question.scenario) }
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
					text = answerText(question.correct, question.scenario),
					style = HandyTheme.typography.bold14,
					color = colors.primary,
				)
			}
		}

		ReviewSection(
			status = state.reviewStatus,
			text = state.reviewText,
			onRequestReview = onRequestReview,
		)

		Row(
			modifier = Modifier.fillMaxWidth(),
			horizontalArrangement = Arrangement.spacedBy(8.dp),
		) {
			if (state.reviewIndex > 0) {
				Box(
					modifier = Modifier
						.weight(1f)
						.clip(RoundedCornerShape(10.dp))
						.background(colors.muted)
						.clickable(onClick = onReviewPrev)
						.padding(vertical = 14.dp),
					contentAlignment = Alignment.Center,
				) {
					Text(
						text = stringResource(Res.string.quiz_review_prev),
						style = HandyTheme.typography.bold14,
						color = colors.textPrimary,
					)
				}
			} else {
				Spacer(Modifier.weight(1f))
			}
			if (state.reviewIndex < state.reviewTotal - 1) {
				Box(
					modifier = Modifier
						.weight(1f)
						.clip(RoundedCornerShape(10.dp))
						.background(colors.primary)
						.clickable(onClick = onReviewNext)
						.padding(vertical = 14.dp),
					contentAlignment = Alignment.Center,
				) {
					Text(
						text = stringResource(Res.string.quiz_review_next),
						style = HandyTheme.typography.bold14,
						color = colors.onPrimary,
					)
				}
			} else {
				Spacer(Modifier.weight(1f))
			}
		}

		Box(
			modifier = Modifier
				.fillMaxWidth()
				.clip(RoundedCornerShape(10.dp))
				.background(colors.muted)
				.clickable(onClick = onExitReview)
				.padding(vertical = 14.dp),
			contentAlignment = Alignment.Center,
		) {
			Text(
				text = stringResource(Res.string.quiz_review_exit),
				style = HandyTheme.typography.bold14,
				color = colors.textPrimary,
			)
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
		ReviewStatus.IDLE -> ReviewCtaButton(
			text = stringResource(Res.string.quiz_review_cta),
			onClick = onRequestReview,
		)

		ReviewStatus.LOADING -> Row(
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

@Composable
private fun HandCards(hand: PreflopHand) {
	val (first, second) = handCards(hand)
	Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
		CardView(first.first, first.second)
		CardView(second.first, second.second)
	}
}

@Composable
private fun CardView(rank: Rank, red: Boolean) {
	val colors = HandyTheme.colorScheme
	Column(
		modifier = Modifier
			.size(width = 52.dp, height = 72.dp)
			.clip(RoundedCornerShape(8.dp))
			.background(Color.White)
			.padding(6.dp),
	) {
		Text(
			text = rank.chartChar,
			style = HandyTheme.typography.bold20,
			color = if (red) colors.suitRed else colors.suitBlack,
		)
		Text(
			text = if (red) "♥" else "♠",
			style = HandyTheme.typography.bold16,
			color = if (red) colors.suitRed else colors.suitBlack,
		)
	}
}

@Composable
private fun SpotChip(text: String) {
	Box(
		modifier = Modifier
			.clip(RoundedCornerShape(14.dp))
			.background(HandyTheme.colorScheme.muted)
			.padding(horizontal = 12.dp, vertical = 6.dp),
	) {
		Text(
			text = text,
			style = HandyTheme.typography.bold12,
			color = HandyTheme.colorScheme.textPrimary,
		)
	}
}

@Composable
private fun ResultContent(
	result: QuizResult,
	reviewTotal: Int,
	onRetry: () -> Unit,
	onViewChart: () -> Unit,
	onStartReview: () -> Unit,
) {
	val colors = HandyTheme.colorScheme
	Column(
		modifier = Modifier
			.fillMaxSize()
			.padding(horizontal = 16.dp, vertical = 24.dp),
		verticalArrangement = Arrangement.spacedBy(20.dp),
		horizontalAlignment = Alignment.CenterHorizontally,
	) {
		ScoreCircle(result)
		val msg = when {
			result.accuracyPct >= 80 -> Res.string.quiz_result_great
			result.accuracyPct >= 50 -> Res.string.quiz_result_good
			else -> Res.string.quiz_result_fair
		}
		Text(
			text = stringResource(msg),
			style = HandyTheme.typography.bold16,
			color = colors.primary,
		)
		Text(
			text = stringResource(Res.string.quiz_result_desc),
			style = HandyTheme.typography.regular12,
			color = colors.textSecondary,
		)

		Column(
			modifier = Modifier
				.fillMaxWidth()
				.clip(RoundedCornerShape(12.dp))
				.background(colors.card),
		) {
			StatRow(
				stringResource(Res.string.quiz_result_accuracy),
				"${result.accuracyPct}%",
				colors.textPrimary,
			)
			StatRow(
				stringResource(Res.string.quiz_result_avgtime),
				stringResource(Res.string.quiz_result_avgtime_value, seconds(result.avgResponseMs)),
				colors.textPrimary,
			)
			StatRow(
				stringResource(Res.string.quiz_result_streak),
				stringResource(Res.string.quiz_result_streak_value, result.bestStreak),
				colors.gold,
			)
		}

		PrimaryButton(text = stringResource(Res.string.quiz_retry), onClick = onRetry)
		if (reviewTotal > 0) {
			Box(
				modifier = Modifier
					.fillMaxWidth()
					.clip(RoundedCornerShape(10.dp))
					.border(1.dp, colors.primary, RoundedCornerShape(10.dp))
					.clickable(onClick = onStartReview)
					.padding(vertical = 14.dp),
				contentAlignment = Alignment.Center,
			) {
				Text(
					text = stringResource(Res.string.quiz_start_review),
					style = HandyTheme.typography.bold14,
					color = colors.primary,
				)
			}
		}
		Box(
			modifier = Modifier
				.fillMaxWidth()
				.clip(RoundedCornerShape(10.dp))
				.background(colors.muted)
				.clickable(onClick = onViewChart)
				.padding(vertical = 14.dp),
			contentAlignment = Alignment.Center,
		) {
			Text(
				text = stringResource(Res.string.quiz_view_chart),
				style = HandyTheme.typography.bold14,
				color = colors.textPrimary,
			)
		}
	}
}

@Composable
private fun ScoreCircle(result: QuizResult) {
	val colors = HandyTheme.colorScheme
	val fraction = if (result.total == 0) 0f else result.score.toFloat() / result.total
	Box(
		modifier = Modifier.size(140.dp),
		contentAlignment = Alignment.Center,
	) {
		val track = colors.muted
		val progress = colors.primary
		Canvas(modifier = Modifier.size(140.dp)) {
			val stroke = 12f
			drawArc(
				color = track,
				startAngle = -90f,
				sweepAngle = 360f,
				useCenter = false,
				style = Stroke(width = stroke),
				size = Size(size.width - stroke, size.height - stroke),
				topLeft = androidx.compose.ui.geometry.Offset(stroke / 2, stroke / 2),
			)
			drawArc(
				color = progress,
				startAngle = -90f,
				sweepAngle = 360f * fraction,
				useCenter = false,
				style = Stroke(width = stroke),
				size = Size(size.width - stroke, size.height - stroke),
				topLeft = androidx.compose.ui.geometry.Offset(stroke / 2, stroke / 2),
			)
		}
		Row(verticalAlignment = Alignment.Bottom) {
			Text(
				text = "${result.score}",
				style = HandyTheme.typography.bold32,
				color = colors.textPrimary,
			)
			Text(
				text = " / ${result.total}",
				style = HandyTheme.typography.regular16,
				color = colors.textSecondary,
			)
		}
	}
}

@Composable
private fun StatRow(label: String, value: String, valueColor: Color) {
	Row(
		modifier = Modifier
			.fillMaxWidth()
			.padding(horizontal = 16.dp, vertical = 14.dp),
		horizontalArrangement = Arrangement.SpaceBetween,
	) {
		Text(label, style = HandyTheme.typography.regular12, color = HandyTheme.colorScheme.textSecondary)
		Text(value, style = HandyTheme.typography.bold14, color = valueColor)
	}
}

@Composable
private fun PrimaryButton(text: String, onClick: () -> Unit) {
	Box(
		modifier = Modifier
			.fillMaxWidth()
			.clip(RoundedCornerShape(10.dp))
			.background(HandyTheme.colorScheme.primary)
			.clickable(onClick = onClick)
			.padding(vertical = 14.dp),
		contentAlignment = Alignment.Center,
	) {
		Text(
			text = text,
			style = HandyTheme.typography.bold16,
			color = HandyTheme.colorScheme.onPrimary,
		)
	}
}

/** 공격 라벨은 시나리오에 따라 레이즈(RFI)·3벳(vs 오픈)·4벳(vs 3벳)으로 달라진다. */
private fun answerLabel(answer: QuizAnswer, scenario: PreflopScenario) = when (answer) {
	QuizAnswer.RAISE -> when (scenario) {
		PreflopScenario.RFI -> Res.string.preflop_action_raise
		PreflopScenario.FACING_RFI -> Res.string.preflop_action_3bet
		PreflopScenario.VS_3BET -> Res.string.preflop_action_4bet
	}
	QuizAnswer.CALL -> Res.string.preflop_action_call
	QuizAnswer.FOLD -> Res.string.preflop_action_fold
}

@Composable
private fun answerText(answer: QuizAnswer, scenario: PreflopScenario): String =
	stringResource(answerLabel(answer, scenario))

/** ms → "3.2" 형태의 초 문자열(소수 1자리). */
private fun seconds(ms: Long): String {
	val tenths = (ms + 50) / 100
	return "${tenths / 10}.${tenths % 10}"
}

private fun handCards(hand: PreflopHand): Pair<Pair<Rank, Boolean>, Pair<Rank, Boolean>> = when (hand.shape) {
	HandShape.PAIR -> (hand.high to false) to (hand.high to true)
	HandShape.SUITED -> (hand.high to false) to (hand.low to false)
	HandShape.OFFSUIT -> (hand.high to false) to (hand.low to true)
}

private val previewQuestion = PreflopQuizQuestion(
	stack = PreflopStack.BB100,
	scenario = PreflopScenario.RFI,
	hero = Position.CO,
	villain = null,
	hand = PreflopHand(Rank.ACE, Rank.KING, HandShape.SUITED),
	correct = QuizAnswer.RAISE,
	options = PreflopScenario.RFI.answerOptions(),
)

@ThemePreviews
@Composable
private fun PreflopQuizSessionPlayingPreview() {
	ThemePreview {
		PreflopQuizSessionScreen(
			state = PreflopQuizSessionState(
				phase = QuizPhase.PLAYING,
				questions = listOf(previewQuestion),
			),
			onAnswer = {},
			onSkip = {},
			onRetry = {},
			onBack = {},
			onViewChart = {},
			onStartReview = {},
			onReviewPrev = {},
			onReviewNext = {},
			onExitReview = {},
			onRequestReview = {},
		)
	}
}

@ThemePreviews
@Composable
private fun PreflopQuizSessionResultPreview() {
	ThemePreview {
		PreflopQuizSessionScreen(
			state = PreflopQuizSessionState(
				phase = QuizPhase.RESULT,
				questions = listOf(previewQuestion, previewQuestion),
				answers = listOf(QuizAnswer.FOLD, null),
				result = QuizResult(
					score = 8,
					total = 10,
					accuracyPct = 80,
					avgResponseMs = 3200,
					bestStreak = 5,
				),
			),
			onAnswer = {},
			onSkip = {},
			onRetry = {},
			onBack = {},
			onViewChart = {},
			onStartReview = {},
			onReviewPrev = {},
			onReviewNext = {},
			onExitReview = {},
			onRequestReview = {},
		)
	}
}

@ThemePreviews
@Composable
private fun PreflopQuizSessionReviewPreview() {
	ThemePreview {
		PreflopQuizSessionScreen(
			state = PreflopQuizSessionState(
				phase = QuizPhase.REVIEW,
				questions = listOf(previewQuestion, previewQuestion),
				answers = listOf(QuizAnswer.FOLD, null),
				reviewIndex = 0,
			),
			onAnswer = {},
			onSkip = {},
			onRetry = {},
			onBack = {},
			onViewChart = {},
			onStartReview = {},
			onReviewPrev = {},
			onReviewNext = {},
			onExitReview = {},
			onRequestReview = {},
		)
	}
}

package com.hand.log.preflop.quiz.session

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.hand.log.designsystem.component.BaseScaffold
import com.hand.log.designsystem.component.HandyTopAppbar
import com.hand.log.designsystem.component.TopAppbarType
import com.hand.log.designsystem.etc.ThemePreview
import com.hand.log.designsystem.etc.ThemePreviews
import com.hand.log.designsystem.theme.HandyTheme
import com.hand.log.domain.model.Position
import com.hand.log.domain.model.preflop.PreflopAction
import com.hand.log.preflop.quiz.common.PreflopQuizType
import com.hand.log.domain.model.preflop.PreflopScenario
import com.hand.log.domain.model.preflop.PreflopStack
import com.hand.log.preflop.quiz.session.component.QuestionContent
import com.hand.log.preflop.quiz.session.component.ResultContent
import com.hand.log.preflop.quiz.session.component.ReviewContent
import com.hand.log.preflop.quiz.session.component.previewQuestion
import com.hand.log.preflop.quiz.session.contract.PreflopQuizSessionState
import com.hand.log.preflop.quiz.session.contract.QuizPhase
import com.hand.log.preflop.quiz.session.contract.QuizResult
import handylog.core.res.generated.resources.Res
import handylog.core.res.generated.resources.preflop_quiz_title
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun PreflopQuizSessionScreen(
	state: PreflopQuizSessionState,
	onPrimarySelect: (PreflopAction) -> Unit,
	onPlanSelect: (PreflopAction) -> Unit,
	onSkip: () -> Unit,
	onPrevious: () -> Unit,
	onRetry: () -> Unit,
	onBack: () -> Unit,
	onViewChart: (PreflopStack, PreflopScenario, Position, Position?) -> Unit,
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
				navigationType = TopAppbarType.Close,
				onBackEvent = onBack,
			)
		},
	) {
		when (state.phase) {
			QuizPhase.LOADING -> LoadingContent()
			QuizPhase.PLAYING -> state.current?.let {
				QuestionContent(
					state = state,
					onPrimarySelect = onPrimarySelect,
					onPlanSelect = onPlanSelect,
					onSkip = onSkip,
					onPrevious = onPrevious,
				)
			}
			QuizPhase.RESULT -> state.result?.let {
				ResultContent(
					result = it,
					reviewTotal = state.reviewTotal,
					onRetry = onRetry,
					onStartReview = onStartReview,
				)
			}
			QuizPhase.REVIEW -> ReviewContent(
				state = state,
				onReviewPrev = onReviewPrev,
				onReviewNext = onReviewNext,
				onExitReview = onExitReview,
				onRequestReview = onRequestReview,
				onViewChart = onViewChart,
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

@ThemePreviews
@Composable
private fun PreflopQuizSessionPlayingPreview() {
	ThemePreview {
		PreflopQuizSessionScreen(
			state = PreflopQuizSessionState(
				phase = QuizPhase.PLAYING,
				questions = listOf(previewQuestion),
			),
			onPrimarySelect = {},
			onPlanSelect = {},
			onSkip = {},
			onPrevious = {},
			onRetry = {},
			onBack = {},
			onViewChart = { _, _, _, _ -> },
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
				answers = listOf(PreflopAction.FOLD, null),
				result = QuizResult(
					score = 8,
					total = 10,
					accuracyPct = 80,
					avgResponseMs = 3200,
					bestStreak = 5,
					quizType = PreflopQuizType.RFI,
					playedAt = 1_753_800_000_000L,
				),
			),
			onPrimarySelect = {},
			onPlanSelect = {},
			onSkip = {},
			onPrevious = {},
			onRetry = {},
			onBack = {},
			onViewChart = { _, _, _, _ -> },
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
				answers = listOf(PreflopAction.FOLD, null),
				reviewIndex = 0,
			),
			onPrimarySelect = {},
			onPlanSelect = {},
			onSkip = {},
			onPrevious = {},
			onRetry = {},
			onBack = {},
			onViewChart = { _, _, _, _ -> },
			onStartReview = {},
			onReviewPrev = {},
			onReviewNext = {},
			onExitReview = {},
			onRequestReview = {},
		)
	}
}

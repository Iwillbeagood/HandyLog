package com.hand.log.preflop.quiz.session

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.text.intl.Locale
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hand.log.navigation.interop.LocalNavigateActionInterop

@Composable
internal fun PreflopQuizSessionRoute(
	viewModel: PreflopQuizSessionViewModel,
) {
	val state by viewModel.state.collectAsStateWithLifecycle()
	val navAction = LocalNavigateActionInterop.current
	val languageName = languageNameFor(Locale.current.language)

	PreflopQuizSessionScreen(
		state = state,
		onAnswer = viewModel::onAnswer,
		onSkip = viewModel::onSkip,
		onRetry = viewModel::onRetry,
		onBack = navAction::popBackStack,
		onViewChart = navAction::navigateToPreflopChart,
		onStartReview = viewModel::onStartReview,
		onReviewPrev = viewModel::onReviewPrev,
		onReviewNext = viewModel::onReviewNext,
		onExitReview = viewModel::onExitReview,
		onRequestReview = { viewModel.onRequestReview(languageName) },
	)
}

/** LLM 응답 언어 지정용 이름. 지원 로케일(ko/en/ja) 외에는 영어로 응답한다. */
private fun languageNameFor(language: String): String = when (language) {
	"ko" -> "한국어"
	"ja" -> "日本語"
	else -> "English"
}

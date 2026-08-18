package com.hand.log.preflop.quiz.session

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.text.intl.Locale
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hand.log.designsystem.component.modal.DefaultDialog
import com.hand.log.navigation.interop.LocalNavigateActionInterop
import com.hand.log.preflop.quiz.session.contract.PreflopQuizSessionModalEffect
import com.hand.log.preflop.quiz.session.contract.QuizPhase
import handylog.core.res.generated.resources.Res
import handylog.core.res.generated.resources.quiz_exit_cancel
import handylog.core.res.generated.resources.quiz_exit_confirm
import handylog.core.res.generated.resources.quiz_exit_desc
import handylog.core.res.generated.resources.quiz_exit_title
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun PreflopQuizSessionRoute(
	viewModel: PreflopQuizSessionViewModel,
) {
	val state by viewModel.state.collectAsStateWithLifecycle()
	val modalEffect by viewModel.modalEffect.collectAsStateWithLifecycle()
	val navAction = LocalNavigateActionInterop.current
	val languageName = languageNameFor(Locale.current.language)

	PreflopQuizSessionScreen(
		state = state,
		onPrimarySelect = viewModel::onPrimarySelect,
		onPlanSelect = viewModel::onPlanSelect,
		onSkip = viewModel::onSkip,
		onPrevious = viewModel::onPrevious,
		onRetry = viewModel::onRetry,
		onBack = {
			if (state.phase == QuizPhase.PLAYING) viewModel.onCloseRequest() else navAction.popBackStack()
		},
		onViewChart = { stack, scenario, hero, villain ->
			navAction.navigateToPreflopChart(stack.name, scenario.name, hero.name, villain?.name)
		},
		onStartReview = viewModel::onStartReview,
		onReviewPrev = viewModel::onReviewPrev,
		onReviewNext = viewModel::onReviewNext,
		onSelectReview = viewModel::onSelectReview,
		onExitReview = viewModel::onExitReview,
		onRequestReview = { viewModel.onRequestReview(languageName) },
	)

	when (modalEffect) {
		PreflopQuizSessionModalEffect.Idle -> Unit
		PreflopQuizSessionModalEffect.ConfirmExit -> DefaultDialog(
			title = stringResource(Res.string.quiz_exit_title),
			content = stringResource(Res.string.quiz_exit_desc),
			confirmText = stringResource(Res.string.quiz_exit_confirm),
			dismissText = stringResource(Res.string.quiz_exit_cancel),
			onDismissRequest = viewModel::dismissModal,
			onConfirmClick = navAction::popBackStack,
		)
	}
}

/** LLM 응답 언어 지정용 이름. 지원 로케일(ko/en/ja) 외에는 영어로 응답한다. */
private fun languageNameFor(language: String): String = when (language) {
	"ko" -> "한국어"
	"ja" -> "日本語"
	else -> "English"
}

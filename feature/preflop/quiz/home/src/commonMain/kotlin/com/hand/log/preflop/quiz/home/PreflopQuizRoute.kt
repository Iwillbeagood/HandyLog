package com.hand.log.preflop.quiz.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hand.log.navigation.interop.LocalNavigateActionInterop
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun PreflopQuizRoute(
	viewModel: PreflopQuizViewModel = koinViewModel(),
) {
	val navAction = LocalNavigateActionInterop.current
	val state by viewModel.state.collectAsStateWithLifecycle()

	PreflopQuizScreen(
		state = state,
		onBack = navAction::popBackStack,
		onStackSelect = viewModel::onStackSelect,
		onTypeSelect = { type ->
			navAction.navigateToPreflopQuizSession(type.name, state.selectedStack?.name ?: "")
		},
		onRecordClick = { record ->
			navAction.navigateToPreflopQuizSession(record.quizType, recordId = record.id)
		},
	)
}

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
	val records by viewModel.recentRecords.collectAsStateWithLifecycle()

	PreflopQuizScreen(
		records = records,
		onBack = navAction::popBackStack,
		onTypeSelect = { type -> navAction.navigateToPreflopQuizSession(type.name) },
	)
}

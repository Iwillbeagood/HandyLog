package com.hand.log.preflop.home

import androidx.compose.runtime.Composable
import com.hand.log.navigation.interop.LocalNavigateActionInterop

@Composable
internal fun PreflopHomeRoute() {
	val navAction = LocalNavigateActionInterop.current

	PreflopHomeScreen(
		onChartClick = navAction::navigateToPreflopChart,
		onQuizClick = navAction::navigateToPreflopQuiz,
	)
}

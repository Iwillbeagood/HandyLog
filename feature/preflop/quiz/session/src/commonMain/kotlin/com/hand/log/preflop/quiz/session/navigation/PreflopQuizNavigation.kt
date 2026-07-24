package com.hand.log.preflop.quiz.session.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.hand.log.navigation.navigation.PreflopQuizSession
import com.hand.log.preflop.quiz.session.PreflopQuizSessionRoute
import com.hand.log.preflop.quiz.session.PreflopQuizSessionViewModel
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

fun EntryProviderScope<NavKey>.preflopQuizSessionNavGraph() {
	entry<PreflopQuizSession> { key ->
		val viewModel = koinViewModel<PreflopQuizSessionViewModel> { parametersOf(key.type) }
		PreflopQuizSessionRoute(viewModel = viewModel)
	}
}

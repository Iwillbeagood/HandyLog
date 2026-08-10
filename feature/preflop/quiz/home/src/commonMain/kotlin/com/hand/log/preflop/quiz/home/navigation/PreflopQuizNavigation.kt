package com.hand.log.preflop.quiz.home.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.hand.log.navigation.navigation.PreflopQuiz
import com.hand.log.preflop.quiz.home.PreflopQuizRoute

fun EntryProviderScope<NavKey>.preflopQuizHomeNavGraph() {
	entry<PreflopQuiz> {
		PreflopQuizRoute()
	}
}

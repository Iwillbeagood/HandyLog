package com.hand.log.preflop.quiz.home.di

import com.hand.log.preflop.quiz.home.PreflopQuizViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val featurePreflopQuizHomeModule = module {
	viewModelOf(::PreflopQuizViewModel)
}

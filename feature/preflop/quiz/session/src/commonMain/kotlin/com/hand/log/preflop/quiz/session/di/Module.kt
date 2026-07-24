package com.hand.log.preflop.quiz.session.di

import com.hand.log.domain.usecase.ReviewSpotUseCase
import com.hand.log.preflop.quiz.common.QuizQuestionGenerator
import com.hand.log.preflop.quiz.session.PreflopQuizSessionViewModel
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val featurePreflopQuizSessionModule = module {
	singleOf(::QuizQuestionGenerator)
	singleOf(::ReviewSpotUseCase)
	viewModelOf(::PreflopQuizSessionViewModel)
}

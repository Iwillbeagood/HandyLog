package com.hand.log.handdetail.di

import com.hand.log.domain.usecase.MarkPlayerOnHandUseCase
import com.hand.log.handdetail.HandDetailViewModel
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val featureHandDetailModule = module {
	singleOf(::MarkPlayerOnHandUseCase)
	viewModelOf(::HandDetailViewModel)
}

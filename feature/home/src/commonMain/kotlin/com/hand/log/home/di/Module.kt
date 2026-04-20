package com.hand.log.home.di

import com.hand.log.domain.usecase.ObserveAllHandsWithTableUseCase
import com.hand.log.domain.usecase.ObserveTableListItemsUseCase
import com.hand.log.home.HomeViewModel
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val featureHomeModule = module {
	singleOf(::ObserveTableListItemsUseCase)
	singleOf(::ObserveAllHandsWithTableUseCase)
	viewModelOf(::HomeViewModel)
}

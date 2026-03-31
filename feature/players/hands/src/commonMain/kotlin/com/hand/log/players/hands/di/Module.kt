package com.hand.log.players.hands.di

import com.hand.log.domain.usecase.ObservePlayerHandsUseCase
import com.hand.log.players.hands.PlayerHandsViewModel
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val featurePlayerHandsModule = module {
	singleOf(::ObservePlayerHandsUseCase)
	viewModelOf(::PlayerHandsViewModel)
}

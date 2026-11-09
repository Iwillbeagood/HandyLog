package com.hand.log.playersetup.di

import com.hand.log.domain.usecase.GetOrCreateSavedPlayerUseCase
import com.hand.log.playersetup.PlayerSetupViewModel
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val featurePlayerSetupModule = module {
	singleOf(::GetOrCreateSavedPlayerUseCase)
	viewModelOf(::PlayerSetupViewModel)
}

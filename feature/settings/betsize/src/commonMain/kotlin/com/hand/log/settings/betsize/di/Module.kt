package com.hand.log.settings.betsize.di

import com.hand.log.settings.betsize.BetSizeViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val featureSettingsBetSizeModule = module {
	viewModelOf(::BetSizeViewModel)
}

package com.hand.log.preflop.home.di

import com.hand.log.preflop.home.PreflopHomeViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val featurePreflopHomeModule = module {
	viewModelOf(::PreflopHomeViewModel)
}

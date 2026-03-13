package com.hand.log.home.di

import com.hand.log.home.HomeViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val featureHomeModule = module {
	viewModelOf(::HomeViewModel)
}

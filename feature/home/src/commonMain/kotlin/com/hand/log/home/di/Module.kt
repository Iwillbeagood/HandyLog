package com.hand.log.home.di

import org.koin.compose.viewmodel.dsl.viewModelOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val featureHomeModule = module {
    viewModelOf(::HomeViewModel)
}

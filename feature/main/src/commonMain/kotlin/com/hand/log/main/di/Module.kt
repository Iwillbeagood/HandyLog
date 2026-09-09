package com.hand.log.main.di

import com.hand.log.main.navigation.MainNavigator
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val featureMainModule = module {
	viewModelOf(::MainNavigator)
}

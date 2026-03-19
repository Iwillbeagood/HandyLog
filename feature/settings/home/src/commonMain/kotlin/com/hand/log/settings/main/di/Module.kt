package com.hand.log.settings.main.di

import com.hand.log.settings.main.SettingsViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val featureSettingsMainModule = module {
	viewModelOf(::SettingsViewModel)
}

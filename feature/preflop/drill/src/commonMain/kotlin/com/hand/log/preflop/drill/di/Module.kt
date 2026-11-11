package com.hand.log.preflop.drill.di

import com.hand.log.preflop.drill.PreflopChartDrillViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val featurePreflopChartDrillModule = module {
	viewModelOf(::PreflopChartDrillViewModel)
}

package com.hand.log.preflop.chart.di

import com.hand.log.preflop.chart.PreflopChartViewModel
import com.hand.log.preflop.chart.data.PreflopChartRepository
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val featurePreflopChartModule = module {
	singleOf(::PreflopChartRepository)
	viewModelOf(::PreflopChartViewModel)
}

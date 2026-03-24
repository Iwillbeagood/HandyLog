package com.hand.log.table.di

import com.hand.log.table.TableViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val featureTableModule = module {
	viewModelOf(::TableViewModel)
}

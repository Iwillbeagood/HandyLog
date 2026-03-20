package com.hand.log.tableedit.di

import com.hand.log.tableedit.TableEditViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val featureTableEditModule = module {
	viewModelOf(::TableEditViewModel)
}

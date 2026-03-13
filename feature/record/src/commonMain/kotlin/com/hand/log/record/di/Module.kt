package com.hand.log.record.di

import com.hand.log.record.RecordHandViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val featureRecordModule = module {
	viewModelOf(::RecordHandViewModel)
}

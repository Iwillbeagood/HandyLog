package com.hand.log.record.di

import com.hand.log.domain.usecase.LoadRecordDataUseCase
import com.hand.log.domain.usecase.SaveHandAndUpdateStacksUseCase
import com.hand.log.record.RecordHandViewModel
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val featureRecordModule = module {
	singleOf(::LoadRecordDataUseCase)
	singleOf(::SaveHandAndUpdateStacksUseCase)
	viewModelOf(::RecordHandViewModel)
}

package com.hand.log.table.di

import com.hand.log.domain.usecase.ApplyTableBalanceUseCase
import com.hand.log.domain.usecase.MarkPositionSetupShownUseCase
import com.hand.log.domain.usecase.SavePlayerPositionsUseCase
import com.hand.log.table.TableViewModel
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val featureTableModule = module {
	singleOf(::SavePlayerPositionsUseCase)
	singleOf(::ApplyTableBalanceUseCase)
	singleOf(::MarkPositionSetupShownUseCase)
	viewModelOf(::TableViewModel)
}

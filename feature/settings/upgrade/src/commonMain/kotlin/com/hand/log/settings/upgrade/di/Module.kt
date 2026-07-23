package com.hand.log.settings.upgrade.di

import com.hand.log.domain.usecase.ObserveProStatusUseCase
import com.hand.log.domain.usecase.PurchaseProUseCase
import com.hand.log.domain.usecase.RestorePurchasesUseCase
import com.hand.log.settings.upgrade.ProUpgradeViewModel
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val featureSettingsUpgradeModule = module {
	singleOf(::ObserveProStatusUseCase)
	singleOf(::PurchaseProUseCase)
	singleOf(::RestorePurchasesUseCase)
	viewModelOf(::ProUpgradeViewModel)
}

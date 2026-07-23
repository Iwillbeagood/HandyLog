package com.hand.log.data.datasoure.billing

import org.koin.android.ext.koin.androidContext
import org.koin.dsl.bind
import org.koin.dsl.module

actual val billingModule = module {
	single { AndroidBillingDataSource(androidContext()) } bind BillingDataSource::class
}

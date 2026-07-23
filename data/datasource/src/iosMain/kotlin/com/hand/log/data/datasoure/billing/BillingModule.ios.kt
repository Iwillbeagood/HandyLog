package com.hand.log.data.datasoure.billing

import org.koin.dsl.bind
import org.koin.dsl.module

actual val billingModule = module {
	single { IosBillingDataSource() } bind BillingDataSource::class
}

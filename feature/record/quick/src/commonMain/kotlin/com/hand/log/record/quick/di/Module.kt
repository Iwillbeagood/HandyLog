package com.hand.log.record.quick.di

import com.hand.log.record.quick.QuickRecordViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val featureQuickRecordModule = module {
	viewModel { (tableId: String?) ->
		QuickRecordViewModel(
			tableId = tableId,
			loadRecordData = get(),
			saveHandAndUpdateStacks = get(),
			appSettingsRepository = get(),
		)
	}
}

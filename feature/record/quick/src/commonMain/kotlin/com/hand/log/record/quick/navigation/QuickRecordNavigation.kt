package com.hand.log.record.quick.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.hand.log.navigation.navigation.QuickRecordHand
import com.hand.log.record.quick.QuickRecordRoute
import com.hand.log.record.quick.QuickRecordViewModel
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

fun EntryProviderScope<NavKey>.quickRecordNavGraph() {
	entry<QuickRecordHand> { key ->
		val viewModel: QuickRecordViewModel = koinViewModel { parametersOf(key.tableId) }

		QuickRecordRoute(
			viewModel = viewModel,
		)
	}
}

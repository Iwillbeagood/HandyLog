package com.hand.log.record.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.hand.log.navigation.navigation.RecordHand
import com.hand.log.record.RecordHandRoute
import com.hand.log.record.RecordHandViewModel
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

fun EntryProviderScope<NavKey>.recordHandNavGraph() {
	entry<RecordHand> { key ->
		val viewModel: RecordHandViewModel = koinViewModel { parametersOf(key.tableId, key.handId) }

		RecordHandRoute(
			viewModel = viewModel,
		)
	}
}

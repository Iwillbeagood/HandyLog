package com.hand.log.table.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.hand.log.navigation.navigation.Table
import com.hand.log.table.TableRoute
import com.hand.log.table.TableViewModel
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

fun EntryProviderScope<NavKey>.tableNavGraph() {
	entry<Table> { key ->
		val viewModel = koinViewModel<TableViewModel> { parametersOf(key.tableId) }

		TableRoute(
			viewModel = viewModel,
			openSeat = key.openSeat,
		)
	}
}

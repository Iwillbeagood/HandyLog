package com.hand.log.table.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.hand.log.navigation.navigation.TableDetail
import com.hand.log.table.TableDetailRoute
import com.hand.log.table.TableDetailViewModel
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

fun EntryProviderScope<NavKey>.tableNavGraph() {
	entry<TableDetail> { key ->
		val viewModel = koinViewModel<TableDetailViewModel> { parametersOf(key.tableId) }

		TableDetailRoute(
			viewModel = viewModel,
		)
	}
}

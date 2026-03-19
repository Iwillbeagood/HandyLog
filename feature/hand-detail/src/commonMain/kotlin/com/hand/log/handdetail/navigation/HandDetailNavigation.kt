package com.hand.log.handdetail.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.hand.log.handdetail.HandDetailRoute
import com.hand.log.handdetail.HandDetailViewModel
import com.hand.log.navigation.navigation.HandDetail
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

fun EntryProviderScope<NavKey>.handDetailNavGraph() {
	entry<HandDetail> { key ->
		val viewModel: HandDetailViewModel = koinViewModel { parametersOf(key.handId) }
		HandDetailRoute(viewModel = viewModel)
	}
}

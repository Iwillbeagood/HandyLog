package com.hand.log.settings.contact.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.hand.log.navigation.navigation.Contact
import com.hand.log.settings.contact.ContactRoute
import com.hand.log.settings.contact.ContactViewModel
import org.koin.compose.viewmodel.koinViewModel

fun EntryProviderScope<NavKey>.contactNavGraph() {
	entry<Contact> { _ ->
		val viewModel: ContactViewModel = koinViewModel()

		ContactRoute(
			viewModel = viewModel,
		)
	}
}

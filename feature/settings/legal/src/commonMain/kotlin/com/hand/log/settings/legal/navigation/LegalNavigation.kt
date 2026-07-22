package com.hand.log.settings.legal.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.hand.log.navigation.navigation.Legal
import com.hand.log.settings.legal.LegalRoute

fun EntryProviderScope<NavKey>.legalNavGraph() {
	entry<Legal> {
		LegalRoute()
	}
}

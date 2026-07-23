package com.hand.log

import androidx.compose.ui.window.ComposeUIViewController
import org.koin.compose.KoinApplication

fun mainViewController() = ComposeUIViewController {
	KoinApplication(
		application = handLogAppDeclaration(),
	) {
		App()
	}
}

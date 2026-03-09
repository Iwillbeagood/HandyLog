package com.hand.log

import androidx.compose.ui.window.ComposeUIViewController
import org.koin.compose.KoinApplication

fun MainViewController() = ComposeUIViewController {
	KoinApplication(
		application = handLogAppDeclaration(),
	) {
		App()
	}
}

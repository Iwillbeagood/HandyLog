package com.hand.log

import androidx.compose.ui.window.ComposeUIViewController
import com.hand.log.common.AppConfig
import org.koin.compose.KoinApplication

fun mainViewController() = ComposeUIViewController {
	AppConfig.initialize(isProBuild = true)

	KoinApplication(
		application = handLogAppDeclaration(),
	) {
		App()
	}
}

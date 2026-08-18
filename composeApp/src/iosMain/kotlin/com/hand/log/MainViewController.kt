package com.hand.log

import androidx.compose.ui.window.ComposeUIViewController
import org.koin.compose.KoinApplication
import kotlin.experimental.ExperimentalNativeApi
import kotlin.native.Platform

@OptIn(ExperimentalNativeApi::class)
fun mainViewController() = ComposeUIViewController {
	KoinApplication(
		application = handLogAppDeclaration {
			// 디버그 바이너리에서만 Pro 권한을 강제로 켜 유료 기능을 테스트한다. 릴리스는 영향 없음.
			if (Platform.isDebugBinary) modules(forceProModule)
		},
	) {
		App()
	}
}

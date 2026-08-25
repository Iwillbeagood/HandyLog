package com.hand.log

import androidx.compose.ui.window.ComposeUIViewController
import org.koin.compose.KoinApplication

fun mainViewController() = ComposeUIViewController {
	KoinApplication(
		application = handLogAppDeclaration {
			// iOS는 App Store 선불 유료 앱이라 설치자 전원이 이미 결제한 상태 → Pro 권한을 항상 켠다(앱 내 구매·페이월 없음).
			modules(forceProModule)
		},
	) {
		App()
	}
}

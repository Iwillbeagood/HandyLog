package com.hand.log.designsystem.etc

import androidx.compose.runtime.compositionLocalOf

/**
 * 앱 루트에서 한 번 관찰한 Pro 활성화 여부를 하위 화면에 내려준다.
 * 화면마다 별도 구독 시 발생하던 진입 시 무료→Pro 깜빡임을 방지하기 위한 단일 소스.
 * designsystem 공용 컴포넌트(예: HomeLogo)에서도 직접 읽을 수 있도록 이 모듈에 둔다.
 */
val LocalProStatus = compositionLocalOf { false }

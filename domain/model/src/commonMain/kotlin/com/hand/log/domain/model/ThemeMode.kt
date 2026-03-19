package com.hand.log.domain.model

enum class ThemeMode(val label: String, val desc: String) {
	AUTO("자동", "시스템 설정에 따라 자동 전환"),
	LIGHT("라이트", "밝은 테마"),
	DARK("다크", "어두운 테마"),
}

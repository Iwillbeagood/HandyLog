package com.hand.log.designsystem.theme

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color

val LocalHandyColorScheme = compositionLocalOf<HandyColorScheme> {
	error("No HandyColorScheme provided")
}

val DarkHandyColorScheme
	get() = HandyColorScheme(
		background = Color(0xFF14171E), // 앱 배경
		textPrimary = Color(0xFFF2F2F2), // 기본 텍스트
		card = Color(0xFF1D212A), // 카드/패널 배경
		modalBackground = Color(0xFF191C24), // 모달 배경
		muted = Color(0xFF272B34), // 비활성 영역 배경
		textSecondary = Color(0xFF808897), // 보조 텍스트
		primary = Color(0xFF0FB67F), // 주요 액션
		onPrimary = Color(0xFFF2F2F2), // 주요 액션 위 텍스트
		accent = Color(0xFF22C38D), // 강조
		onAccent = Color(0xFFF2F2F2), // 강조 위 텍스트
		secondary = Color(0xFF2F3541), // 보조 요소 배경
		onSecondary = Color(0xFFF2F2F2), // 보조 요소 텍스트
		error = Color(0xFFDC2828), // 경고/삭제
		border = Color(0xFF30353F), // 테두리
		inputBorder = Color(0xFF2B303A), // 입력 필드 테두리
		focusRing = Color(0xFF0FB67F), // 포커스 링
		felt = Color(0xFF235C41), // 포커 테이블
		feltLight = Color(0xFF2D6B4E), // 포커 테이블 (밝은)
		gold = Color(0xFFF7C530), // 팟/칩 금액
		goldMuted = Color(0xFFC39A22), // 보조 금색
		suitRed = Color(0xFFE83030), // 하트/다이아
		suitBlack = Color(0xFF363C49), // 클럽/스페이드
	)

val LightHandyColorScheme
	get() = HandyColorScheme(
		background = Color(0xFFF9F9F9),
		textPrimary = Color(0xFF181C24),
		card = Color(0xFFFFFFFF),
		modalBackground = Color(0xFFFFFFFF),
		muted = Color(0xFFF0F1F4),
		textSecondary = Color(0xFF676E7E),
		primary = Color(0xFF0EA875),
		onPrimary = Color(0xFFFFFFFF),
		accent = Color(0xFF1EAD7D),
		onAccent = Color(0xFFFFFFFF),
		secondary = Color(0xFFE7E9ED),
		onSecondary = Color(0xFF353C49),
		error = Color(0xFFDC2828),
		border = Color(0xFFDCDEE4),
		inputBorder = Color(0xFFE1E4E9),
		focusRing = Color(0xFF0EA875),
		felt = Color(0xFF428968),
		feltLight = Color(0xFF559F7C),
		gold = Color(0xFFDDA808),
		goldMuted = Color(0xFFA4821D),
		suitRed = Color(0xFFE51919),
		suitBlack = Color(0xFF414857),
	)

data class HandyColorScheme(
	val background: Color, // 앱 전체 배경
	val card: Color, // 카드/패널 배경
	val modalBackground: Color, // 모달 배경
	val muted: Color, // 비활성 영역 배경

	// 텍스트
	val textPrimary: Color, // 기본 텍스트
	val textSecondary: Color, // 보조/비활성 텍스트

	// 액션
	val primary: Color, // 주요 액션 (에메랄드)
	val onPrimary: Color, // 주요 액션 위 텍스트
	val accent: Color, // 강조 색상
	val onAccent: Color, // 강조 위 텍스트

	// 보조
	val secondary: Color, // 보조 요소 배경
	val onSecondary: Color, // 보조 요소 텍스트

	// 상태
	val error: Color, // 경고/삭제

	// 테두리
	val border: Color, // 일반 테두리
	val inputBorder: Color, // 입력 필드 테두리
	val focusRing: Color, // 포커스 링

	// 포커 테이블
	val felt: Color, // 테이블 배경
	val feltLight: Color, // 테이블 배경 (밝은)

	// 칩/금액
	val gold: Color, // 팟/칩 금액
	val goldMuted: Color, // 보조 금색

	// 카드 문양
	val suitRed: Color, // 하트/다이아
	val suitBlack: Color, // 클럽/스페이드
)

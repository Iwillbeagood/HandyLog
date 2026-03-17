package com.hand.log.designsystem.etc

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.hand.log.designsystem.theme.HandLogTheme
import com.hand.log.designsystem.theme.HandyTheme

/**
 * 다크/라이트 모드를 한번에 프리뷰하기 위한 annotation.
 * CMP에서는 uiMode가 지원되지 않으므로 [ThemePreviews] + [HandLogTheme]만 사용합니다.
 */
typealias ThemePreviews = Preview

/**
 * 다크/라이트 모드를 나란히 보여주는 프리뷰 헬퍼.
 *
 * ```
 * @Preview
 * @Composable
 * private fun MyPreview() = DualThemePreview {
 *     MyComponent()
 * }
 * ```
 */
@Composable
fun ThemePreview(content: @Composable () -> Unit) {
	Column {
		HandLogTheme(darkTheme = true) {
			Column(
				modifier = Modifier
					.fillMaxWidth()
					.background(HandyTheme.colorScheme.background),
			) {
				content()
			}
		}
		HandLogTheme(darkTheme = false) {
			Column(
				modifier = Modifier
					.fillMaxWidth()
					.background(HandyTheme.colorScheme.background),
			) {
				content()
			}
		}
	}
}

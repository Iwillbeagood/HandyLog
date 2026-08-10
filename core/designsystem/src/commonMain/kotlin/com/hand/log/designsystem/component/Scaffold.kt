package com.hand.log.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.contentColorFor
import com.hand.log.designsystem.theme.HandyTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp

/**
 * navigationBarPadding, background, statusBarPadding 순서 변경x
 */
@Composable
fun BaseScaffold(
	modifier: Modifier = Modifier,
	statusBarColor: Color = HandyTheme.colorScheme.background,
	contentPadding: PaddingValues = PaddingValues(0.dp),
	applyNavigationBarsPadding: Boolean = true,
	topBar: @Composable () -> Unit = {},
	bottomBar: @Composable () -> Unit = {},
	snackbarHost: @Composable () -> Unit = {},
	floatingActionButton: @Composable () -> Unit = {},
	containerColor: Color = HandyTheme.colorScheme.background,
	contentColor: Color = contentColorFor(containerColor),
	content: @Composable ColumnScope.() -> Unit,
) {
	val focusManager = LocalFocusManager.current

	Scaffold(
		topBar = {
			Column(
				modifier = Modifier
					.background(color = statusBarColor)
					// 탑 앱바는 상태바(위) 인셋만 필요. systemBarsPadding 을 쓰면 네비바(아래) 인셋까지
					// 붙어 앱바 아래에 빈 패딩이 생긴다(특히 스캐폴드가 navigationBarsPadding 을 소비하지 않는 탭 화면).
					.statusBarsPadding(),
			) {
				topBar()
			}
		},
		bottomBar = bottomBar,
		snackbarHost = snackbarHost,
		floatingActionButton = floatingActionButton,
		containerColor = containerColor,
		contentColor = contentColor,
		contentWindowInsets = WindowInsets(0.dp),
		modifier = modifier
			.then(if (applyNavigationBarsPadding) Modifier.navigationBarsPadding() else Modifier),
	) {
		Column(
			modifier = Modifier
				.fillMaxSize()
				.padding(it)
				.padding(contentPadding)
				.pointerInput(Unit) {
					detectTapGestures { focusManager.clearFocus() }
				},
		) {
			content()
		}
	}
}

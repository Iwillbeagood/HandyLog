package com.hand.log.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Scaffold
import androidx.compose.material3.contentColorFor
import com.hand.log.designsystem.theme.HandyTheme
import com.hand.log.designsystem.window.LocalWindowSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

val DefaultContentMaxWidth: Dp = 840.dp

/**
 * navigationBarPadding, background, statusBarPadding 순서 변경x
 */
@Composable
fun BaseScaffold(
	modifier: Modifier = Modifier,
	statusBarColor: Color = HandyTheme.colorScheme.background,
	contentPadding: PaddingValues = PaddingValues(0.dp),
	applyNavigationBarsPadding: Boolean = true,
	contentMaxWidth: Dp? = DefaultContentMaxWidth,
	topBar: @Composable () -> Unit = {},
	bottomBar: @Composable () -> Unit = {},
	snackbarHost: @Composable () -> Unit = {},
	floatingActionButton: @Composable () -> Unit = {},
	containerColor: Color = HandyTheme.colorScheme.background,
	contentColor: Color = contentColorFor(containerColor),
	content: @Composable ColumnScope.() -> Unit,
) {
	val focusManager = LocalFocusManager.current
	val windowSize = LocalWindowSize.current
	val constrainWidth = contentMaxWidth != null && windowSize.isLarge

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
		Box(
			modifier = Modifier
				.fillMaxSize()
				.padding(it)
				.pointerInput(Unit) {
					detectTapGestures { focusManager.clearFocus() }
				},
			contentAlignment = Alignment.TopCenter,
		) {
			Column(
				modifier = Modifier
					.then(
						if (constrainWidth) {
							Modifier.fillMaxHeight().widthIn(max = contentMaxWidth!!)
						} else {
							Modifier.fillMaxSize()
						},
					)
					.padding(contentPadding),
			) {
				content()
			}
		}
	}
}

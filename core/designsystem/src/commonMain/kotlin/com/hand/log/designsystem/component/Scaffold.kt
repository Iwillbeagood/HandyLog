package com.hand.log.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.contentColorFor
import com.hand.log.designsystem.theme.HandyTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * navigationBarPadding, background, statusBarPadding 순서 변경x
 */
@Composable
fun BaseScaffold(
	modifier: Modifier = Modifier,
	statusBarColor: Color = HandyTheme.colorScheme.background,
	contentPadding: PaddingValues = PaddingValues(0.dp),
	topBar: @Composable () -> Unit = {},
	bottomBar: @Composable () -> Unit = {},
	snackbarHost: @Composable () -> Unit = {},
	floatingActionButton: @Composable () -> Unit = {},
	containerColor: Color = HandyTheme.colorScheme.background,
	contentColor: Color = contentColorFor(containerColor),
	content: @Composable ColumnScope.() -> Unit,
) {
	Scaffold(
		topBar = {
			Column(
				modifier = Modifier
					.background(color = statusBarColor)
					.systemBarsPadding(),
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
			.navigationBarsPadding(),
	) {
		Column(
			modifier = Modifier
				.fillMaxSize()
				.padding(it)
				.padding(contentPadding),
		) {
			content()
		}
	}
}

@Composable
fun TopAppBarScaffold(
	title: String,
	onBackEvent: () -> Unit,
	modifier: Modifier = Modifier,
	statusBarColor: Color = HandyTheme.colorScheme.background,
	contentPadding: PaddingValues = PaddingValues(0.dp),
	bottomBar: @Composable () -> Unit = {},
	floatingActionButton: @Composable () -> Unit = {},
	containerColor: Color = HandyTheme.colorScheme.background,
	contentColor: Color = contentColorFor(containerColor),
	content: @Composable ColumnScope.() -> Unit,
) {
	BaseScaffold(
		contentPadding = contentPadding,
		statusBarColor = statusBarColor,
		topBar = {
			HmTopAppbar(
				title = title,
				onBackEvent = onBackEvent,
			)
		},
		bottomBar = bottomBar,
		floatingActionButton = floatingActionButton,
		containerColor = containerColor,
		contentColor = contentColor,
		modifier = modifier,
		content = content,
	)
}

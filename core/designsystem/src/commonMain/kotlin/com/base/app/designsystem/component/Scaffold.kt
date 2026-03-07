package com.hand.log.designsystem.component

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.contentColorFor
import androidx.compose.material3.pulltorefresh.PullToRefreshState
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
	statusBarColor: Color = MaterialTheme.colorScheme.surface,
	contentPadding: PaddingValues = PaddingValues(0.dp),
	topBar: @Composable () -> Unit = {},
	bottomBar: @Composable () -> Unit = {},
	snackbarHost: @Composable () -> Unit = {},
	floatingActionButton: @Composable () -> Unit = {},
	containerColor: Color = MaterialTheme.colorScheme.surface,
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
	statusBarColor: Color = MaterialTheme.colorScheme.surface,
	contentPadding: PaddingValues = PaddingValues(0.dp),
	bottomBar: @Composable () -> Unit = {},
	floatingActionButton: @Composable () -> Unit = {},
	containerColor: Color = MaterialTheme.colorScheme.surface,
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

@Composable
fun HmRefreshScaffold(
	state: PullToRefreshState,
	isRefreshing: Boolean,
	onRefresh: () -> Unit,
	modifier: Modifier = Modifier,
	statusBarColor: Color = MaterialTheme.colorScheme.surface,
	topBar: @Composable () -> Unit = {},
	bottomBar: @Composable () -> Unit = {},
	floatingActionButton: @Composable () -> Unit = {},
	containerColor: Color = MaterialTheme.colorScheme.surface,
	contentColor: Color = contentColorFor(containerColor),
	content: @Composable BoxScope.() -> Unit,
) {
	BaseScaffold(
		topBar = topBar,
		bottomBar = bottomBar,
		floatingActionButton = floatingActionButton,
		containerColor = containerColor,
		contentColor = contentColor,
		statusBarColor = statusBarColor,
		modifier = modifier,
	) {
		HmPullToRefreshBox(
			modifier = Modifier
				.fillMaxSize()
				.animateContentSize(),
			state = state,
			isRefreshing = isRefreshing,
			onRefresh = onRefresh,
			content = content,
		)
	}
}

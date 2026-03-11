package com.hand.log.designsystem.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults.Indicator
import androidx.compose.material3.pulltorefresh.PullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.hand.log.designsystem.theme.HmmTheme

@Composable
fun HmPullToRefreshBox(
	state: PullToRefreshState,
	isRefreshing: Boolean,
	onRefresh: () -> Unit,
	modifier: Modifier = Modifier,
	content: @Composable BoxScope.() -> Unit,
) {
	PullToRefreshBox(
		modifier = modifier,
		state = state,
		isRefreshing = isRefreshing,
		onRefresh = onRefresh,
		indicator = {
			Box(
				modifier = Modifier
					.fillMaxWidth()
					.align(Alignment.TopCenter),
			) {
				Indicator(
					modifier = Modifier
						.align(Alignment.TopCenter),
					isRefreshing = isRefreshing,
					state = state,
					color = MaterialTheme.colorScheme.primary,
					containerColor = HmmTheme.fixedColor.white,
				)
			}
		},
		content = content,
	)
}

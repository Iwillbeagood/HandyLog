package com.hand.log.handdetail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.hand.log.handdetail.contract.HandDetailState
import com.hand.log.navigation.interop.LocalNavigateActionInterop

@Composable
internal fun HandDetailRoute(
	viewModel: HandDetailViewModel,
) {
	val state by viewModel.state.collectAsState()
	val navAction = LocalNavigateActionInterop.current

	when (val current = state) {
		HandDetailState.Loading -> {}
		HandDetailState.Error -> {}
		is HandDetailState.Success -> {
			HandDetailScreen(
				hand = current.hand,
				onBack = navAction::popBackStack,
			)
		}
	}
}

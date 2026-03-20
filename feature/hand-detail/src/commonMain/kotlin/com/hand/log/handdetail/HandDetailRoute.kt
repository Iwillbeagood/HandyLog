package com.hand.log.handdetail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hand.log.handdetail.contract.HandDetailState
import com.hand.log.navigation.interop.LocalNavigateActionInterop
import com.hand.log.navigation.interop.rememberShowSnackBar

@Composable
internal fun HandDetailRoute(
	viewModel: HandDetailViewModel,
) {
	val state by viewModel.state.collectAsStateWithLifecycle()
	val navAction = LocalNavigateActionInterop.current
	val showToast = rememberShowSnackBar()

	when (val current = state) {
		HandDetailState.Loading -> {}
		HandDetailState.Error -> {}
		is HandDetailState.Success -> {
			HandDetailScreen(
				state = current,
				onToggleBbUnit = viewModel::toggleBbUnit,
				onBack = navAction::popBackStack,
				onShowToast = showToast,
			)
		}
	}
}

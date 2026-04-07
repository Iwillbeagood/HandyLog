package com.hand.log.players.hands

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hand.log.navigation.interop.LocalNavigateActionInterop

@Composable
internal fun PlayerHandsRoute(
	playerName: String,
	viewModel: PlayerHandsViewModel,
	paddingValues: PaddingValues = PaddingValues(),
) {
	val state by viewModel.state.collectAsStateWithLifecycle()
	val navAction = LocalNavigateActionInterop.current

	PlayerHandsScreen(
		playerName = playerName,
		state = state,
		onHandClick = navAction::navigateToHandDetail,
		onBack = navAction::popBackStack,
		paddingValues = paddingValues,
	)
}

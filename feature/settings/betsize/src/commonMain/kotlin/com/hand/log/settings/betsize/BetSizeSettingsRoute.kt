package com.hand.log.settings.betsize

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hand.log.navigation.interop.LocalNavigateActionInterop
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun BetSizeSettingsRoute() {
	val viewModel: BetSizeViewModel = koinViewModel()
	val state by viewModel.state.collectAsStateWithLifecycle()
	val navAction = LocalNavigateActionInterop.current

	BetSizeSettingsScreen(
		state = state,
		onAddPreflopPreset = viewModel::addPreflopPreset,
		onRemovePreflopPreset = viewModel::removePreflopPreset,
		onAddPostflopPreset = viewModel::addPostflopPreset,
		onRemovePostflopPreset = viewModel::removePostflopPreset,
		onBack = navAction::popBackStack,
	)
}

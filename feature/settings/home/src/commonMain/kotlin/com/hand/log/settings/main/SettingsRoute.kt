package com.hand.log.settings.main

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hand.log.navigation.interop.LocalNavigateActionInterop

@Composable
internal fun SettingsRoute(
	viewModel: SettingsViewModel,
) {
	val settings by viewModel.settings.collectAsStateWithLifecycle()
	val navAction = LocalNavigateActionInterop.current

	SettingsScreen(
		settings = settings,
		onThemeChange = viewModel::updateTheme,
		onNavigateToBetSize = navAction::navigateToBetSizeSettings,
	)
}

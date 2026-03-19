package com.hand.log.settings.main

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hand.log.navigation.interop.LocalNavigateActionInterop
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun SettingsRoute() {
	val viewModel: SettingsViewModel = koinViewModel()
	val settings by viewModel.settings.collectAsStateWithLifecycle()
	val navAction = LocalNavigateActionInterop.current

	SettingsScreen(
		settings = settings,
		onThemeChange = viewModel::updateTheme,
		onNavigateToBetSize = navAction::navigateToBetSizeSettings,
	)
}

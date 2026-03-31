package com.hand.log.settings.main

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalUriHandler
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hand.log.navigation.interop.LocalNavigateActionInterop

private const val CONTACT_FORM_URL = "https://forms.gle/YOUR_GOOGLE_FORM_ID"

@Composable
internal fun SettingsRoute(
	viewModel: SettingsViewModel,
) {
	val settings by viewModel.settings.collectAsStateWithLifecycle()
	val navAction = LocalNavigateActionInterop.current
	val uriHandler = LocalUriHandler.current

	SettingsScreen(
		settings = settings,
		onThemeChange = viewModel::updateTheme,
		onNavigateToBetSize = navAction::navigateToBetSizeSettings,
		onContactClick = { uriHandler.openUri(CONTACT_FORM_URL) },
	)
}

package com.hand.log.settings.main

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hand.log.common.AppConfig
import com.hand.log.navigation.interop.LocalNavigateActionInterop
import com.hand.log.settings.main.contract.SettingsModalEffect
import com.hand.log.ui.ProPaywallSheet

@Composable
internal fun SettingsRoute(
	viewModel: SettingsViewModel,
) {
	val settings by viewModel.settings.collectAsStateWithLifecycle()
	val modalEffect by viewModel.modalEffect.collectAsStateWithLifecycle()
	val navAction = LocalNavigateActionInterop.current

	SettingsScreen(
		settings = settings,
		isProBuild = AppConfig.isProBuild,
		onThemeChange = viewModel::updateTheme,
		onNavigateToBetSize = {
			if (viewModel.canNavigateToBetSize()) {
				navAction.navigateToBetSizeSettings()
			} else {
				viewModel.showPresetsPaywall()
			}
		},
		onUpgradeClick = navAction::navigateToProUpgrade,
		onContactClick = navAction::navigateToContact,
		onLegalClick = navAction::navigateToLegal,
	)

	when (val effect = modalEffect) {
		SettingsModalEffect.Idle -> {}
		is SettingsModalEffect.ShowPaywall -> {
			ProPaywallSheet(
				feature = effect.feature,
				onDismiss = viewModel::dismissModal,
			)
		}
	}
}

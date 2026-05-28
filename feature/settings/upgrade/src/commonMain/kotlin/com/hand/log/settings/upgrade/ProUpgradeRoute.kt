package com.hand.log.settings.upgrade

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalUriHandler
import com.hand.log.navigation.interop.LocalNavigateActionInterop

private const val PRO_STORE_URL = "https://play.google.com/store/apps/details?id=com.hand.log.pro"

@Composable
internal fun ProUpgradeRoute() {
	val navAction = LocalNavigateActionInterop.current
	val uriHandler = LocalUriHandler.current

	ProUpgradeScreen(
		onBack = navAction::popBackStack,
		onDownloadClick = { uriHandler.openUri(PRO_STORE_URL) },
	)
}

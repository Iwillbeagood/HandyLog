package com.hand.log.settings.legal

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalUriHandler
import com.hand.log.navigation.interop.LocalNavigateActionInterop
import handylog.core.res.generated.resources.Res
import handylog.core.res.generated.resources.legal_privacy_policy

private const val PRIVACY_POLICY_URL =
	"https://royal-soapwort-5f5.notion.site/HandyLog-3938c719c24d800fba3fd6bde3af23d2"

@Composable
internal fun LegalRoute() {
	val navAction = LocalNavigateActionInterop.current
	val uriHandler = LocalUriHandler.current

	val items = remember {
		listOf(
			LegalItem(Res.string.legal_privacy_policy, PRIVACY_POLICY_URL),
		)
	}

	LegalScreen(
		items = items,
		onBack = navAction::popBackStack,
		onItemClick = { uriHandler.openUri(it.url) },
	)
}

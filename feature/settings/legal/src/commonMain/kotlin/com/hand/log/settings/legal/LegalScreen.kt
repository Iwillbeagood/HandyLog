package com.hand.log.settings.legal

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hand.log.designsystem.component.BaseScaffold
import com.hand.log.designsystem.component.HandyTopAppbar
import com.hand.log.designsystem.component.SettingsNavigationItem
import com.hand.log.designsystem.etc.ThemePreview
import com.hand.log.designsystem.etc.ThemePreviews
import handylog.core.res.generated.resources.Res
import handylog.core.res.generated.resources.legal_privacy_policy
import handylog.core.res.generated.resources.legal_title
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

internal data class LegalItem(
	val titleRes: StringResource,
	val url: String,
)

@Composable
internal fun LegalScreen(
	items: List<LegalItem>,
	onBack: () -> Unit,
	onItemClick: (LegalItem) -> Unit,
) {
	BaseScaffold(
		topBar = {
			HandyTopAppbar(
				title = stringResource(Res.string.legal_title),
				onBackEvent = onBack,
			)
		},
	) {
		Column(
			modifier = Modifier
				.fillMaxSize()
				.verticalScroll(rememberScrollState())
				.padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp),
			verticalArrangement = Arrangement.spacedBy(12.dp),
		) {
			items.forEach { item ->
				SettingsNavigationItem(
					title = stringResource(item.titleRes),
					onClick = { onItemClick(item) },
				)
			}
		}
	}
}

@ThemePreviews
@Composable
private fun LegalScreenPreview() {
	ThemePreview {
		LegalScreen(
			items = listOf(LegalItem(Res.string.legal_privacy_policy, "")),
			onBack = {},
			onItemClick = {},
		)
	}
}

package com.hand.log.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import com.hand.log.designsystem.component.modal.HandyBottomSheet
import com.hand.log.designsystem.theme.HandyTheme
import com.hand.log.domain.model.ProFeature
import handylog.core.res.generated.resources.Res
import handylog.core.res.generated.resources.pro_paywall_confirm
import handylog.core.res.generated.resources.pro_paywall_hands
import handylog.core.res.generated.resources.pro_paywall_players
import handylog.core.res.generated.resources.pro_paywall_presets
import handylog.core.res.generated.resources.pro_paywall_tables
import handylog.core.res.generated.resources.pro_paywall_title
import org.jetbrains.compose.resources.stringResource

@Composable
fun ProPaywallSheet(
	feature: ProFeature,
	onDismiss: () -> Unit,
) {
	HandyBottomSheet(
		onDismissRequest = onDismiss,
		title = stringResource(Res.string.pro_paywall_title),
		confirmText = stringResource(Res.string.pro_paywall_confirm),
		onConfirm = onDismiss,
	) {
		Text(
			text = featureDescription(feature),
			style = HandyTheme.typography.regular16,
			color = HandyTheme.colorScheme.textSecondary,
			textAlign = TextAlign.Center,
			modifier = Modifier.fillMaxWidth(),
		)
	}
}

@Composable
private fun featureDescription(feature: ProFeature): String = when (feature) {
	ProFeature.UNLIMITED_TABLES -> stringResource(
		Res.string.pro_paywall_tables,
		MAX_FREE_TABLES,
	)
	ProFeature.UNLIMITED_HANDS -> stringResource(
		Res.string.pro_paywall_hands,
		MAX_FREE_HANDS_PER_TABLE,
	)
	ProFeature.UNLIMITED_PLAYERS -> stringResource(
		Res.string.pro_paywall_players,
		MAX_FREE_PLAYERS,
	)
	ProFeature.CUSTOM_PRESETS -> stringResource(Res.string.pro_paywall_presets)
}

private const val MAX_FREE_TABLES = 2
private const val MAX_FREE_HANDS_PER_TABLE = 5
private const val MAX_FREE_PLAYERS = 5

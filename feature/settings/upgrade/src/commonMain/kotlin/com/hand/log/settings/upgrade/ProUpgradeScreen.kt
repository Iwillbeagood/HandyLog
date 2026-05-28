package com.hand.log.settings.upgrade

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.hand.log.designsystem.component.BaseScaffold
import com.hand.log.designsystem.component.HandyHorizontalDivider
import com.hand.log.designsystem.component.HandyTopAppbar
import com.hand.log.designsystem.component.RegularButton
import com.hand.log.designsystem.component.TopAppbarType
import com.hand.log.designsystem.etc.ThemePreview
import com.hand.log.designsystem.etc.ThemePreviews
import com.hand.log.designsystem.theme.HandyTheme
import handylog.core.res.generated.resources.Res
import handylog.core.res.generated.resources.crown
import handylog.core.res.generated.resources.upgrade_title
import handylog.core.res.generated.resources.upgrade_hero_title
import handylog.core.res.generated.resources.upgrade_hero_desc
import handylog.core.res.generated.resources.upgrade_header_feature
import handylog.core.res.generated.resources.upgrade_header_free
import handylog.core.res.generated.resources.upgrade_header_pro
import handylog.core.res.generated.resources.upgrade_tables
import handylog.core.res.generated.resources.upgrade_hands
import handylog.core.res.generated.resources.upgrade_players
import handylog.core.res.generated.resources.upgrade_presets
import handylog.core.res.generated.resources.upgrade_free_limit
import handylog.core.res.generated.resources.upgrade_unlimited
import handylog.core.res.generated.resources.upgrade_download
import handylog.core.res.generated.resources.upgrade_store_note
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun ProUpgradeScreen(
	onBack: () -> Unit,
	onDownloadClick: () -> Unit,
) {
	BaseScaffold {
		Column(modifier = Modifier.fillMaxSize()) {
			HandyTopAppbar(
				title = stringResource(Res.string.upgrade_title),
				navigationType = TopAppbarType.Default,
				onBackEvent = onBack,
			)
			HandyHorizontalDivider()

			Column(
				modifier = Modifier
					.fillMaxSize()
					.verticalScroll(rememberScrollState())
					.padding(horizontal = 16.dp, vertical = 24.dp),
				verticalArrangement = Arrangement.spacedBy(28.dp),
			) {
				HeroSection()
				ComparisonTable()
				DownloadSection(onDownloadClick = onDownloadClick)
			}
		}
	}
}

@Composable
private fun HeroSection() {
	Column(
		modifier = Modifier.fillMaxWidth(),
		horizontalAlignment = Alignment.CenterHorizontally,
		verticalArrangement = Arrangement.spacedBy(12.dp),
	) {
		Icon(
			painter = painterResource(Res.drawable.crown),
			contentDescription = null,
			modifier = Modifier.size(48.dp),
			tint = HandyTheme.colorScheme.gold,
		)
		Text(
			text = stringResource(Res.string.upgrade_hero_title),
			style = HandyTheme.typography.bold24,
			color = HandyTheme.colorScheme.textPrimary,
		)
		Text(
			text = stringResource(Res.string.upgrade_hero_desc),
			style = HandyTheme.typography.regular14,
			color = HandyTheme.colorScheme.textSecondary,
		)
	}
}

@Composable
private fun ComparisonTable() {
	val colors = HandyTheme.colorScheme
	val typography = HandyTheme.typography

	Column(
		modifier = Modifier
			.fillMaxWidth()
			.clip(RoundedCornerShape(12.dp))
			.background(colors.card),
	) {
		Row(
			modifier = Modifier
				.fillMaxWidth()
				.background(colors.muted)
				.padding(horizontal = 12.dp, vertical = 12.dp),
		) {
			Text(
				text = stringResource(Res.string.upgrade_header_feature),
				style = typography.bold12,
				color = colors.textSecondary,
				modifier = Modifier.weight(1f),
			)
			Text(
				text = stringResource(Res.string.upgrade_header_free),
				style = typography.bold12,
				color = colors.textSecondary,
				textAlign = TextAlign.Center,
				modifier = Modifier.weight(1f),
			)
			Text(
				text = stringResource(Res.string.upgrade_header_pro),
				style = typography.bold12,
				color = colors.primary,
				textAlign = TextAlign.Center,
				modifier = Modifier.weight(1f),
			)
		}

		val features = listOf(
			Triple(Res.string.upgrade_tables, Res.string.upgrade_free_limit to 2, true),
			Triple(Res.string.upgrade_hands, Res.string.upgrade_free_limit to 5, true),
			Triple(Res.string.upgrade_players, Res.string.upgrade_free_limit to 5, true),
			Triple(Res.string.upgrade_presets, null, true),
		)

		features.forEach { (featureRes, freeLimit, _) ->
			HandyHorizontalDivider()
			Row(
				modifier = Modifier
					.fillMaxWidth()
					.padding(horizontal = 12.dp, vertical = 12.dp),
				verticalAlignment = Alignment.CenterVertically,
			) {
				Text(
					text = stringResource(featureRes),
					style = typography.medium14,
					color = colors.textPrimary,
					modifier = Modifier.weight(1f),
				)
				Text(
					text = if (freeLimit != null) {
						stringResource(freeLimit.first, freeLimit.second)
					} else {
						"X"
					},
					style = typography.regular12,
					color = colors.textSecondary,
					textAlign = TextAlign.Center,
					modifier = Modifier.weight(1f),
				)
				Text(
					text = stringResource(Res.string.upgrade_unlimited),
					style = typography.medium12,
					color = colors.primary,
					textAlign = TextAlign.Center,
					modifier = Modifier.weight(1f),
				)
			}
		}
	}
}

@Composable
private fun DownloadSection(onDownloadClick: () -> Unit) {
	Column(
		modifier = Modifier.fillMaxWidth(),
		horizontalAlignment = Alignment.CenterHorizontally,
		verticalArrangement = Arrangement.spacedBy(12.dp),
	) {
		RegularButton(
			onClick = onDownloadClick,
			text = stringResource(Res.string.upgrade_download),
			modifier = Modifier.fillMaxWidth(),
		)
		Text(
			text = stringResource(Res.string.upgrade_store_note),
			style = HandyTheme.typography.regular12,
			color = HandyTheme.colorScheme.textSecondary,
		)
	}
}

@ThemePreviews
@Composable
private fun ProUpgradeScreenPreview() {
	ThemePreview {
		ProUpgradeScreen(
			onBack = {},
			onDownloadClick = {},
		)
	}
}

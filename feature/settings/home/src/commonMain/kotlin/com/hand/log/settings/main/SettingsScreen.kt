package com.hand.log.settings.main

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.hand.log.designsystem.component.BaseScaffold
import com.hand.log.designsystem.component.HandyHorizontalDivider
import com.hand.log.designsystem.component.HandySectionLabel
import com.hand.log.designsystem.component.HandyTopAppbar
import com.hand.log.designsystem.component.TopAppbarType
import com.hand.log.designsystem.component.VerticalSpacer
import com.hand.log.designsystem.etc.ThemePreview
import com.hand.log.designsystem.etc.ThemePreviews
import com.hand.log.designsystem.theme.HandyTheme
import com.hand.log.domain.model.ThemeMode
import com.hand.log.ui.localizedLabel
import com.hand.log.ui.localizedDesc
import com.hand.log.settings.main.contract.AppSettings
import handylog.core.res.generated.resources.Res
import handylog.core.res.generated.resources.monitor
import handylog.core.res.generated.resources.moon
import handylog.core.res.generated.resources.sun
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import handylog.core.res.generated.resources.*

@Composable
internal fun SettingsScreen(
	settings: AppSettings,
	onThemeChange: (ThemeMode) -> Unit,
	onNavigateToBetSize: () -> Unit,
	onContactClick: () -> Unit,
) {
	BaseScaffold {
		Column(
			modifier = Modifier.fillMaxSize(),
		) {
			HandyTopAppbar(
				title = stringResource(Res.string.settings_title),
				navigationType = TopAppbarType.Main,
			)
			HandyHorizontalDivider()

			Column(
				modifier = Modifier
					.fillMaxSize()
					.verticalScroll(rememberScrollState())
					.padding(horizontal = 16.dp, vertical = 20.dp),
				verticalArrangement = Arrangement.spacedBy(24.dp),
			) {
				// 테마 섹션
				ThemeSection(
					currentTheme = settings.themeMode,
					onThemeChange = onThemeChange,
				)

				// 게임 설정 섹션
				BetSizeNavigationItem(
					presets = settings.betSizePresets,
					onClick = onNavigateToBetSize,
				)

				// 문의하기
				ContactNavigationItem(onClick = onContactClick)

				// 앱 정보
				AppInfoSection()
			}
		}
	}
}

@Composable
private fun ThemeSection(
	currentTheme: ThemeMode,
	onThemeChange: (ThemeMode) -> Unit,
) {
	val colors = HandyTheme.colorScheme

	Column {
		HandySectionLabel(stringResource(Res.string.settings_theme))
		VerticalSpacer(8.dp)
		Column(
			modifier = Modifier
				.fillMaxWidth()
				.clip(RoundedCornerShape(12.dp))
				.background(colors.card),
		) {
			ThemeMode.entries.forEachIndexed { index, mode ->
				val isSelected = mode == currentTheme

				Row(
					modifier = Modifier
						.fillMaxWidth()
						.clickable { onThemeChange(mode) }
						.background(
							if (isSelected) colors.primary.copy(alpha = 0.1f) else colors.card,
						)
						.padding(horizontal = 16.dp, vertical = 14.dp),
					verticalAlignment = Alignment.CenterVertically,
					horizontalArrangement = Arrangement.spacedBy(12.dp),
				) {
					Icon(
						painter = painterResource(
							when (mode) {
								ThemeMode.AUTO -> Res.drawable.monitor
								ThemeMode.LIGHT -> Res.drawable.sun
								ThemeMode.DARK -> Res.drawable.moon
							},
						),
						contentDescription = null,
						modifier = Modifier.size(20.dp),
						tint = if (isSelected) colors.primary else colors.textSecondary,
					)

					Column(modifier = Modifier.weight(1f)) {
						Text(
							text = mode.localizedLabel(),
							style = HandyTheme.typography.medium14,
							color = if (isSelected) colors.primary else colors.textPrimary,
						)
						Text(
							text = mode.localizedDesc(),
							style = HandyTheme.typography.regular12,
							color = colors.textSecondary,
						)
					}

					if (isSelected) {
						Box(
							modifier = Modifier
								.size(8.dp)
								.clip(CircleShape)
								.background(colors.primary),
						)
					}
				}

				if (index < ThemeMode.entries.lastIndex) {
					HandyHorizontalDivider()
				}
			}
		}
	}
}

@Composable
private fun BetSizeNavigationItem(
	presets: List<Double>,
	onClick: () -> Unit,
) {
	val colors = HandyTheme.colorScheme

	Column {
		HandySectionLabel(stringResource(Res.string.settings_game))
		VerticalSpacer(8.dp)
		Row(
			modifier = Modifier
				.fillMaxWidth()
				.clip(RoundedCornerShape(12.dp))
				.background(colors.card)
				.clickable(onClick = onClick)
				.padding(16.dp),
			verticalAlignment = Alignment.CenterVertically,
			horizontalArrangement = Arrangement.SpaceBetween,
		) {
			Text(
				text = stringResource(Res.string.settings_bet_size_preset),
				style = HandyTheme.typography.medium14,
				color = colors.textPrimary,
			)
			Icon(
				painter = painterResource(Res.drawable.chevron_right),
				contentDescription = null,
				tint = colors.textSecondary,
				modifier = Modifier.size(16.dp),
			)
		}
	}
}

@Composable
private fun ContactNavigationItem(
	onClick: () -> Unit,
) {
	val colors = HandyTheme.colorScheme

	Column {
		HandySectionLabel(stringResource(Res.string.settings_support))
		VerticalSpacer(8.dp)
		Row(
			modifier = Modifier
				.fillMaxWidth()
				.clip(RoundedCornerShape(12.dp))
				.background(colors.card)
				.clickable(onClick = onClick)
				.padding(16.dp),
			verticalAlignment = Alignment.CenterVertically,
			horizontalArrangement = Arrangement.SpaceBetween,
		) {
			Text(
				text = stringResource(Res.string.settings_contact),
				style = HandyTheme.typography.medium14,
				color = colors.textPrimary,
			)
			Icon(
				painter = painterResource(Res.drawable.chevron_right),
				contentDescription = null,
				tint = colors.textSecondary,
				modifier = Modifier.size(16.dp),
			)
		}
	}
}

@Composable
private fun AppInfoSection() {
	val colors = HandyTheme.colorScheme

	Column {
		HandySectionLabel(stringResource(Res.string.settings_app_info))
		VerticalSpacer(8.dp)
		Column(
			modifier = Modifier
				.fillMaxWidth()
				.clip(RoundedCornerShape(12.dp))
				.background(colors.card)
				.padding(16.dp),
			verticalArrangement = Arrangement.spacedBy(8.dp),
		) {
			Row(
				modifier = Modifier.fillMaxWidth(),
				horizontalArrangement = Arrangement.SpaceBetween,
			) {
				Text(
					text = stringResource(Res.string.settings_version),
					style = HandyTheme.typography.regular14,
					color = colors.textSecondary,
				)
				Text(
					text = "1.0.0",
					style = HandyTheme.typography.medium14,
					color = colors.textPrimary,
				)
			}
		}
	}
}

@ThemePreviews
@Composable
private fun SettingsScreenPreview() {
	ThemePreview {
		SettingsScreen(
			settings = AppSettings(),
			onThemeChange = {},
			onNavigateToBetSize = {},
			onContactClick = {},
		)
	}
}

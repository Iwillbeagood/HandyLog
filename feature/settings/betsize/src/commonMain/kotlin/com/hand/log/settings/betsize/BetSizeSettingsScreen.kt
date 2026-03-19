package com.hand.log.settings.betsize

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.hand.log.designsystem.component.BaseScaffold
import com.hand.log.settings.betsize.contract.BetSizeState
import com.hand.log.designsystem.component.HandyHorizontalDivider
import com.hand.log.designsystem.component.HandySectionLabel
import com.hand.log.designsystem.component.HandyTextField
import com.hand.log.designsystem.component.HandyTopAppbar
import com.hand.log.designsystem.component.RegularButton
import com.hand.log.designsystem.component.VerticalSpacer
import com.hand.log.designsystem.etc.ThemePreview
import com.hand.log.designsystem.etc.ThemePreviews
import com.hand.log.designsystem.theme.HandyTheme
import handylog.core.res.generated.resources.Res
import handylog.core.res.generated.resources.x
import org.jetbrains.compose.resources.painterResource

@Composable
internal fun BetSizeSettingsScreen(
	state: BetSizeState,
	onAddPreflopPreset: (Double) -> Unit,
	onRemovePreflopPreset: (Double) -> Unit,
	onAddPostflopPreset: (Int) -> Unit,
	onRemovePostflopPreset: (Int) -> Unit,
	onBack: () -> Unit,
) {
	val colors = HandyTheme.colorScheme

	BaseScaffold(
		containerColor = colors.background,
		topBar = {
			HandyTopAppbar(
				title = "베팅 사이즈 프리셋",
				onBackEvent = onBack,
			)
		},
	) {
		Column(
			modifier = Modifier
				.fillMaxSize()
				.verticalScroll(rememberScrollState())
				.padding(horizontal = 16.dp, vertical = 16.dp),
			verticalArrangement = Arrangement.spacedBy(24.dp),
		) {
			PresetSection(
				title = "프리플랍 (BB)",
				description = "프리플랍에서 BB 배수로 빠른 베팅 사이즈를 선택합니다",
				presets = state.preflopPresets.map { v ->
					if (v % 1.0 == 0.0) "${v.toInt()}BB" else "${v}BB"
				},
				inputLabel = "예: 3.5",
				keyboardType = KeyboardType.Decimal,
				canAdd = state.canAddPreflop,
				onAdd = { input ->
					input.toDoubleOrNull()?.let { if (it > 0) onAddPreflopPreset(it) }
				},
				onRemove = { index ->
					state.preflopPresets.getOrNull(index)?.let { onRemovePreflopPreset(it) }
				},
			)

			PresetSection(
				title = "포스트플랍 (POT %)",
				description = "포스트플랍에서 팟 대비 퍼센트로 빠른 베팅 사이즈를 선택합니다",
				presets = state.postflopPresets.map { "$it%" },
				inputLabel = "예: 66",
				keyboardType = KeyboardType.Number,
				canAdd = state.canAddPostflop,
				onAdd = { input ->
					input.toIntOrNull()?.let { if (it > 0) onAddPostflopPreset(it) }
				},
				onRemove = { index ->
					state.postflopPresets.getOrNull(index)?.let { onRemovePostflopPreset(it) }
				},
			)
		}
	}
}

@Composable
private fun PresetSection(
	title: String,
	description: String,
	presets: List<String>,
	inputLabel: String,
	keyboardType: KeyboardType,
	canAdd: Boolean,
	onAdd: (String) -> Unit,
	onRemove: (Int) -> Unit,
) {
	val colors = HandyTheme.colorScheme
	var newValue by remember { mutableStateOf("") }
	val focusManager = LocalFocusManager.current

	Column {
		HandySectionLabel(title)
		VerticalSpacer(4.dp)
		Text(
			text = description,
			style = HandyTheme.typography.regular12,
			color = colors.textSecondary,
		)

		VerticalSpacer(12.dp)

		Row(
			modifier = Modifier.fillMaxWidth(),
			horizontalArrangement = Arrangement.spacedBy(8.dp),
			verticalAlignment = Alignment.CenterVertically,
		) {
			HandyTextField(
				value = newValue,
				onValueChange = { newValue = it },
				label = inputLabel,
				modifier = Modifier.weight(1f),
				keyboardType = keyboardType,
			)
			RegularButton(
				text = "추가",
				enabled = canAdd,
				onClick = {
					if (newValue.isNotBlank()) {
						onAdd(newValue)
						newValue = ""
						focusManager.clearFocus()
					}
				},
			)
		}

		VerticalSpacer(12.dp)

		if (presets.isEmpty()) {
			Box(
				modifier = Modifier
					.fillMaxWidth()
					.clip(RoundedCornerShape(12.dp))
					.background(colors.card)
					.padding(32.dp),
				contentAlignment = Alignment.Center,
			) {
				Text(
					text = "프리셋이 없습니다",
					style = HandyTheme.typography.regular14,
					color = colors.textSecondary,
				)
			}
		} else {
			Column(
				modifier = Modifier
					.fillMaxWidth()
					.clip(RoundedCornerShape(12.dp))
					.background(colors.card),
			) {
				presets.forEachIndexed { index, label ->
					Row(
						modifier = Modifier
							.fillMaxWidth()
							.padding(horizontal = 16.dp, vertical = 14.dp),
						verticalAlignment = Alignment.CenterVertically,
						horizontalArrangement = Arrangement.SpaceBetween,
					) {
						Text(
							text = label,
							style = HandyTheme.typography.bold16,
							color = colors.textPrimary,
						)
						Icon(
							painter = painterResource(Res.drawable.x),
							contentDescription = "삭제",
							modifier = Modifier
								.size(28.dp)
								.clip(RoundedCornerShape(4.dp))
								.clickable { onRemove(index) }
								.padding(4.dp),
							tint = colors.textSecondary,
						)
					}
					if (index < presets.lastIndex) {
						HandyHorizontalDivider()
					}
				}
			}
		}
	}
}

@ThemePreviews
@Composable
private fun BetSizeSettingsScreenPreview() {
	ThemePreview {
		BetSizeSettingsScreen(
			state = BetSizeState(
				preflopPresets = listOf(2.0, 2.5, 3.0, 4.0),
				postflopPresets = listOf(33, 50, 75, 100),
				canAddPreflop = false,
				canAddPostflop = false,
			),
			onAddPreflopPreset = {},
			onRemovePreflopPreset = {},
			onAddPostflopPreset = {},
			onRemovePostflopPreset = {},
			onBack = {},
		)
	}
}

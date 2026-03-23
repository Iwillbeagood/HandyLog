package com.hand.log.settings.betsize

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import org.jetbrains.compose.resources.stringResource
import handylog.core.res.generated.resources.*

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
				title = stringResource(Res.string.betsize_title),
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
				title = stringResource(Res.string.betsize_preflop),
				presets = state.preflopPresets.map { v ->
					if (v % 1.0 == 0.0) "${v.toInt()}BB" else "${v}BB"
				},
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
				title = stringResource(Res.string.betsize_postflop),
				presets = state.postflopPresets.map { "$it%" },
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
	presets: List<String>,
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

		if (presets.isEmpty()) {
			Text(
				text = stringResource(Res.string.betsize_empty),
				style = HandyTheme.typography.regular14,
				color = colors.textSecondary,
			)
		} else {
			LazyRow(
				horizontalArrangement = Arrangement.spacedBy(8.dp),
			) {
				itemsIndexed(presets) { index, label ->
					Row(
						modifier = Modifier
							.clip(RoundedCornerShape(20.dp))
							.background(colors.primary.copy(alpha = 0.12f))
							.padding(start = 14.dp, end = 6.dp, top = 8.dp, bottom = 8.dp),
						verticalAlignment = Alignment.CenterVertically,
						horizontalArrangement = Arrangement.spacedBy(6.dp),
					) {
						Text(
							text = label,
							style = HandyTheme.typography.bold14,
							color = colors.primary,
						)
						Icon(
							painter = painterResource(Res.drawable.x),
							contentDescription = stringResource(Res.string.btn_delete),
							modifier = Modifier
								.size(20.dp)
								.clip(CircleShape)
								.background(colors.primary.copy(alpha = 0.15f))
								.clickable { onRemove(index) }
								.padding(4.dp),
							tint = colors.primary,
						)
					}
				}
			}
		}

		VerticalSpacer(10.dp)

		Row(
			modifier = Modifier.fillMaxWidth(),
			verticalAlignment = Alignment.CenterVertically,
			horizontalArrangement = Arrangement.spacedBy(8.dp),
		) {
			HandyTextField(
				value = newValue,
				onValueChange = { newValue = it },
				keyboardType = keyboardType,
				modifier = Modifier.weight(8f),
			)
			RegularButton(
				text = stringResource(Res.string.btn_add),
				enabled = canAdd,
				textStyle = HandyTheme.typography.bold14,
				onClick = {
					if (newValue.isNotBlank()) {
						onAdd(newValue)
						newValue = ""
						focusManager.clearFocus()
					}
				},
				modifier = Modifier.weight(2f),
			)
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

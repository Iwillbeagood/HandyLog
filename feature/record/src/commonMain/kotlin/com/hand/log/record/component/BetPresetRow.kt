package com.hand.log.record.component

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hand.log.designsystem.component.RegularButton
import com.hand.log.designsystem.etc.ThemePreview
import com.hand.log.designsystem.etc.ThemePreviews
import com.hand.log.designsystem.theme.HandyTheme

@Composable
internal fun RaiseMultiplierPresetRow(
	lastBetAmount: Double,
	bbAmount: Double,
	useBbUnit: Boolean,
	onUpdateAmount: (String) -> Unit,
	modifier: Modifier = Modifier,
) {
	val multipliers = listOf(2.0, 3.0, 3.5, 4.0)

	Row(
		horizontalArrangement = Arrangement.spacedBy(8.dp),
		modifier = modifier.horizontalScroll(rememberScrollState()),
	) {
		multipliers.forEach { multiplier ->
			val chipAmount = lastBetAmount * multiplier
			val presetAmount = formatPresetAmount(chipAmount, bbAmount, useBbUnit)
			val label = if (multiplier % 1.0 == 0.0) {
				"x${multiplier.toInt()}"
			} else {
				"x$multiplier"
			}
			PresetButton(
				text = label,
				onClick = { onUpdateAmount(presetAmount) },
			)
		}
	}
}

@Composable
internal fun PreflopBBPresetRow(
	preflopPresets: List<Double>,
	bbAmount: Double,
	useBbUnit: Boolean,
	onUpdateAmount: (String) -> Unit,
	modifier: Modifier = Modifier,
) {
	Row(
		horizontalArrangement = Arrangement.spacedBy(8.dp),
		modifier = modifier.horizontalScroll(rememberScrollState()),
	) {
		preflopPresets.forEach { multiplier ->
			val presetAmount = if (useBbUnit) {
				formatBBValue(multiplier)
			} else {
				(bbAmount * multiplier).toLong().toString()
			}
			val label = if (multiplier % 1.0 == 0.0) {
				"${multiplier.toInt()}BB"
			} else {
				"${multiplier}BB"
			}
			PresetButton(
				text = label,
				onClick = { onUpdateAmount(presetAmount) },
			)
		}
	}
}

@Composable
internal fun PostflopPotPresetRow(
	postflopPresets: List<Int>,
	currentPot: Double,
	bbAmount: Double,
	useBbUnit: Boolean,
	onUpdateAmount: (String) -> Unit,
	modifier: Modifier = Modifier,
) {
	Row(
		horizontalArrangement = Arrangement.spacedBy(8.dp),
		modifier = modifier.horizontalScroll(rememberScrollState()),
	) {
		postflopPresets.forEach { percent ->
			val chipAmount = currentPot * percent / 100
			val presetAmount = formatPresetAmount(chipAmount, bbAmount, useBbUnit)
			PresetButton(
				text = "$percent%",
				onClick = { onUpdateAmount(presetAmount) },
			)
		}
	}
}

@Composable
internal fun StackPresetRow(
	bbAmount: Double,
	onUpdateStack: (String) -> Unit,
	modifier: Modifier = Modifier,
) {
	val presets = listOf(50, 100, 150, 200)

	Row(
		horizontalArrangement = Arrangement.spacedBy(8.dp),
		modifier = modifier.horizontalScroll(rememberScrollState()),
	) {
		presets.forEach { bbMultiplier ->
			val chipAmount = (bbAmount * bbMultiplier).toLong().toString()
			PresetButton(
				text = "${bbMultiplier}BB",
				onClick = { onUpdateStack(chipAmount) },
			)
		}
	}
}

@Composable
private fun PresetButton(
	text: String,
	onClick: () -> Unit,
) {
	val colors = HandyTheme.colorScheme
	RegularButton(
		text = text,
		onClick = onClick,
		containerColor = colors.muted,
		contentColor = colors.textPrimary,
		textStyle = HandyTheme.typography.medium14,
		borderStroke = 8.dp,
		verticalPadding = 8.dp,
		horizontalPadding = 16.dp,
	)
}

private fun formatPresetAmount(
	chipAmount: Double,
	bbAmount: Double,
	useBbUnit: Boolean,
): String {
	return if (useBbUnit && bbAmount > 0) {
		val bbCount = chipAmount / bbAmount
		formatBBValue(bbCount)
	} else {
		chipAmount.toLong().toString()
	}
}

private fun formatBBValue(value: Double): String {
	val rounded = (value * 10).toLong() / 10.0
	return if (rounded == rounded.toLong().toDouble()) {
		rounded.toLong().toString()
	} else {
		rounded.toString()
	}
}

@ThemePreviews
@Composable
private fun RaiseMultiplierPresetRowPreview() {
	ThemePreview {
		RaiseMultiplierPresetRow(
			lastBetAmount = 3000.0,
			bbAmount = 1000.0,
			useBbUnit = false,
			onUpdateAmount = {},
		)
	}
}

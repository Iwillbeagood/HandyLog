package com.hand.log.record.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.hand.log.designsystem.etc.ThemePreview
import com.hand.log.designsystem.etc.ThemePreviews
import com.hand.log.designsystem.theme.HandyTheme

@Immutable
internal data class Preset(val label: String, val value: String)

@Composable
internal fun PresetRow(
	presets: List<Preset>,
	onSelect: (String) -> Unit,
	modifier: Modifier = Modifier,
) {
	val colors = HandyTheme.colorScheme
	val shape = RoundedCornerShape(8.dp)

	Row(
		horizontalArrangement = Arrangement.spacedBy(8.dp),
		modifier = modifier.horizontalScroll(rememberScrollState()),
	) {
		presets.forEach { preset ->
			Box(
				modifier = Modifier
					.clip(shape)
					.background(colors.muted, shape)
					.clickable { onSelect(preset.value) }
					.padding(vertical = 8.dp, horizontal = 16.dp),
				contentAlignment = Alignment.Center,
			) {
				Text(
					text = preset.label,
					style = HandyTheme.typography.medium14,
					color = colors.textPrimary,
				)
			}
		}
	}
}

internal fun raiseMultiplierPresets(
	lastBetAmount: Double,
	bbAmount: Double,
	useBbUnit: Boolean,
): List<Preset> {
	val multipliers = listOf(2.0, 3.0, 3.5, 4.0)
	return multipliers.map { multiplier ->
		val chipAmount = lastBetAmount * multiplier
		val label = if (multiplier % 1.0 == 0.0) "x${multiplier.toInt()}" else "x$multiplier"
		Preset(label = label, value = formatPresetAmount(chipAmount, bbAmount, useBbUnit))
	}
}

internal fun preflopBBPresets(
	preflopPresets: List<Double>,
	bbAmount: Double,
	useBbUnit: Boolean,
): List<Preset> {
	return preflopPresets.map { multiplier ->
		val value = if (useBbUnit) {
			formatBBValue(
				multiplier,
			)
		} else {
			(bbAmount * multiplier).toLong().toString()
		}
		val label = if (multiplier % 1.0 == 0.0) "${multiplier.toInt()}BB" else "${multiplier}BB"
		Preset(label = label, value = value)
	}
}

internal fun postflopPotPresets(
	postflopPresets: List<Int>,
	currentPot: Double,
	bbAmount: Double,
	useBbUnit: Boolean,
): List<Preset> {
	return postflopPresets.map { percent ->
		val chipAmount = currentPot * percent / 100
		Preset(label = "$percent%", value = formatPresetAmount(chipAmount, bbAmount, useBbUnit))
	}
}

internal fun stackPresets(bbAmount: Double): List<Preset> {
	val multipliers = listOf(10, 15, 20, 50, 100, 200)
	return multipliers.map { bbMultiplier ->
		Preset(label = "${bbMultiplier}BB", value = (bbAmount * bbMultiplier).toLong().toString())
	}
}

internal fun blindsIncrementPresets(currentBb: Long): List<Preset> {
	val increments = listOf(100L, 1000L, 10000L)
	return increments.map { increment ->
		Preset(label = "+$increment", value = (currentBb + increment).toString())
	}
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
	val rounded = (value * 100).toLong() / 100.0
	return if (rounded == rounded.toLong().toDouble()) {
		rounded.toLong().toString()
	} else {
		rounded.toString()
	}
}

@ThemePreviews
@Composable
private fun PresetRowPreview() {
	ThemePreview {
		PresetRow(
			presets = raiseMultiplierPresets(
				lastBetAmount = 3000.0,
				bbAmount = 1000.0,
				useBbUnit = false,
			),
			onSelect = {},
		)
	}
}

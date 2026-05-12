package com.hand.log.record.component.street

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.hand.log.designsystem.component.HandyTextField
import com.hand.log.designsystem.component.VerticalSpacer
import com.hand.log.record.component.StackPresetRow
import com.hand.log.designsystem.etc.ThemePreview
import com.hand.log.designsystem.etc.ThemePreviews
import com.hand.log.designsystem.theme.HandyTheme
import handylog.core.res.generated.resources.Res
import handylog.core.res.generated.resources.player_stack
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun PlayerStackInput(
	initialStack: Double?,
	blindCost: Double,
	bbAmount: Double,
	posName: String,
	onValueChange: (String) -> Unit,
	modifier: Modifier = Modifier,
) {
	val colors = HandyTheme.colorScheme

	Column(modifier = modifier) {
		HandyTextField(
			value = initialStack?.toLong()?.toString() ?: "",
			onValueChange = onValueChange,
			label = stringResource(Res.string.player_stack),
			keyboardType = KeyboardType.Number,
		)
		if (blindCost > 0) {
			Text(
				text = "-${blindCost.toLong()} ($posName)",
				style = HandyTheme.typography.regular10,
				color = colors.textSecondary,
				modifier = Modifier.padding(top = 2.dp),
			)
		}
		if (bbAmount > 0) {
			VerticalSpacer(8.dp)
			StackPresetRow(
				bbAmount = bbAmount,
				onUpdateStack = onValueChange,
			)
		}
	}
}

@ThemePreviews
@Composable
private fun PlayerStackInputPreview() {
	ThemePreview {
		PlayerStackInput(
			initialStack = 50000.0,
			blindCost = 1000.0,
			bbAmount = 1000.0,
			posName = "BB",
			onValueChange = {},
		)
	}
}

@ThemePreviews
@Composable
private fun PlayerStackInputNoBlindPreview() {
	ThemePreview {
		PlayerStackInput(
			initialStack = 50000.0,
			blindCost = 0.0,
			bbAmount = 1000.0,
			posName = "UTG",
			onValueChange = {},
		)
	}
}

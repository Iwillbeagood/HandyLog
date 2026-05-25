package com.hand.log.record.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.hand.log.designsystem.component.HandyTextField
import com.hand.log.designsystem.component.RegularButton
import com.hand.log.designsystem.component.VerticalSpacer
import com.hand.log.designsystem.theme.HandyTheme
import com.hand.log.domain.model.ActionType
import com.hand.log.ui.stringRes
import com.hand.log.ui.poker.actionColor
import com.hand.log.domain.model.Street
import com.hand.log.designsystem.etc.ThemePreview
import com.hand.log.designsystem.etc.ThemePreviews
import org.jetbrains.compose.resources.stringResource
import handylog.core.res.generated.resources.Res
import handylog.core.res.generated.resources.*

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun ActionSelector(
	availableActions: List<ActionType>,
	selectedAction: ActionType?,
	onSelectAction: (ActionType) -> Unit,
	currentStreet: Street = Street.PREFLOP,
	currentAmount: String = "",
	onUpdateAmount: (String) -> Unit = {},
	onConfirmAction: () -> Unit = {},
	bbAmount: Double = 0.0,
	currentPot: Double = 0.0,
	preflopPresets: List<Double> = listOf(2.0, 2.5, 3.0, 4.0, 5.0),
	postflopPresets: List<Int> = listOf(33, 50, 75, 100),
	minRaiseAmount: Double = 0.0,
	lastBetAmount: Double = 0.0,
	useBbUnit: Boolean = false,
	playerStack: Double? = null,
	modifier: Modifier = Modifier,
) {
	val colors = HandyTheme.colorScheme
	val focusManager = LocalFocusManager.current

	Column(modifier = modifier) {
		FlowRow(
			horizontalArrangement = Arrangement.spacedBy(8.dp),
			verticalArrangement = Arrangement.spacedBy(8.dp),
		) {
			availableActions.forEach { actionType ->
				val isSelected = actionType == selectedAction
				val actionColors = actionType.actionColor()

				Box(
					modifier = Modifier
						.clip(RoundedCornerShape(8.dp))
						.background(if (isSelected) actionColors.background else colors.muted)
						.clickable {
							focusManager.clearFocus()
							onSelectAction(actionType)
						}
						.padding(horizontal = 16.dp, vertical = 10.dp),
				) {
					Text(
						text = stringResource(actionType.stringRes()),
						style = HandyTheme.typography.bold14,
						color = if (isSelected) actionColors.content else colors.textSecondary,
					)
				}
			}
		}

		val needsAmount = selectedAction == ActionType.BET ||
			selectedAction == ActionType.RAISE ||
			selectedAction == ActionType.ALL_IN

		if (needsAmount) {
			VerticalSpacer(12.dp)

			val displayMinRaise = minRaiseAmount.toLong().toString()

			val chipAmount = if (currentAmount.isNotBlank()) {
				val parsed = currentAmount.toDoubleOrNull() ?: 0.0
				if (useBbUnit && bbAmount > 0) parsed * bbAmount else parsed
			} else {
				0.0
			}
			val exceedsStack = playerStack != null && chipAmount > playerStack

			HandyTextField(
				value = currentAmount,
				onValueChange = onUpdateAmount,
				label = stringResource(Res.string.record_amount_label, displayMinRaise),
				keyboardType = KeyboardType.Number,
				onDone = if ((selectedAction == ActionType.BET || selectedAction == ActionType.RAISE) && !exceedsStack) {
					onConfirmAction
				} else {
					null
				},
			)
			VerticalSpacer(8.dp)

			val isRaiseOrBet = selectedAction == ActionType.RAISE || selectedAction == ActionType.BET
			val presets = when {
				isRaiseOrBet && lastBetAmount > 0 -> raiseMultiplierPresets(lastBetAmount, bbAmount, useBbUnit)
				currentStreet == Street.PREFLOP && bbAmount > 0 -> preflopBBPresets(
					preflopPresets,
					bbAmount,
					useBbUnit,
				)
				currentPot > 0 -> postflopPotPresets(postflopPresets, currentPot, bbAmount, useBbUnit)
				else -> emptyList()
			}
			if (presets.isNotEmpty()) {
				PresetRow(presets = presets, onSelect = onUpdateAmount)
			}

			if (selectedAction == ActionType.BET || selectedAction == ActionType.RAISE) {
				VerticalSpacer(12.dp)
				RegularButton(
					text = stringResource(Res.string.btn_confirm),
					onClick = onConfirmAction,
					enabled = !exceedsStack,
				)
			}
		}
	}
}

@ThemePreviews
@Composable
private fun ActionSelectorPreflopPreview() {
	ThemePreview {
		ActionSelector(
			availableActions = listOf(
				ActionType.FOLD,
				ActionType.CALL,
				ActionType.RAISE,
				ActionType.ALL_IN,
			),
			selectedAction = null,
			onSelectAction = {},
		)
	}
}

@ThemePreviews
@Composable
private fun ActionSelectorPreflopRaisePreview() {
	ThemePreview {
		ActionSelector(
			availableActions = listOf(
				ActionType.FOLD,
				ActionType.CALL,
				ActionType.RAISE,
				ActionType.ALL_IN,
			),
			selectedAction = ActionType.RAISE,
			onSelectAction = {},
			currentStreet = Street.PREFLOP,
			currentAmount = "3000",
			onUpdateAmount = {},
			onConfirmAction = {},
			bbAmount = 1000.0,
		)
	}
}

@ThemePreviews
@Composable
private fun ActionSelectorPostflopBetPreview() {
	ThemePreview {
		ActionSelector(
			availableActions = listOf(
				ActionType.FOLD,
				ActionType.CHECK,
				ActionType.BET,
				ActionType.ALL_IN,
			),
			selectedAction = ActionType.BET,
			onSelectAction = {},
			currentStreet = Street.FLOP,
			currentAmount = "5000",
			onUpdateAmount = {},
			onConfirmAction = {},
			currentPot = 10000.0,
		)
	}
}

@ThemePreviews
@Composable
private fun ActionSelectorBBCheckPreview() {
	ThemePreview {
		ActionSelector(
			availableActions = listOf(
				ActionType.CHECK,
				ActionType.RAISE,
				ActionType.ALL_IN,
			),
			selectedAction = ActionType.CHECK,
			onSelectAction = {},
		)
	}
}

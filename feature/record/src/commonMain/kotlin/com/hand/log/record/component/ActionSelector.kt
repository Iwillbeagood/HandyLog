package com.hand.log.record.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.hand.log.designsystem.component.HandyTextField
import com.hand.log.designsystem.component.RegularButton
import com.hand.log.designsystem.component.VerticalSpacer
import com.hand.log.designsystem.theme.HandyTheme
import com.hand.log.domain.model.ActionType
import com.hand.log.ui.poker.actionColor
import com.hand.log.domain.model.Street
import com.hand.log.designsystem.etc.ThemePreview
import com.hand.log.designsystem.etc.ThemePreviews

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
	maxAmount: Double = 0.0,
	showAmountWarning: Boolean = false,
	modifier: Modifier = Modifier,
) {
	val colors = HandyTheme.colorScheme

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
						.clickable { onSelectAction(actionType) }
						.padding(horizontal = 16.dp, vertical = 10.dp),
				) {
					Text(
						text = actionType.label,
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
			HandyTextField(
				value = currentAmount,
				onValueChange = onUpdateAmount,
				label = "금액 (최소 ${minRaiseAmount.toLong()})",
				keyboardType = KeyboardType.Number,
				onDone = if (selectedAction == ActionType.BET || selectedAction == ActionType.RAISE) {
					onConfirmAction
				} else {
					null
				},
			)
			AnimatedVisibility(visible = showAmountWarning) {
				Text(
					text = "스택(${maxAmount.toLong()})을 초과하면 올인으로 처리됩니다",
					style = HandyTheme.typography.regular10,
					color = colors.error,
					modifier = Modifier.padding(top = 4.dp),
				)
			}

			VerticalSpacer(8.dp)
			Row(
				horizontalArrangement = Arrangement.spacedBy(8.dp),
				modifier = Modifier.horizontalScroll(rememberScrollState()),
			) {
				if (currentStreet == Street.PREFLOP) {
					if (bbAmount > 0) {
						preflopPresets.forEach { multiplier ->
							val amount = (bbAmount * multiplier).toLong().toString()
							val label = if (multiplier % 1.0 == 0.0) {
								"${multiplier.toInt()}BB"
							} else {
								"${multiplier}BB"
							}
							RegularButton(
								text = label,
								onClick = { onUpdateAmount(amount) },
								containerColor = colors.muted,
								contentColor = colors.textPrimary,
								textStyle = HandyTheme.typography.medium14,
								borderStroke = 8.dp,
								verticalPadding = 8.dp,
								horizontalPadding = 16.dp,
							)
						}
					}
				} else {
					if (currentPot > 0) {
						postflopPresets.forEach { percent ->
							val amount = (currentPot * percent / 100).toLong().toString()
							RegularButton(
								text = "$percent%",
								onClick = { onUpdateAmount(amount) },
								containerColor = colors.muted,
								contentColor = colors.textPrimary,
								textStyle = HandyTheme.typography.medium14,
								borderStroke = 8.dp,
								verticalPadding = 8.dp,
								horizontalPadding = 16.dp,
							)
						}
					}
				}
			}

			if (selectedAction == ActionType.BET || selectedAction == ActionType.RAISE) {
				VerticalSpacer(12.dp)
				RegularButton(
					text = "확인",
					onClick = onConfirmAction,
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

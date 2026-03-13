package com.hand.log.ui.poker

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.hand.log.designsystem.theme.HandyTheme
import com.hand.log.domain.model.ActionType

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ActionButtons(
	availableActions: List<ActionType>,
	onActionSelected: (ActionType, Double?) -> Unit,
	modifier: Modifier = Modifier,
) {
	val colors = HandyTheme.colorScheme
	var showAmountInput by remember { mutableStateOf(false) }
	var pendingAction by remember { mutableStateOf<ActionType?>(null) }
	var amountText by remember { mutableStateOf("") }

	Column(modifier = modifier.fillMaxWidth()) {
		FlowRow(
			horizontalArrangement = Arrangement.spacedBy(8.dp),
			verticalArrangement = Arrangement.spacedBy(8.dp),
			modifier = Modifier.fillMaxWidth(),
		) {
			availableActions.forEach { action ->
				val buttonColor = when (action) {
					ActionType.FOLD -> colors.muted
					ActionType.CHECK -> colors.secondary
					ActionType.CALL -> colors.primary
					ActionType.BET -> colors.gold
					ActionType.RAISE -> colors.accent
					ActionType.ALL_IN -> colors.error
				}
				val contentColor = when (action) {
					ActionType.FOLD -> colors.textSecondary
					ActionType.CHECK -> colors.onSecondary
					ActionType.BET, ActionType.RAISE -> colors.card
					else -> colors.onPrimary
				}

				Button(
					onClick = {
						if (action == ActionType.BET || action == ActionType.RAISE) {
							pendingAction = action
							showAmountInput = true
							amountText = ""
						} else {
							onActionSelected(action, null)
						}
					},
					colors = ButtonDefaults.buttonColors(
						containerColor = buttonColor,
						contentColor = contentColor,
					),
					shape = RoundedCornerShape(8.dp),
				) {
					Text(text = action.label)
				}
			}
		}

		if (showAmountInput && pendingAction != null) {
			Row(
				modifier = Modifier
					.fillMaxWidth()
					.padding(top = 8.dp),
				horizontalArrangement = Arrangement.spacedBy(8.dp),
			) {
				OutlinedTextField(
					value = amountText,
					onValueChange = { amountText = it },
					label = { Text("금액") },
					keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
					modifier = Modifier.weight(1f),
					singleLine = true,
				)
				Button(
					onClick = {
						val amount = amountText.toDoubleOrNull()
						if (amount != null && amount > 0) {
							onActionSelected(pendingAction!!, amount)
							showAmountInput = false
							pendingAction = null
						}
					},
				) {
					Text("확인")
				}
				TextButton(
					onClick = {
						showAmountInput = false
						pendingAction = null
					},
				) {
					Text("취소")
				}
			}
		}
	}
}

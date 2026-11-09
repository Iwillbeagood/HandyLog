package com.hand.log.record.component

import androidx.compose.runtime.Composable
import com.hand.log.domain.model.ActionType
import com.hand.log.domain.model.Street
import com.hand.log.record.component.street.PostflopStepContent
import com.hand.log.record.component.street.PreflopStepContent
import com.hand.log.record.contract.RecordHandState

@Composable
internal fun StreetStepContent(
	state: RecordHandState.Recording,
	onSelectBoardCard: (Street) -> Unit,
	onSelectActionSeat: (Int) -> Unit,
	onSelectActionType: (ActionType) -> Unit,
	onUpdateActionAmount: (String) -> Unit,
	onUpdatePlayerStack: (Int, String) -> Unit,
	onConfirmAction: () -> Unit,
	onRemoveLastAction: () -> Unit,
	preflopPresets: List<Double> = listOf(2.0, 2.5, 3.0, 4.0, 5.0),
	postflopPresets: List<Int> = listOf(33, 50, 75, 100),
) {
	if (state.currentStreet == Street.PREFLOP) {
		PreflopStepContent(
			state = state,
			onSelectActionSeat = onSelectActionSeat,
			onSelectActionType = onSelectActionType,
			onUpdateActionAmount = onUpdateActionAmount,
			onUpdatePlayerStack = onUpdatePlayerStack,
			onConfirmAction = onConfirmAction,
			onRemoveLastAction = onRemoveLastAction,
			preflopPresets = preflopPresets,
			postflopPresets = postflopPresets,
		)
	} else {
		PostflopStepContent(
			state = state,
			onSelectBoardCard = onSelectBoardCard,
			onSelectActionType = onSelectActionType,
			onUpdateActionAmount = onUpdateActionAmount,
			onUpdatePlayerStack = onUpdatePlayerStack,
			onConfirmAction = onConfirmAction,
			onRemoveLastAction = onRemoveLastAction,
			preflopPresets = preflopPresets,
			postflopPresets = postflopPresets,
		)
	}
}

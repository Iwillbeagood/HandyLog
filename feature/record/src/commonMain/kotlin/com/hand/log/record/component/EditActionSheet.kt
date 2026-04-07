package com.hand.log.record.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import com.hand.log.designsystem.component.VerticalSpacer
import com.hand.log.designsystem.component.modal.SheetDragBlocker
import com.hand.log.designsystem.etc.ThemePreview
import com.hand.log.designsystem.etc.ThemePreviews
import com.hand.log.designsystem.theme.HandyTheme
import com.hand.log.domain.model.Action
import com.hand.log.domain.model.ActionType
import com.hand.log.domain.model.Blinds
import com.hand.log.domain.model.Card
import com.hand.log.domain.model.GameType
import com.hand.log.domain.model.HandStreets
import com.hand.log.domain.model.Player
import com.hand.log.domain.model.PocketCards
import com.hand.log.domain.model.PokerTable
import com.hand.log.domain.model.PreflopStreet
import com.hand.log.domain.model.Rank
import com.hand.log.record.model.calculateLastRaiseTo
import com.hand.log.domain.model.Suit
import com.hand.log.record.contract.RecordHandState
import com.hand.log.record.contract.RecordStep
import com.hand.log.ui.localizedLabel
import com.hand.log.ui.poker.indicatorColor
import handylog.core.res.generated.resources.Res
import handylog.core.res.generated.resources.record_edit_action_modify
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun EditActionSheet(
	state: RecordHandState.Recording,
	positionName: String,
	actionIndex: Int,
	onSelectActionType: (ActionType) -> Unit,
	onUpdateActionAmount: (String) -> Unit,
	onConfirmEditAction: (Int) -> Unit,
	onDismiss: () -> Unit,
) {
	val colors = HandyTheme.colorScheme
	val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

	ModalBottomSheet(
		onDismissRequest = onDismiss,
		sheetState = sheetState,
		containerColor = colors.card,
	) {
		EditActionSheetContent(
			state = state,
			positionName = positionName,
			actionIndex = actionIndex,
			onSelectActionType = onSelectActionType,
			onUpdateActionAmount = onUpdateActionAmount,
			onConfirmEditAction = onConfirmEditAction,
		)
	}
}

@Composable
internal fun EditActionSheetContent(
	state: RecordHandState.Recording,
	positionName: String,
	actionIndex: Int,
	onSelectActionType: (ActionType) -> Unit,
	onUpdateActionAmount: (String) -> Unit,
	onConfirmEditAction: (Int) -> Unit,
) {
	val colors = HandyTheme.colorScheme
	val streetActions = state.streets.getActions(state.currentStreet)
	val lastBet = calculateLastRaiseTo(
		streetActions = streetActions,
		currentStreet = state.currentStreet,
		bb = state.blinds?.bb ?: 0.0,
	)

	Column(
		modifier = Modifier
			.fillMaxWidth()
			.nestedScroll(SheetDragBlocker)
			.verticalScroll(rememberScrollState())
			.padding(horizontal = 16.dp)
			.padding(bottom = 32.dp),
	) {
		Text(
			text = "${stringResource(Res.string.record_edit_action_modify)} · $positionName",
			style = HandyTheme.typography.bold16,
			color = colors.textPrimary,
		)

		// 수정 전 액션 표시
		val currentAction = streetActions.getOrNull(actionIndex)
		if (currentAction != null) {
			VerticalSpacer(12.dp)
			CurrentActionInfo(
				action = currentAction,
				state = state,
			)
		}

		VerticalSpacer(16.dp)
		ActionSelector(
			availableActions = state.availableActions,
			selectedAction = state.currentActionType,
			onSelectAction = onSelectActionType,
			currentStreet = state.currentStreet,
			currentAmount = state.currentActionAmount,
			onUpdateAmount = onUpdateActionAmount,
			onConfirmAction = { onConfirmEditAction(actionIndex) },
			bbAmount = state.blinds?.bb ?: 0.0,
			currentPot = state.currentPot,
			preflopPresets = state.preflopPresets,
			postflopPresets = state.postflopPresets,
			minRaiseAmount = state.minRaiseAmount,
			maxAmount = state.currentActionSeat?.let { seat ->
				state.getPlayerStack(seat) + (state.players[seat]?.currentBet ?: 0.0)
			} ?: 0.0,
			lastBetAmount = lastBet,
			useBbUnit = state.useBbUnit,
		)
	}
}

@Composable
private fun CurrentActionInfo(
	action: Action,
	state: RecordHandState.Recording,
) {
	val colors = HandyTheme.colorScheme
	val actionLabel = action.localizedLabel()
	val amountText = action.amount?.let { " ${state.formatAmount(it)}" } ?: ""

	Row(
		modifier = Modifier
			.fillMaxWidth()
			.clip(RoundedCornerShape(8.dp))
			.background(colors.muted)
			.padding(horizontal = 12.dp, vertical = 10.dp),
		verticalAlignment = Alignment.CenterVertically,
		horizontalArrangement = Arrangement.spacedBy(8.dp),
	) {
		Text(
			text = actionLabel + amountText,
			style = HandyTheme.typography.medium14,
			color = action.type.indicatorColor(),
		)
	}
}

@ThemePreviews
@Composable
private fun EditActionSheetContentPreview() {
	ThemePreview {
		EditActionSheetContent(
			state = RecordHandState.Recording(
				tableId = "test",
				table = PokerTable(
					id = "test",
					date = LocalDate(2026, 3, 14),
					gameType = GameType.Cash(sb = 500.0, bb = 1000.0),
					heroSeat = 3,
					players = (1..6).map { Player(seat = it) },
					createdAt = 0L,
				),
				currentStep = RecordStep.PREFLOP,
				buttonSeat = 1,
				blinds = Blinds(sb = 500.0, bb = 1000.0),
				heroHand = PocketCards(Card(Rank.ACE, Suit.SPADES), Card(Rank.KING, Suit.SPADES)),
				currentActionSeat = 4,
				isEditing = true,
				streets = HandStreets(
					preflop = PreflopStreet(
						actions = listOf(
							Action(playerSeat = 4, type = ActionType.RAISE, amount = 2500.0, betLevel = 2),
							Action(playerSeat = 5, type = ActionType.FOLD),
							Action(playerSeat = 3, type = ActionType.CALL, amount = 2500.0),
						),
					),
				),
			),
			positionName = "UTG",
			actionIndex = 0,
			onSelectActionType = {},
			onUpdateActionAmount = {},
			onConfirmEditAction = {},
		)
	}
}

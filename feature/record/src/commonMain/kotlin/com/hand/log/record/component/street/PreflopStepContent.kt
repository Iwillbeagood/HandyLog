package com.hand.log.record.component.street

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hand.log.designsystem.component.VerticalSpacer
import com.hand.log.designsystem.etc.ThemePreview
import com.hand.log.designsystem.etc.ThemePreviews
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
import com.hand.log.domain.model.Street
import com.hand.log.domain.model.Suit
import com.hand.log.record.component.ActionTableView
import com.hand.log.record.contract.RecordHandState
import com.hand.log.record.contract.RecordStep
import com.hand.log.record.model.RecordPlayers
import kotlinx.datetime.LocalDate

@Composable
internal fun PreflopStepContent(
	state: RecordHandState.Recording,
	onSelectActionSeat: (Int) -> Unit,
	onSelectActionType: (ActionType) -> Unit,
	onUpdateActionAmount: (String) -> Unit,
	onUpdatePlayerStack: (Int, String) -> Unit,
	onConfirmAction: () -> Unit,
	onRemoveLastAction: () -> Unit,
	preflopPresets: List<Double> = listOf(2.0, 2.5, 3.0, 4.0, 5.0),
	postflopPresets: List<Int> = listOf(33, 50, 75, 100),
) {
	val streetActions = state.streets.getActions(Street.PREFLOP)
	val isOpenerSelection = streetActions.isEmpty() && state.currentActionSeat == null

	Column {
		ActionTableView(
			state = state,
			modifier = Modifier.fillMaxWidth(),
			onSeatClick = onSelectActionSeat,
		)

		if (isOpenerSelection) {
			VerticalSpacer(8.dp)
			OpenerSelectionHint()
		}

		if (streetActions.isNotEmpty()) {
			VerticalSpacer(8.dp)
			UndoButton(onClick = onRemoveLastAction)
		}

		if (!isOpenerSelection) {
			VerticalSpacer(16.dp)
			PlayerActionArea(
				state = state,
				onSelectActionType = onSelectActionType,
				onUpdateActionAmount = onUpdateActionAmount,
				onUpdatePlayerStack = onUpdatePlayerStack,
				onConfirmAction = onConfirmAction,
				preflopPresets = preflopPresets,
				postflopPresets = postflopPresets,
			)
		}

		VerticalSpacer(16.dp)
	}
}

@ThemePreviews
@Composable
private fun PreflopStepContentPreview() {
	ThemePreview {
		PreflopStepContent(
			state = RecordHandState.Recording(
				tableId = "test",
				table = PokerTable(
					id = "test",
					date = LocalDate(2026, 3, 14),
					gameType = GameType.Cash(sb = 500.0, bb = 1000.0),
					heroSeat = 3,
					maxPlayers = 6,
					players = (1..6).map { Player(seat = it) },
					createdAt = 0L,
				),
				currentStep = RecordStep.PREFLOP,
				buttonSeat = 1,
				blinds = Blinds(sb = 500.0, bb = 1000.0),
				players = RecordPlayers.create(playerCount = 6, defaultStack = 50000.0)
					.update(
						3,
					) { copy(cards = PocketCards(Card(Rank.ACE, Suit.SPADES), Card(Rank.KING, Suit.SPADES))) },
				currentActionSeat = 4,
				streets = HandStreets(
					preflop = PreflopStreet(
						actions = listOf(
							Action(playerSeat = 4, type = ActionType.RAISE, amount = 2500.0),
							Action(playerSeat = 5, type = ActionType.FOLD),
						),
					),
				),
			),
			onSelectActionSeat = {},
			onSelectActionType = {},
			onUpdateActionAmount = {},
			onUpdatePlayerStack = { _, _ -> },
			onConfirmAction = {},
			onRemoveLastAction = {},
		)
	}
}

@ThemePreviews
@Composable
private fun PreflopStepContentOpenerSelectionPreview() {
	ThemePreview {
		PreflopStepContent(
			state = RecordHandState.Recording(
				tableId = "test",
				table = PokerTable(
					id = "test",
					date = LocalDate(2026, 3, 14),
					gameType = GameType.Cash(sb = 500.0, bb = 1000.0),
					heroSeat = 3,
					maxPlayers = 6,
					players = (1..6).map { Player(seat = it) },
					createdAt = 0L,
				),
				currentStep = RecordStep.PREFLOP,
				buttonSeat = 1,
				blinds = Blinds(sb = 500.0, bb = 1000.0),
				players = RecordPlayers.create(playerCount = 6, defaultStack = 50000.0)
					.update(
						3,
					) { copy(cards = PocketCards(Card(Rank.ACE, Suit.SPADES), Card(Rank.KING, Suit.SPADES))) },
				currentActionSeat = null,
				streets = HandStreets(preflop = PreflopStreet()),
			),
			onSelectActionSeat = {},
			onSelectActionType = {},
			onUpdateActionAmount = {},
			onUpdatePlayerStack = { _, _ -> },
			onConfirmAction = {},
			onRemoveLastAction = {},
		)
	}
}
